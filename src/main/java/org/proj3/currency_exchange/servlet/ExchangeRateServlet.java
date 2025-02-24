package org.proj3.currency_exchange.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.proj3.currency_exchange.dto.ErrorResponse;
import org.proj3.currency_exchange.dto.ExchangeRateResponseDto;
import org.proj3.currency_exchange.service.ExchangeRateService;

import java.io.IOException;
import java.util.Optional;

@WebServlet("/exchangeRate/*")
public class ExchangeRateServlet extends HttpServlet {
    private static final String CURRENCY_CODES_MISSING_IN_ADDRESS = "{\"message\": \"Currency codes of the pair are missing in the address.\"}";
    private static final String EXCHANGE_RATE_NOT_FOUND = "{\"message\": \"Exchange rate not found.\"}";

    private final ExchangeRateService exchangeRateService = ExchangeRateService.getInstance();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String unverifiedCurrencyPairs = req.getPathInfo();

        if (unverifiedCurrencyPairs == null || unverifiedCurrencyPairs.equals("/")) {
            sendResponse(resp, HttpServletResponse.SC_BAD_REQUEST, CURRENCY_CODES_MISSING_IN_ADDRESS );
            return;
        }

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            Optional<ExchangeRateResponseDto> dtoOptional = exchangeRateService.findByCode(unverifiedCurrencyPairs);
            if (dtoOptional.isPresent()) {
                ExchangeRateResponseDto responseDto = dtoOptional.get();
                String responseAsString = mapper.writeValueAsString(responseDto);
                sendResponse(resp, HttpServletResponse.SC_OK, responseAsString);
            } else {
                sendResponse(resp, HttpServletResponse.SC_NOT_FOUND, EXCHANGE_RATE_NOT_FOUND);
            }
        } catch (Exception e) {
            sendResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private void sendResponse(HttpServletResponse resp, int status, String message) throws IOException {
        ErrorResponse errorResponse = new ErrorResponse(message);
        String json = mapper.writeValueAsString(errorResponse);

        resp.setStatus(status);
        resp.getWriter().write(json);
    }
}
