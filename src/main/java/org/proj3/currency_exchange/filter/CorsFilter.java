package org.proj3.currency_exchange.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class CorsFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse res = (HttpServletResponse) response;
        res.setHeader("Access-Control-Allow-Origin", "http://localhost");  // Allow requests from the frontend
        res.setHeader("Access-Control-Allow-Methods", "GET, POST, PATCH, OPTIONS");
        res.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
//        res.setHeader("Access-Control-Allow-Credentials", "true"); // If you need cookies

        chain.doFilter(request, response);

    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
