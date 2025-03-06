package org.proj3.currency_exchange.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.proj3.currency_exchange.dto.CurrencyResponseDto;
import org.proj3.currency_exchange.service.CurrencyService;
import org.proj3.currency_exchange.util.JsonUtill;

import java.io.IOException;
import java.util.Optional;

@WebServlet("/currency/*")
public class CurrencyServlet extends HttpServlet {
    CurrencyService currencyService = CurrencyService.getInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String unverifiedCurrencyCode = req.getPathInfo();

        if (unverifiedCurrencyCode == null || unverifiedCurrencyCode.equals("/")) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "The currency code is missing in the request.");
            return;
        }

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            Optional<CurrencyResponseDto> dtoOptional = currencyService.findByCode(unverifiedCurrencyCode);
            if (dtoOptional.isPresent()) {
                CurrencyResponseDto responseDto = dtoOptional.get();
                String json = JsonUtill.toJson(responseDto);
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write(json);
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("Currency not found.");
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("Internal server error: " + e.getMessage());
        }
    }
}
