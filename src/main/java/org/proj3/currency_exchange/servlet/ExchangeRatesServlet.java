package org.proj3.currency_exchange.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.proj3.currency_exchange.dto.ExchangeRateRequestDto;
import org.proj3.currency_exchange.dto.ExchangeRateResponseDto;
import org.proj3.currency_exchange.exception.*;
import org.proj3.currency_exchange.service.ExchangeRateService;
import org.proj3.currency_exchange.util.JsonUtill;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@WebServlet("/exchangeRates")
public class ExchangeRatesServlet extends BaseServlet {
    private static final String FIELD_IS_MISSING = "A required form field is missing.";
    private static final String INTERNAL_SERVER_ERROR = "Internal server error.";
    private static final String EMPTY_BASE_CURRENCY_CODE = "Base currency code cannot be empty.";
    private static final String EMPTY_TARGET_CURRENCY_CODE = "Target currency code cannot be empty.";
    private static final String EMPTY_RATE = "Rate cannot be empty.";
    private static final String PAIR_ALREADY_EXISTS = "The currency pair already exists.";
    private static final String INVALID_EXCHANGE_RATE = "The exchange rate is not a number. Enter a positive number.";
    private static final String BASE_CURRENCY_PARAMETER = "baseCurrencyCode";
    private static final String TARGET_CURRENCY_PARAMETER = "targetCurrencyCode";
    private static final String RATE_PARAMETER = "rate";

    private final ExchangeRateService exchangeRateService = ExchangeRateService.getInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            List<ExchangeRateResponseDto> exchangeRates = exchangeRateService.findAll();
            String jsonResponse = JsonUtill.toJson(exchangeRates);

            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(jsonResponse);
        } catch (RuntimeException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR );
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        if (isParametersInvalid(req, resp)) {
            return;
        }

        String baseCurrencyCode = req.getParameter(BASE_CURRENCY_PARAMETER);
        String targetCurrencyCode = req.getParameter(TARGET_CURRENCY_PARAMETER);
        String parameterRate = req.getParameter(RATE_PARAMETER);

        BigDecimal exchangeRate = exchangeRateService.validateExchangeRate(parameterRate);

        try {
            String currencyPair = baseCurrencyCode + targetCurrencyCode;
            Optional<ExchangeRateResponseDto> dtoOptional = exchangeRateService.findByCode(currencyPair);
            if (dtoOptional.isPresent()) {
                sendErrorResponse(resp, HttpServletResponse.SC_CONFLICT, PAIR_ALREADY_EXISTS);
                return;
            }
        } catch (IllegalCurrencyCodeException | IllegalExchangeRateException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            return;

        } catch (ExchangeRateServiceException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
            return;
        }

        ExchangeRateRequestDto requestDto = new ExchangeRateRequestDto();
        requestDto.setBaseCurrencyCode(baseCurrencyCode);
        requestDto.setTargetCurrencyCode(targetCurrencyCode);
        requestDto.setRate(exchangeRate);

        try {
            Optional<ExchangeRateResponseDto> savedDtoOptional = exchangeRateService.save(requestDto);
            if (savedDtoOptional.isPresent()) {
                ExchangeRateResponseDto rateResponseDto = savedDtoOptional.get();
                String json = JsonUtill.toJson(rateResponseDto);
                resp.setStatus(HttpServletResponse.SC_CREATED);
                resp.getWriter().write(json);
            }

        } catch (IllegalExchangeRateException | ExchangeRateServiceException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isRequestParameterNamesInvalid(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Map<String, String[]> parameterMap = req.getParameterMap();

        if (!parameterMap.containsKey(BASE_CURRENCY_PARAMETER) ||
            !parameterMap.containsKey(TARGET_CURRENCY_PARAMETER) ||
            !parameterMap.containsKey(RATE_PARAMETER)) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, FIELD_IS_MISSING);
            return true;
        }
        return false;
    }

    private boolean isRateNotNumber(String parameterRate, HttpServletResponse resp) throws IOException {
        try {
            new BigDecimal(parameterRate);
            return false;
        } catch (NumberFormatException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, INVALID_EXCHANGE_RATE);
            return true;
        }
    }

    private boolean isRequestParametersEmpty(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String base = req.getParameter(BASE_CURRENCY_PARAMETER);
        String target = req.getParameter(TARGET_CURRENCY_PARAMETER);
        String rate = req.getParameter(RATE_PARAMETER);

        if (base == null || base.trim().isEmpty()) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, EMPTY_BASE_CURRENCY_CODE);
            return true;
        }

        if (target == null || target.trim().isEmpty()) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, EMPTY_TARGET_CURRENCY_CODE);
            return true;
        }

        if (rate == null || rate.trim().isEmpty()) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, EMPTY_RATE);
            return true;
        }
        return false;
    }

    private boolean isParametersInvalid(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        return isRequestParameterNamesInvalid(req, resp)
               || isRequestParametersEmpty(req, resp)
               || isRateNotNumber(req.getParameter(RATE_PARAMETER), resp);
    }

}
