package org.proj3.currency_exchange.mapper;

import org.proj3.currency_exchange.dto.ExchangeRateRequestDto;
import org.proj3.currency_exchange.dto.ExchangeRateResponseDto;
import org.proj3.currency_exchange.entity.CurrencyEntity;
import org.proj3.currency_exchange.entity.ExchangeRateEntity;

import java.math.BigDecimal;

public class CurrencyExchangeRateMapper {
    private static final CurrencyExchangeRateMapper instance = new CurrencyExchangeRateMapper();

    private CurrencyExchangeRateMapper() {

    }

    public static CurrencyExchangeRateMapper getInstance() {
        return instance;
    }

    public ExchangeRateEntity toEntity(ExchangeRateRequestDto requestDto) {
        CurrencyEntity baseCurrency = requestDto.getBaseCurrency();
        CurrencyEntity targetCurrency = requestDto.getTargetCurrency();
        BigDecimal rate = requestDto.getRate();
        return new ExchangeRateEntity( baseCurrency, targetCurrency, rate);
    }

    public ExchangeRateResponseDto toDto(ExchangeRateEntity exchangeRateEntity) {
        ExchangeRateResponseDto dto = new ExchangeRateResponseDto();
        dto.setId(exchangeRateEntity.getId());
        dto.setBaseCurrency(exchangeRateEntity.getBaseCurrency());
        dto.setTargetCurrency(exchangeRateEntity.getTargetCurrency());
        dto.setRate(exchangeRateEntity.getRate());
        return dto;
    }
}
