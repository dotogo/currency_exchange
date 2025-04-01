package org.proj3.currency_exchange.servlet;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.proj3.currency_exchange.config.AppConfig;
import org.proj3.currency_exchange.dto.ExchangeRateResponseDto;
import org.proj3.currency_exchange.exception.ExchangeRateServiceException;
import org.proj3.currency_exchange.exception.IllegalCurrencyCodeException;
import org.proj3.currency_exchange.service.ExchangeRateService;
import org.proj3.currency_exchange.util.JsonUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@WebServlet("/exchangeRate/*")
public class ExchangeRateServlet extends BaseServlet {
    private static final String CURRENCY_CODES_MISSING_IN_ADDRESS = "Currency codes of the pair are missing in the address.";
    private static final String EXCHANGE_RATE_NOT_FOUND = "Exchange rate not found.";
    private static final String JSON_ERROR = "Error processing JSON. ";
    private static final String IO_ERROR = "Input/output data error. ";
    private static final String PARAMETER_IS_MISSING = "Missing required parameter: \"rate\"";
    private static final String EMPTY_RATE = "Rate cannot be empty.";
    private static final String INVALID_EXCHANGE_RATE = "Invalid exchange rate. " +
                                                        "Please enter a positive decimal number with no more than 6 decimal places.";
    private static final String CURRENCY_PAIR_IS_ABSENT = "The currency pair is absent in the database.";
    private static final String INVALID_CONTENT_TYPE = "Invalid Content-Type. Expected \"application/x-www-form-urlencoded\".";
    private static final String ERROR_READING_REQUEST_BODY = "Error reading request body";
    private static final String RATE = "rate";

    private final ExchangeRateService exchangeRateService = AppConfig.getExchangeRateService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String currencyPair = req.getPathInfo();

        if (validateAndSendErrorForEmptyCurrencyPair(resp, currencyPair)) {
            return;
        }

        try {
            Optional<ExchangeRateResponseDto> dtoOptional = exchangeRateService.findByCode(currencyPair);
            if (dtoOptional.isPresent()) {
                ExchangeRateResponseDto responseDto = dtoOptional.get();

                String json = JsonUtil.toJson(responseDto);

                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write(json);
            } else {
                sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, EXCHANGE_RATE_NOT_FOUND);
            }
        } catch (IllegalCurrencyCodeException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (JsonProcessingException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, JSON_ERROR + e.getMessage());
        } catch (IOException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, IO_ERROR + e.getMessage());
        }
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String method = req.getMethod();

        if (method.equals("PATCH")) {
            this.doPatch(req, resp);
        } else {
            super.service(req, resp);
        }
    }

    @Override
    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (validateAndSendErrorForInvalidContentType(req, resp)){
            return;
        }

        StringBuilder requestBody = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }
        } catch (IOException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ERROR_READING_REQUEST_BODY);
            return;
        }

        String body = requestBody.toString();

        Map<String, String> parameters = parseFormData(body);

        String currencyPair = req.getPathInfo();
        if (validateAndSendErrorForEmptyCurrencyPair(resp, currencyPair)) {
            return;
        }

        if (!parameters.containsKey(RATE)) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, PARAMETER_IS_MISSING);
            return;
        }

        if (validateAndSendErrorForEmptyRequestParameters(parameters, resp)) {
            return;
        }

        String rateFromParameter = parameters.get(RATE);

        try {
            rateFromParameter = rateFromParameter.trim().replaceAll(",", ".");
            BigDecimal exchangeRate = new BigDecimal(rateFromParameter);

            Optional<ExchangeRateResponseDto> updatedRateDtoOptional = exchangeRateService.update(currencyPair, exchangeRate);

            if (updatedRateDtoOptional.isPresent()) {
                ExchangeRateResponseDto responseDto = updatedRateDtoOptional.get();
                String json = JsonUtil.toJson(responseDto);

                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write(json);
            } else {
                sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, CURRENCY_PAIR_IS_ABSENT);
            }
        } catch (NumberFormatException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, INVALID_EXCHANGE_RATE);
        } catch (IllegalCurrencyCodeException | ExchangeRateServiceException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private boolean validateAndSendErrorForInvalidContentType(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!"application/x-www-form-urlencoded".equalsIgnoreCase(req.getContentType())) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, INVALID_CONTENT_TYPE);
            return true;
        }
        return false;
    }

    private Map<String, String> parseFormData(String data) {
        Map<String, String> paramMap = new HashMap<>();

        if (data == null || data.isEmpty()) {
            return paramMap;
        }

        String[] pairs = data.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2) {
                String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
                String value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8).trim();
                paramMap.put(key, value);
            }
        }
        return paramMap;
    }

    private boolean validateAndSendErrorForEmptyCurrencyPair(HttpServletResponse resp, String pair) throws IOException {
        if (isCurrencyPairEmpty(pair)){
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, CURRENCY_CODES_MISSING_IN_ADDRESS);
            return true;
        }
        return false;
    }

    private boolean isCurrencyPairEmpty(String unverifiedCurrencyPair) {
        return unverifiedCurrencyPair == null || unverifiedCurrencyPair.equals("/");
    }

    private boolean validateAndSendErrorForEmptyRequestParameters(Map<String, String> parameterMap, HttpServletResponse resp) throws IOException {
        String rate = parameterMap.get(RATE);

        if (rate == null || rate.trim().isEmpty()) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, EMPTY_RATE);
            return true;
        }
        return false;
    }
}
