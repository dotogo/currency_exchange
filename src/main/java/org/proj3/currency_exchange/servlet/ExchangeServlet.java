package org.proj3.currency_exchange.servlet;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.proj3.currency_exchange.config.AppConfig;
import org.proj3.currency_exchange.dto.CurrencyResponseDto;
import org.proj3.currency_exchange.dto.ExchangeDto;
import org.proj3.currency_exchange.exception.ExchangeServiceException;
import org.proj3.currency_exchange.exception.IllegalCurrencyCodeException;
import org.proj3.currency_exchange.service.CurrencyService;
import org.proj3.currency_exchange.service.ExchangeService;
import org.proj3.currency_exchange.util.JsonUtill;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

@WebServlet("/exchange")
public class ExchangeServlet extends BaseServlet {
    private static final String PARAMETER_FROM = "from";
    private static final String PARAMETER_TO = "to";
    private static final String PARAMETER_AMOUNT = "amount";
    private static final String REQUIRED_PARAMETERS_MISSING = "One or more parameters have invalid names or are missing. " +
                                                              "Required parameters: \"%s\", \"%s\", \"%s\""
                                                                      .formatted(PARAMETER_FROM, PARAMETER_TO, PARAMETER_AMOUNT);

    private static final String FROM_FIELD_EMPTY = "The \"%s\" field is empty. Please provide the base currency code.".formatted(PARAMETER_FROM);
    private static final String TO_FIELD_EMPTY = "The \"%s\" field is empty. Please provide the target currency code.".formatted(PARAMETER_TO);
    private static final String AMOUNT_FIELD_EMPTY = "The \"%s\" field is empty. Please specify the amount.".formatted(PARAMETER_AMOUNT);

    private static final String NO_CURRENCY_IN_DATABASE = "Currency with code \"%s\" is not in the database. Please add currency before conversion.";
    private static final String NO_EXCHANGE_RATES_IN_DATABASE = "Currency exchange is not available. " +
                                                                "There is no direct, reverse or cross exchange rate (via USD) in the database.";

    private static final String JSON_ERROR = "Error processing JSON. ";
    private static final String IO_ERROR = "Input/output data error. ";


    private final CurrencyService currencyService = AppConfig.getCurrencyService();
    private final ExchangeService exchangeService = AppConfig.getExchangeService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Map<String, String[]> parameterMap = req.getParameterMap();

        try {
            if (sendErrorIfParameterNameInvalid(parameterMap, resp)) {
                return;
            }

            List<ParameterCheck> paramsToCheck = getParametersToCheck(parameterMap);
            if (sendErrorIfParameterEmpty(paramsToCheck, resp)) {
                return;
            }

            String from = paramsToCheck.get(0).value();
            String to = paramsToCheck.get(1).value();
            String amount = paramsToCheck.get(2).value();

            Optional<CurrencyResponseDto> baseDto = currencyService.findByCode(from);
            if (baseDto.isEmpty()) {
                sendErrorCurrencyIsMissing(resp, from);
                return;
            }

            Optional<CurrencyResponseDto> targetDto = currencyService.findByCode(to);
            if (targetDto.isEmpty()) {
                sendErrorCurrencyIsMissing(resp, to);
                return;
            }

            BigDecimal validatedAmount = exchangeService.validateAmount(amount);

            CurrencyResponseDto base = baseDto.get();
            CurrencyResponseDto target = targetDto.get();

            Optional<BigDecimal> rate = exchangeService.getDirectRate(from, to)
                                        .or(() -> exchangeService.getReverseRate(from, to)
                                        .or(() -> exchangeService.getCrossRate(from, to)));

            if (rate.isPresent()) {
                ExchangeDto exchangeDto = createDtoForOkResponse(validatedAmount, base, target, rate.get());
                sendOkResponse(resp, exchangeDto);
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

    private boolean sendErrorIfParameterNameInvalid(Map<String, String[]> parameterMap, HttpServletResponse resp) throws IOException {
        Set<String> validParameterNames = Set.of(PARAMETER_FROM, PARAMETER_TO, PARAMETER_AMOUNT);

        if (isParameterNamesInvalid(parameterMap, validParameterNames)) {
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

    private void sendErrorCurrencyIsMissing(HttpServletResponse resp, String currencyCode) throws IOException {
        sendErrorResponse(resp,
                HttpServletResponse.SC_BAD_REQUEST,
                NO_CURRENCY_IN_DATABASE.formatted(currencyCode.toUpperCase()));
    }

    private void sendOkResponse(HttpServletResponse resp, ExchangeDto exchangeDto) throws IOException {
        String json = JsonUtill.toJson(exchangeDto);

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write(json);
    }

    private ExchangeDto createDtoForOkResponse(BigDecimal validatedAmount, CurrencyResponseDto base,
                                          CurrencyResponseDto target, BigDecimal rate) {

        BigDecimal convertedAmount = exchangeService.calculateConvertedAmount(rate, validatedAmount);
        return new ExchangeDto(
                base, target, rate, validatedAmount, convertedAmount);
    }

    private boolean isParameterNamesInvalid(Map<String, String[]> parameterMap, Set<String> validNames) {
        Set<String> parameterNames = parameterMap.keySet();
        return !parameterNames.containsAll(validNames);
    }

    private List<ParameterCheck> getParametersToCheck(Map<String, String[]> parameterMap) {
        return List.of(
                new ParameterCheck(PARAMETER_FROM, parameterMap.get(PARAMETER_FROM)[0], FROM_FIELD_EMPTY),
                new ParameterCheck(PARAMETER_TO, parameterMap.get(PARAMETER_TO)[0], TO_FIELD_EMPTY),
                new ParameterCheck(PARAMETER_AMOUNT, parameterMap.get(PARAMETER_AMOUNT)[0], AMOUNT_FIELD_EMPTY)
        );
    }

    private record ParameterCheck(String name, String value, String errorMessage) {
        private boolean isEmpty() {
            return value == null || value.isEmpty();
        }
    }

}
