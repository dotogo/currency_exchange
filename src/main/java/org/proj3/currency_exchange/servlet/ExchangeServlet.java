package org.proj3.currency_exchange.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.proj3.currency_exchange.config.AppConfig;
import org.proj3.currency_exchange.dto.ExchangeRequestDto;
import org.proj3.currency_exchange.dto.ExchangeResponseDto;
import org.proj3.currency_exchange.service.ExchangeService;
import org.proj3.currency_exchange.util.ExchangeUtil;
import org.proj3.currency_exchange.util.JsonUtil;

import java.io.IOException;
import java.util.*;

@WebServlet("/exchange")
public class ExchangeServlet extends BaseServlet {
    private static final String PARAMETER_FROM = "from";
    private static final String PARAMETER_TO = "to";
    private static final String PARAMETER_AMOUNT = "amount";
    private static final String REQUIRED_PARAMETERS_MISSING = "One or more parameters have invalid names or are missing. " +
                                                              "Required parameters: \"%s\", \"%s\", \"%s\""
                                                                      .formatted(PARAMETER_FROM, PARAMETER_TO, PARAMETER_AMOUNT);

    private static final String FROM_FIELD_EMPTY = "The \"%s\" field is empty. Please provide the base currency code.".formatted(PARAMETER_FROM);
    private static final String TO_FIELD_EMPTY = "The \"%s\" field is empty. Please provide the target currency code.".formatted(PARAMETER_TO);
    private static final String AMOUNT_FIELD_EMPTY = "The \"%s\" field is empty. Please specify the amount.".formatted(PARAMETER_AMOUNT);

    private final ExchangeService exchangeService = AppConfig.getExchangeService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Map<String, String[]> parameterMap = req.getParameterMap();

        if (sendErrorIfParameterNameInvalid(parameterMap, resp)) {
            return;
        }

        List<ParameterCheck> paramsToCheck = getParametersToCheck(parameterMap);
        if (sendErrorIfParameterEmpty(paramsToCheck, resp)) {
            return;
        }

        String from = paramsToCheck.get(0).value();
        String to = paramsToCheck.get(1).value();
        String amount = paramsToCheck.get(2).value();

        ExchangeRequestDto requestDto = new ExchangeRequestDto(from, to, ExchangeUtil.convertToNumber(amount));

        ExchangeResponseDto responseDto = exchangeService.exchange(requestDto);

        String json = JsonUtil.toJson(responseDto);

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write(json);
    }

    private boolean sendErrorIfParameterNameInvalid(Map<String, String[]> parameterMap, HttpServletResponse resp) throws IOException {
        Set<String> validParameterNames = Set.of(PARAMETER_FROM, PARAMETER_TO, PARAMETER_AMOUNT);

        if (isParameterNamesInvalid(parameterMap, validParameterNames)) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, REQUIRED_PARAMETERS_MISSING);
            return true;
        }
        return false;
    }

    private boolean isParameterNamesInvalid(Map<String, String[]> parameterMap, Set<String> validNames) {
        Set<String> parameterNames = parameterMap.keySet();
        return !parameterNames.containsAll(validNames);
    }

    private boolean sendErrorIfParameterEmpty(List<ParameterCheck> parametersToCheck, HttpServletResponse resp) throws IOException {
        for (ParameterCheck parameter : parametersToCheck) {
            if (parameter.isEmpty()) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, parameter.errorMessage());
                return true;
            }
        }
        return false;
    }

    private List<ParameterCheck> getParametersToCheck(Map<String, String[]> parameterMap) {
        return List.of(
                new ParameterCheck(PARAMETER_FROM, parameterMap.get(PARAMETER_FROM)[0], FROM_FIELD_EMPTY),
                new ParameterCheck(PARAMETER_TO, parameterMap.get(PARAMETER_TO)[0], TO_FIELD_EMPTY),
                new ParameterCheck(PARAMETER_AMOUNT, parameterMap.get(PARAMETER_AMOUNT)[0], AMOUNT_FIELD_EMPTY)
        );
    }

    private record ParameterCheck(String name, String value, String errorMessage) {
        private boolean isEmpty() {
            return value == null || value.isEmpty();
        }
    }

}
