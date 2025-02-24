package org.proj3.currency_exchange.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletResponse;
import org.proj3.currency_exchange.dto.ErrorResponse;

import java.io.IOException;

public class BaseServlet extends HttpServlet {
    private final ObjectMapper objectMapper = new ObjectMapper();

    protected void sendErrorResponse(HttpServletResponse resp, int status, String message) throws IOException {
        ErrorResponse errorResponse = new ErrorResponse(message);
        String json = objectMapper.writeValueAsString(errorResponse);

        resp.setStatus(status);
        resp.getWriter().write(json);
    }
}
