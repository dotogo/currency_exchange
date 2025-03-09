package org.proj3.currency_exchange.service;

import org.proj3.currency_exchange.exception.ExchangeServiceException;
import org.proj3.currency_exchange.util.ExchangeUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ExchangeService {
    private static final ExchangeService instance = new ExchangeService();

    private static final String AMOUNT_ERROR_MESSAGE = "Please enter a valid amount: a number greater than 0," +
                                                       " less than a billion, no more than 6 decimal places (will be rounded to 2 digits).";

    private static final int MAX_AMOUNT_INTEGER_DIGITS = 9;
    private static final int MAX_AMOUNT_FRACTIONAL_DIGITS = 6;

    private ExchangeService() {

    }

    public static ExchangeService getInstance() {
        return instance;
    }

    public BigDecimal validateAmount(String amount) {
        try {
            return ExchangeUtil.validatePositiveNumber(amount, MAX_AMOUNT_INTEGER_DIGITS, MAX_AMOUNT_FRACTIONAL_DIGITS, AMOUNT_ERROR_MESSAGE)
                    .setScale(2, RoundingMode.HALF_EVEN);

        } catch (IllegalArgumentException e) {
            throw new ExchangeServiceException(e.getMessage());
        }
    }
}
