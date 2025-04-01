package org.proj3.currency_exchange.util;

import java.math.BigDecimal;

public final class ExchangeUtil {
    private static final String INVALID_NUMBER_FORMAT = "Invalid number format.";

    private ExchangeUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static BigDecimal validatePositiveNumber(String number, int maxInteger, int maxFractional, String errorMessage) {
        BigDecimal value = null;
        try {
            number = number.trim();
            number = number.replace(',', '.');
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

    public static void validatePositiveNumber(BigDecimal number, int maxInteger, int maxFractional, String errorMessage) {

        if (number.compareTo(BigDecimal.ZERO) <= 0
            || isLengthInvalid(number, maxInteger, maxFractional)) {

            throw new IllegalArgumentException(errorMessage);
        }
    }

    private static boolean isLengthInvalid(BigDecimal value, int maxInteger, int maxFractional) {
        int fractionalDigits = value.scale();
        int integerDigits = value.precision() - fractionalDigits;

        return integerDigits > maxInteger
               || fractionalDigits > maxFractional
               || (integerDigits + fractionalDigits) > (maxInteger + maxFractional);
    }
}
