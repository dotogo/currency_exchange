package org.proj3.currency_exchange.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.proj3.currency_exchange.dto.ErrorResponse;
import org.proj3.currency_exchange.exception.*;
import org.proj3.currency_exchange.util.JsonUtil;

import java.io.IOException;


@WebFilter(value = {
        "/currency/*", "/exchangeRate/*", "/exchangeRates", "/exchange"
})
public class ExceptionHandlingFilter extends HttpFilter {
    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {
        try {
            super.doFilter(req, res, chain);

        } catch (DaoException e) {
            sendErrorResponse(res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());

        } catch (IllegalCurrencyCodeException | IllegalArgumentException | IllegalPararmeterException e) {
            sendErrorResponse(res, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());

        } catch (NotFoundException e) {
            sendErrorResponse(res, HttpServletResponse.SC_NOT_FOUND, e.getMessage());

        } catch (EntityExistsException e) {
            sendErrorResponse(res, HttpServletResponse.SC_CONFLICT, e.getMessage());

        } catch (ServletException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendErrorResponse(HttpServletResponse resp, int status, String message) throws IOException {
        ErrorResponse errorResponse = new ErrorResponse(message);
        String json = JsonUtil.toJson(errorResponse);

        resp.setStatus(status);
        resp.getWriter().write(json);
    }
}
