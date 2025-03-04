package org.proj3.currency_exchange.util;

import java.math.BigDecimal;

public class ExchangeUtill {
    private static final String INVALID_AMOUNT = "Please enter a valid amount: a number greater than 0, less than a million, no more than 6 decimal places.";

    private ExchangeUtill() {

    }

    public static BigDecimal validateAmount(String amount) {
        try {
            BigDecimal value = new BigDecimal(amount);
            if (value.compareTo(BigDecimal.ZERO) <= 0 || !isAmountLengthValid(value) ) {
                throw new IllegalArgumentException(INVALID_AMOUNT);
            }
            return value;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(INVALID_AMOUNT);
        }
    }

    private static boolean isAmountLengthValid(BigDecimal value) {
        int fractionalDigits = value.scale();
        int integerDigits = value.precision() - fractionalDigits;

        return integerDigits <= 6 && fractionalDigits <= 6 && (integerDigits + fractionalDigits) <= 12;
    }
}
