package org.proj3.currency_exchange.servlet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.proj3.currency_exchange.dto.ErrorResponse;
import org.proj3.currency_exchange.dto.ExchangeRateResponseDto;
import org.proj3.currency_exchange.exception.IllegalCurrencyCodeException;
import org.proj3.currency_exchange.service.ExchangeRateService;

import java.io.IOException;
import java.util.Optional;

@WebServlet("/exchangeRate/*")
public class ExchangeRateServlet extends BaseServlet {
    private static final String CURRENCY_CODES_MISSING_IN_ADDRESS = "Currency codes of the pair are missing in the address.";
    private static final String EXCHANGE_RATE_NOT_FOUND = "Exchange rate not found.";
    private static final String JSON_ERROR = "Error processing JSON. ";
    private static final String IO_ERROR = "Input/output data error. ";

    private final ExchangeRateService exchangeRateService = ExchangeRateService.getInstance();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String unverifiedCurrencyPair = req.getPathInfo();

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        if (isCurrencyPairEmpty(unverifiedCurrencyPair, resp)) {
            return;
        }

        try {
            Optional<ExchangeRateResponseDto> dtoOptional = exchangeRateService.findByCode(unverifiedCurrencyPair);
            if (dtoOptional.isPresent()) {
                ExchangeRateResponseDto responseDto = dtoOptional.get();
                String responseAsString = mapper.writeValueAsString(responseDto);
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write(responseAsString);
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
    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String unverifiedCurrencyPair = req.getPathInfo();

        if (isCurrencyPairEmpty(unverifiedCurrencyPair, resp)) {
            return;
        }

        Optional<ExchangeRateResponseDto> dtoOptional = exchangeRateService.findByCode(unverifiedCurrencyPair);

    }

    private boolean isCurrencyPairEmpty(String unverifiedCurrencyPair, HttpServletResponse resp) throws IOException {
        if (unverifiedCurrencyPair == null || unverifiedCurrencyPair.equals("/")) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, CURRENCY_CODES_MISSING_IN_ADDRESS);
            return true;
        }
        return false;
    }
}
