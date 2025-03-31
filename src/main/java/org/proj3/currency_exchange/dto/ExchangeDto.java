package org.proj3.currency_exchange.dto;

import java.math.BigDecimal;

public record ExchangeDto(CurrencyResponseDto baseCurrency, CurrencyResponseDto targetCurrency, BigDecimal rate,
                          BigDecimal amount, BigDecimal convertedAmount) {
}
