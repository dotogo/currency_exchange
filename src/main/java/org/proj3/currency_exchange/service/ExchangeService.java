package org.proj3.currency_exchange.service;

import org.proj3.currency_exchange.util.ExchangeUtill;

import java.math.BigDecimal;

public class ExchangeService {
    private static final ExchangeService instance = new ExchangeService();

    private ExchangeService() {

    }

    public static ExchangeService getInstance() {
        return instance;
    }

    public BigDecimal validateAmount(String amount) {
        return ExchangeUtill.validateAmount(amount);
    }
}
