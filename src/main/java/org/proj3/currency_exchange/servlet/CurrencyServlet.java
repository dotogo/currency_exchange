package org.proj3.currency_exchange.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.proj3.currency_exchange.config.AppConfig;
import org.proj3.currency_exchange.dto.CurrencyResponseDto;
import org.proj3.currency_exchange.service.CurrencyService;
import org.proj3.currency_exchange.util.JsonUtil;

import java.io.IOException;
import java.util.Optional;

@WebServlet("/currency/*")
public class CurrencyServlet extends BaseServlet {
    private static final String CURRENCY_CODE_MISSING = "The currency code is missing in the request.";
    private static final String CURRENCY_NOT_FOUND = "Currency not found.";

    CurrencyService currencyService = AppConfig.getCurrencyService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String unverifiedCurrencyCode = req.getPathInfo();

        if (unverifiedCurrencyCode == null || unverifiedCurrencyCode.equals("/")) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, CURRENCY_CODE_MISSING);
            return;
        }

        Optional<CurrencyResponseDto> response = currencyService.findByCode(unverifiedCurrencyCode);
        if (response.isPresent()) {
            CurrencyResponseDto responseDto = response.get();
            String json = JsonUtil.toJson(responseDto);

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(json);
        } else {
            sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, CURRENCY_NOT_FOUND);
        }
    }
}
