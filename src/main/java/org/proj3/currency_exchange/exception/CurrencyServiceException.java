package org.proj3.currency_exchange.exception;

public class CurrencyServiceException extends RuntimeException {

    public CurrencyServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
