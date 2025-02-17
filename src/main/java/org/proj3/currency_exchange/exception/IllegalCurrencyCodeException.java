package org.proj3.currency_exchange.exception;

public class IllegalCurrencyCodeException extends RuntimeException {

    public IllegalCurrencyCodeException(String message) {
        super(message);
    }
    public IllegalCurrencyCodeException(String message, Throwable cause) {
        super(message, cause);
    }
}
