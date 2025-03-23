package org.proj3.currency_exchange.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.proj3.currency_exchange.config.AppConfig;
import org.proj3.currency_exchange.dto.CurrencyRequestDto;
import org.proj3.currency_exchange.dto.CurrencyResponseDto;
import org.proj3.currency_exchange.exception.*;
import org.proj3.currency_exchange.service.CurrencyService;
import org.proj3.currency_exchange.util.JsonUtill;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@WebServlet("/currencies")
public class CurrenciesServlet extends BaseServlet {
    private static final String NAME = "name";
    private static final String CODE = "code";
    private static final String SIGN = "sign";

    private static final String REQUIRED_PARAMETERS_MISSING = "One or more parameters have invalid names or are missing. " +
                                                              "Required parameters: \"%s\", \"%s\", \"%s\"".formatted(NAME, CODE, SIGN);

    private static final String NAME_EMPTY = "Name cannot be empty.";
    private static final String CODE_EMPTY = "Code cannot be empty.";
    private static final String SIGN_EMPTY = "Sign cannot be empty.";

    private static final String CURRENCY_ALREADY_EXISTS = "A currency with this code already exists.";
    private static final String CURRENCY_CANNOT_BE_ADDED = "Currency cannot be added. ";
    private static final String DATABASE_ERROR = "Database error. ";

    private final CurrencyService currencyService = AppConfig.getCurrencyService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
                try {
            List<CurrencyResponseDto> currencies = currencyService.findAll();
            String json = JsonUtill.toJson(currencies);

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(json);
        } catch (CurrencyServiceException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Map<String, String[]> parameterMap = req.getParameterMap();

        if (!parameterMap.containsKey(NAME) || !parameterMap.containsKey(CODE) || !parameterMap.containsKey(SIGN)) {

            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, REQUIRED_PARAMETERS_MISSING);
            return;
        }

        String name = parameterMap.get(NAME)[0];
        String code = parameterMap.get(CODE)[0];
        String sign = parameterMap.get(SIGN)[0];

        if (name.trim().isEmpty()) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, NAME_EMPTY);
            return;
        }

        if (code.trim().isEmpty()) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, CODE_EMPTY);
            return;
        }

        if (sign.trim().isEmpty()) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, SIGN_EMPTY);
            return;
        }

        try {
            Optional<CurrencyResponseDto> dtoOptional = currencyService.findByCode(code);
            if (dtoOptional.isPresent()) {
                sendErrorResponse(resp, HttpServletResponse.SC_CONFLICT, CURRENCY_ALREADY_EXISTS);
                return;
            }

            CurrencyRequestDto requestDto = new CurrencyRequestDto(code, name, sign);

            Optional<CurrencyResponseDto> savedDtoOptional = currencyService.save(requestDto);
            if (savedDtoOptional.isPresent()) {
                CurrencyResponseDto currencyResponseDto = savedDtoOptional.get();

                String json = JsonUtill.toJson(currencyResponseDto);

                resp.setStatus(HttpServletResponse.SC_CREATED);
                resp.getWriter().write(json);
            }

        } catch (IllegalCurrencyCodeException | IllegalCurrencyNameException | IllegalCurrencySignException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, CURRENCY_CANNOT_BE_ADDED + e.getMessage());

        } catch (DaoException | CurrencyServiceException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, DATABASE_ERROR + e.getMessage());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
