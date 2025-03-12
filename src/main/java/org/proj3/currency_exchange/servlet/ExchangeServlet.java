package org.proj3.currency_exchange.servlet;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.proj3.currency_exchange.dto.CurrencyResponseDto;
import org.proj3.currency_exchange.dto.ExchangeDto;
import org.proj3.currency_exchange.dto.ExchangeRateResponseDto;
import org.proj3.currency_exchange.exception.ExchangeServiceException;
import org.proj3.currency_exchange.exception.IllegalCurrencyCodeException;
import org.proj3.currency_exchange.service.CurrencyService;
import org.proj3.currency_exchange.service.ExchangeRateService;
import org.proj3.currency_exchange.service.ExchangeService;
import org.proj3.currency_exchange.util.JsonUtill;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@WebServlet("/exchange")
public class ExchangeServlet extends BaseServlet {
    private static final String FROM_FIELD_EMPTY = "The \"from\" field is empty. Please provide the base currency code.";
    private static final String TO_FIELD_EMPTY = "The \"to\" field is empty. Please provide the target currency code.";
    private static final String AMOUNT_FIELD_EMPTY = "The \"amount\" field is empty. Please specify the amount.";
    private static final String REQUIRED_PARAMETERS_MISSING = "A required parameter is missing.";
    private static final String NO_CURRENCY_IN_DATABASE = "Currency with code %s is not in the database. Please add currency before conversion.";
    private static final String NO_EXCHANGE_RATES_IN_DATABASE = "Currency exchange is not available. " +
                                                                "There is no direct, reverse or cross exchange rate (via USD) in the database.";
    private static final String USD = "USD";
    private static final String JSON_ERROR = "Error processing JSON. ";
    private static final String IO_ERROR = "Input/output data error. ";

    private static final String PARAMETER_FROM = "from";
    private static final String PARAMETER_TO = "to";
    private static final String PARAMETER_AMOUNT = "amount";

    private static final int DECIMAL_PLACES = 6;
    private static final int CONVERTED_AMOUNT_SCALE = 2;

    private final ExchangeRateService rateService = ExchangeRateService.getInstance();
    private final CurrencyService currencyService = CurrencyService.getInstance();
    private final ExchangeService exchangeService = ExchangeService.getInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        Map<String, String[]> parameterMap = req.getParameterMap();
        List<ParameterCheck> paramsToCheck = getParametersToCheck(parameterMap);

        try {
            if (sendErrorIfParameterNameInvalid(paramsToCheck, resp)) {
                return;
            }

            if (sendErrorIfParameterEmpty(paramsToCheck, resp)) {
                return;
            }

            String from = paramsToCheck.get(0).value();
            String to = paramsToCheck.get(1).value();
            String amount = paramsToCheck.get(2).value();

            System.out.println("String after checking");

            Optional<CurrencyResponseDto> baseDto = currencyService.findByCode(from);
            if (baseDto.isEmpty()) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, NO_CURRENCY_IN_DATABASE.formatted(from));
                return;
            }

            Optional<CurrencyResponseDto> targetDto = currencyService.findByCode(to);
            if (targetDto.isEmpty()) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, NO_CURRENCY_IN_DATABASE.formatted(to));
                return;
            }

            BigDecimal validatedAmount = exchangeService.validateAmount(amount);
            CurrencyResponseDto base = baseDto.get();
            CurrencyResponseDto target = targetDto.get();

            Optional<BigDecimal> rate = getDirectRate(from, to)
                                        .or(() -> getReverseRate(from, to)
                                        .or(() -> getCrossRate(from, to)));

            if (rate.isPresent()) {
                sendOkResponse(resp, validatedAmount, base, target, rate.get());
            } else {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, NO_EXCHANGE_RATES_IN_DATABASE);
            }

        } catch (IllegalCurrencyCodeException | ExchangeServiceException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (JsonProcessingException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, JSON_ERROR + e.getMessage());
        } catch (IOException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, IO_ERROR + e.getMessage());
        }
    }

    private boolean sendErrorIfParameterNameInvalid(List<ParameterCheck> parametersToCheck, HttpServletResponse resp) throws IOException {
        Set<String> validParameterNames = Set.of(PARAMETER_FROM, PARAMETER_TO, PARAMETER_AMOUNT);

        if (isParameterNamesInvalid(parametersToCheck, validParameterNames)) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, REQUIRED_PARAMETERS_MISSING);
            return true;
        }
        return false;
    }

    private boolean sendErrorIfParameterEmpty(List<ParameterCheck> parametersToCheck, HttpServletResponse resp) throws IOException {
        for (ParameterCheck parameter : parametersToCheck) {
            if (parameter.isEmpty()) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, parameter.errorMessage());
                return true;
            }
        }
        return false;
    }

    private void sendOkResponse(HttpServletResponse resp, BigDecimal validatedAmount, CurrencyResponseDto base,
                                CurrencyResponseDto target, BigDecimal rate) throws IOException {

        BigDecimal convertedAmount = rate.multiply(validatedAmount).setScale(CONVERTED_AMOUNT_SCALE, RoundingMode.HALF_EVEN).stripTrailingZeros();
        convertedAmount = new BigDecimal(convertedAmount.toPlainString());

        ExchangeDto exchangeDto = new ExchangeDto(base, target, rate, validatedAmount, convertedAmount);
        String json = JsonUtill.toJson(exchangeDto);

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write(json);
    }

    private boolean isParameterNamesInvalid(List<ParameterCheck> paramsToCheck, Set<String> parametersNames) {
        return !paramsToCheck.stream()
                .map(ParameterCheck::name)
                .collect(Collectors.toSet())
                .containsAll(parametersNames);
    }

    private List<ParameterCheck> getParametersToCheck(Map<String, String[]> parameterMap) {
        return List.of(
                new ParameterCheck(PARAMETER_FROM, parameterMap.get(PARAMETER_FROM)[0], FROM_FIELD_EMPTY),
                new ParameterCheck(PARAMETER_TO, parameterMap.get(PARAMETER_TO)[0], TO_FIELD_EMPTY),
                new ParameterCheck(PARAMETER_AMOUNT, parameterMap.get(PARAMETER_AMOUNT)[0], AMOUNT_FIELD_EMPTY)
        );
    }

    private Optional<BigDecimal> getDirectRate(String from, String to) {
        // A--->B
        // In the ExchangeRates table there is a currency pair AB - we take its rate
        Optional<ExchangeRateResponseDto> fromTo = rateService.findByCode(from + to);
        if (fromTo.isPresent()) {
            ExchangeRateResponseDto rateResponseDto = fromTo.get();
            BigDecimal rate = rateResponseDto.getRate();
            return Optional.of(rate);
        } else {
            return Optional.empty();
        }
    }

    private Optional<BigDecimal> getReverseRate(String from, String to) {
        // A--->B
        // In the ExchangeRates table there is a currency pair BA - we take its rate and calculate the reverse to get AB
        Optional<ExchangeRateResponseDto> toFrom = rateService.findByCode(to + from);
        if (toFrom.isPresent()) {
            ExchangeRateResponseDto rateResponseDto = toFrom.get();
            BigDecimal rate = BigDecimal.ONE.divide(rateResponseDto.getRate(), DECIMAL_PLACES, RoundingMode.HALF_EVEN);
            return Optional.of(rate);
        } else {
            return Optional.empty();
        }
    }

    private Optional<BigDecimal> getCrossRate(String from, String to) {
        // A--->B
        // In the ExchangeRates table there are currency pairs USD-A and USD-B - we calculate the AB rate from these rates
        Optional<ExchangeRateResponseDto> usdFrom = rateService.findByCode(USD + from);
        Optional<ExchangeRateResponseDto> usdTo = rateService.findByCode(USD + to);
        if (usdFrom.isPresent() && usdTo.isPresent()) {
            ExchangeRateResponseDto usdFromRateDto = usdFrom.get();
            BigDecimal usdFromRate = usdFromRateDto.getRate();

            ExchangeRateResponseDto usdToRateDto = usdTo.get();
            BigDecimal usdToRate = usdToRateDto.getRate();

            BigDecimal rate = usdToRate.divide(usdFromRate, 6, RoundingMode.HALF_EVEN);
            return Optional.of(rate);
        } else {
            return Optional.empty();
        }
    }

    private record ParameterCheck(String name, String value, String errorMessage) {
        private boolean isEmpty() {
            return value == null || value.isEmpty();
        }
    }

}
