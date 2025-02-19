package org.proj3.currency_exchange.service;

public class ExchangeRateService {
    private static final ExchangeRateService instance = new ExchangeRateService();

    private ExchangeRateService() {

    }
    public static ExchangeRateService getInstance() {
        return instance;
    }
}
