package org.proj3.currency_exchange.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ExchangeUtill {
    private static final String AMOUNT_ERROR_MESSAGE = "Please enter a valid amount: a number greater than 0," +
                                                       " less than a billion, no more than 6 decimal places (will be rounded to 2 digits).";
    private static final String RATE_ERROR_MESSAGE = "Please enter a valid rate: a number greater than 0," +
                                                     " less than a million, no more than 6 decimal places.";

    private static final int MAX_AMOUNT_INTEGER_DIGITS = 9;
    private static final int MAX_AMOUNT_FRACTIONAL_DIGITS = 6;

    private static final int MAX_RATE_INTEGER_DIGITS = 6;
    private static final int MAX_RATE_FRACTIONAL_DIGITS = 6;


    private ExchangeUtill() {

    }

    public static BigDecimal validateAmount(String amount) {
        try {
            return validate(amount, MAX_AMOUNT_INTEGER_DIGITS, MAX_AMOUNT_FRACTIONAL_DIGITS, AMOUNT_ERROR_MESSAGE)
                    .setScale(2, RoundingMode.HALF_EVEN);

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(AMOUNT_ERROR_MESSAGE);
        }
    }

    public static BigDecimal validateRate(String rate) {
        try {
            return validate(rate,MAX_RATE_INTEGER_DIGITS, MAX_RATE_FRACTIONAL_DIGITS, RATE_ERROR_MESSAGE);

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(RATE_ERROR_MESSAGE);
        }
    }

    private static BigDecimal validate(String number, int maxInteger, int maxFractional, String errorMessage) {
        BigDecimal value = new BigDecimal(number);

        if (value.compareTo(BigDecimal.ZERO) <= 0
            || isLengthInvalid(value, maxInteger, maxFractional)) {

            throw new IllegalArgumentException(errorMessage);
        }
        return value;
    }

    private static boolean isLengthInvalid(BigDecimal value, int maxInteger, int maxFractional) {
        int fractionalDigits = value.scale();
        int integerDigits = value.precision() - fractionalDigits;

        return integerDigits > maxInteger
               || fractionalDigits > maxFractional
               || (integerDigits + fractionalDigits) > (maxInteger + maxFractional);
    }
}
