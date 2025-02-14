package org.proj3.currency_exchange.exception;

public class IllegalCurrencyNameException extends RuntimeException {

    public IllegalCurrencyNameException(String message) {
        super(message);
    }
}
