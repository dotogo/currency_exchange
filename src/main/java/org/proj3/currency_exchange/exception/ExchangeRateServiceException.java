package org.proj3.currency_exchange.exception;

public class ExchangeRateServiceException extends RuntimeException {

    public ExchangeRateServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExchangeRateServiceException(String message) {
        super(message);
    }
}
