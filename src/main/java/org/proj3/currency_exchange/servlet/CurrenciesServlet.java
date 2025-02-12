package org.proj3.currency_exchange.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.proj3.currency_exchange.dto.CurrencyResponseDto;
import org.proj3.currency_exchange.service.CurrencyService;

import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@WebServlet("/currencies")
public class CurrenciesServlet extends HttpServlet {
    private final CurrencyService currencyService = CurrencyService.getInstance();
    private final ObjectMapper objectMapper = new ObjectMapper();


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            List<CurrencyResponseDto> currencies = currencyService.findAll();
            String jsonResponse = objectMapper.writeValueAsString(currencies);

            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            resp.getWriter().write(jsonResponse);
            resp.setStatus(HttpServletResponse.SC_OK);
        } catch (RuntimeException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"Internal server error\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Map<String, String[]> parameterMap = req.getParameterMap();

        if (!parameterMap.containsKey("name") || !parameterMap.containsKey("code") || !parameterMap.containsKey("sign")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("A required form field is missing.");
            return;
        }

        String currencyCode = parameterMap.get("code")[0];
        try {
            Optional<CurrencyResponseDto> currencyResponseDto = currencyService.findByCode(currencyCode);
            if(currencyResponseDto.isPresent()) {
                resp.setStatus(HttpServletResponse.SC_CONFLICT);
                resp.getWriter().write("A currency with this code already exists.");
                return;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        String[] codes = parameterMap.get("code");
        int length = codes.length;
        System.out.println("codes length " + length);

        if (length == 1) {
            System.out.println("codes length 1.  " + codes[0]);
        }
        System.out.println();
    }
}
