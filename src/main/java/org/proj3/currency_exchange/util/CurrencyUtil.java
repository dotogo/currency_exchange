package org.proj3.currency_exchange.util;

import org.proj3.currency_exchange.exception.IllegalCurrencyCodeException;

import java.util.Currency;

public final class CurrencyUtil {
    private static final String INVALID_CURRENCY_CODE = "Invalid currency code. Only real currency codes can be used.";
    private static final String EMPTY_CURRENCY_CODE = "Currency code cannot be null or empty";

    private CurrencyUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static void validateCurrencyCode(String currencyCode) {
        try {
            Currency.getInstance(currencyCode);
        } catch (IllegalArgumentException e) {
            throw new IllegalCurrencyCodeException(INVALID_CURRENCY_CODE, e);
        }
    }

    public static String normalizeCurrencyCode(String currencyCode) {
        if (currencyCode == null || currencyCode.isBlank()) {
            throw new IllegalCurrencyCodeException(EMPTY_CURRENCY_CODE);
        }
        currencyCode = currencyCode.trim();
        if (currencyCode.startsWith("/")) {
            currencyCode = currencyCode.substring(1);
        }
        return currencyCode.toUpperCase();
    }
}
