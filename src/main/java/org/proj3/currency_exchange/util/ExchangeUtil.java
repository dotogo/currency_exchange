package org.proj3.currency_exchange.util;

import java.math.BigDecimal;

public class ExchangeUtil {
    private static final String INVALID_NUMBER_FORMAT = "Invalid number format.";

    private ExchangeUtil() {

    }

    public static BigDecimal validatePositiveNumber(String number, int maxInteger, int maxFractional, String errorMessage) {
        BigDecimal value = null;
        try {
            value = new BigDecimal(number);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(INVALID_NUMBER_FORMAT);
        }

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
