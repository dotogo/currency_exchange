package org.proj3.currency_exchange.exception;

public class IllegalExchangeRateException extends RuntimeException {

    public IllegalExchangeRateException(String message) {
        super(message);
    }
}
