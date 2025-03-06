package org.proj3.currency_exchange.servlet;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletResponse;
import org.proj3.currency_exchange.dto.ErrorResponse;
import org.proj3.currency_exchange.util.JsonUtill;

import java.io.IOException;

public class BaseServlet extends HttpServlet {

    protected void sendErrorResponse(HttpServletResponse resp, int status, String message) throws IOException {
        ErrorResponse errorResponse = new ErrorResponse(message);
        String json = JsonUtill.toJson(errorResponse);

        resp.setStatus(status);
        resp.getWriter().write(json);
    }
}
