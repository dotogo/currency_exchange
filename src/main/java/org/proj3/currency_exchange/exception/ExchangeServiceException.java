package org.proj3.currency_exchange.exception;

public class ExchangeServiceException extends RuntimeException {

    public ExchangeServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
