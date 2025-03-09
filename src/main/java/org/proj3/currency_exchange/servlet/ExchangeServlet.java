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
import java.util.Map;
import java.util.Optional;

@WebServlet("/exchange")
public class ExchangeServlet extends BaseServlet {
    private static final String FROM_FIELD_EMPTY = "The \"from\" field is empty. Please provide the base currency code.";
    private static final String TO_FIELD_EMPTY = "The \"to\" field is empty. Please provide the target currency code.";
    private static final String AMOUNT_FIELD_EMPTY = "The \"amount\" field is empty. Please specify the amount.";
    private static final String REQUIRED_PARAMETERS_MISSING = "A required parameter is missing.";
    private static final String NO_CURRENCY_IN_DATABASE = "Currency with code %s is not in the database. Please add currency before conversion.";
    private static final String JSON_ERROR = "Error processing JSON. ";
    private static final String IO_ERROR = "Input/output data error. ";

    private final ExchangeRateService rateService = ExchangeRateService.getInstance();
    private final CurrencyService currencyService = CurrencyService.getInstance();
    private final ExchangeService exchangeService = ExchangeService.getInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        Map<String, String[]> parameterMap = req.getParameterMap();

        try {
            if (isParameterNamesInvalid(resp, parameterMap)) {
                return;
            }

            if(isParametersEmpty(resp, parameterMap)) {
                return;
            }

            String from = req.getParameter("from");
            String to = req.getParameter("to");
            String amount = req.getParameter("amount");

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

            // A--->B
            // In the ExchangeRates table there is a currency pair AB - we take its rate
            Optional<ExchangeRateResponseDto> fromTo = rateService.findByCode(from + to);
            if (fromTo.isPresent()) {
                ExchangeRateResponseDto rateResponseDto = fromTo.get();
                BigDecimal rate = rateResponseDto.getRate();
                sendOkResponse(resp, validatedAmount, base, target, rate);
            }

            // A--->B
            // In the ExchangeRates table there is a currency pair BA - we take its rate and calculate the reverse to get AB
            Optional<ExchangeRateResponseDto> toFrom = rateService.findByCode(to + from);
            if (toFrom.isPresent()) {
                ExchangeRateResponseDto rateResponseDto = toFrom.get();
                BigDecimal rate = BigDecimal.ONE.divide(rateResponseDto.getRate(), 6, RoundingMode.HALF_EVEN);
                sendOkResponse(resp, validatedAmount, base, target, rate);
                return;
            }

            // A--->B
            // In the ExchangeRates table there are currency pairs USD-A and USD-B - we calculate the AB rate from these rates
            Optional<ExchangeRateResponseDto> usdFrom = rateService.findByCode("USD" + from);
            Optional<ExchangeRateResponseDto> usdTo = rateService.findByCode("USD" + to);
            if (usdFrom.isPresent() && usdTo.isPresent()) {
                ExchangeRateResponseDto usdFromRateDto = usdFrom.get();
                BigDecimal usdFromRate = usdFromRateDto.getRate();

                ExchangeRateResponseDto usdToRateDto = usdTo.get();
                BigDecimal usdToRate = usdToRateDto.getRate();

                BigDecimal rate = usdToRate.divide(usdFromRate, 6, RoundingMode.HALF_EVEN);
                sendOkResponse(resp, validatedAmount, base, target, rate);
            }

        } catch (IllegalCurrencyCodeException | ExchangeServiceException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (JsonProcessingException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, JSON_ERROR + e.getMessage());
        } catch (IOException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, IO_ERROR + e.getMessage());
        }
    }

    private void sendOkResponse(HttpServletResponse resp, BigDecimal validatedAmount, CurrencyResponseDto base,
                                CurrencyResponseDto target, BigDecimal rate) throws IOException {
        BigDecimal convertedAmount = rate.multiply(validatedAmount).setScale(2, RoundingMode.HALF_EVEN).stripTrailingZeros();
        convertedAmount = new BigDecimal(convertedAmount.toPlainString());

        ExchangeDto exchangeDto = new ExchangeDto(base, target, rate, validatedAmount, convertedAmount);
        String json = JsonUtill.toJson(exchangeDto);

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write(json);
    }

    private boolean isParameterNamesInvalid(HttpServletResponse resp, Map<String, String[]> parameterMap) throws IOException {
        if (!parameterMap.containsKey("from") || !parameterMap.containsKey("to") || !parameterMap.containsKey("amount")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(REQUIRED_PARAMETERS_MISSING);
            return true;
        }
        return false;
    }

    private boolean isParametersEmpty(HttpServletResponse resp, Map<String, String[]> parameterMap) throws IOException {
        String from = parameterMap.get("from")[0];
        String to = parameterMap.get("to")[0];
        String amount = parameterMap.get("amount")[0];

        if (from == null || from.trim().isEmpty()) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, FROM_FIELD_EMPTY);
            return true;
        }

        if (to == null || to.trim().isEmpty()) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, TO_FIELD_EMPTY);
            return true;
        }

        if (amount == null || amount.trim().isEmpty()) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, AMOUNT_FIELD_EMPTY);
            return true;
        }
        return false;
    }

}
