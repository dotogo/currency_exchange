package org.proj3.currency_exchange.entity;

import java.math.BigDecimal;

public class ExchangeRateEntity {
    private int id;
    private CurrencyEntity baseCurrency;
    private CurrencyEntity targetCurrency;
    private BigDecimal rate;

    public ExchangeRateEntity() {

    }

    public ExchangeRateEntity(CurrencyEntity baseCurrency, CurrencyEntity targetCurrency, BigDecimal rate) {
        this.baseCurrency = baseCurrency;
        this.targetCurrency = targetCurrency;
        this.rate = rate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public CurrencyEntity getBaseCurrency() {
        return baseCurrency;
    }

    public void setBaseCurrency(CurrencyEntity baseCurrency) {
        this.baseCurrency = baseCurrency;
    }

    public CurrencyEntity getTargetCurrency() {
        return targetCurrency;
    }

    public void setTargetCurrency(CurrencyEntity targetCurrency) {
        this.targetCurrency = targetCurrency;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    @Override
    public String toString() {
        return "ExchangeRateEntity{" +
               "id=" + id +
               ", baseCurrency=" + baseCurrency +
               ", targetCurrency=" + targetCurrency +
               ", rate=" + rate +
               '}';
    }
}
