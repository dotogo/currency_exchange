package org.proj3.currency_exchange.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.proj3.currency_exchange.dto.CurrencyRequestDto;
import org.proj3.currency_exchange.dto.CurrencyResponseDto;
import org.proj3.currency_exchange.entity.CurrencyEntity;
import org.proj3.currency_exchange.exception.*;
import org.proj3.currency_exchange.mapper.CurrencyMapper;
import org.proj3.currency_exchange.service.CurrencyService;

import java.io.IOException;
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

        String currencyName = parameterMap.get("name")[0];
        String currencyCode = parameterMap.get("code")[0];
        String currencySign = parameterMap.get("sign")[0];

        if (currencyName.trim().isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Name cannot be empty.");
            return;
        }

        if (currencyCode.trim().isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Code cannot be empty.");
            return;
        }

        if (currencySign.trim().isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Sign cannot be empty.");
            return;
        }

        try {
            Optional<CurrencyResponseDto> dtoOptional = currencyService.findByCode(currencyCode);
            if (dtoOptional.isPresent()) {
                resp.setStatus(HttpServletResponse.SC_CONFLICT);
                resp.getWriter().write("A currency with this code already exists.");
                return;
            }

            CurrencyRequestDto requestDto = new CurrencyRequestDto();
            requestDto.setCode(currencyCode);
            requestDto.setName(currencyName);
            requestDto.setSign(currencySign);

            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");

            Optional<CurrencyResponseDto> savedDtoOptional = currencyService.save(requestDto);
            if (savedDtoOptional.isPresent()) {
                CurrencyResponseDto currencyResponseDto = savedDtoOptional.get();
                String responseAsString = objectMapper.writeValueAsString(currencyResponseDto);
                resp.setStatus(HttpServletResponse.SC_CREATED);
                resp.getWriter().write(responseAsString);
            }

        } catch (IllegalCurrencyCodeException | IllegalCurrencyNameException | IllegalCurrencySignException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("Currency cannot be added. " + e.getMessage());
        } catch (DaoException | CurrencyServiceException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("Database error. " + e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
