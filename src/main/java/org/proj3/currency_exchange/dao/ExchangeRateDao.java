package org.proj3.currency_exchange.dao;

public class ExchangeRateDao {
    private final static ExchangeRateDao INSTANCE = new ExchangeRateDao();

    private ExchangeRateDao() {
    }

    public static ExchangeRateDao getInstance() {
        return INSTANCE;
    }
}
