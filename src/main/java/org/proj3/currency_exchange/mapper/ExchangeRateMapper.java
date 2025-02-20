package org.proj3.currency_exchange.mapper;

import org.proj3.currency_exchange.dto.CurrencyResponseDto;
import org.proj3.currency_exchange.dto.ExchangeRateRequestDto;
import org.proj3.currency_exchange.dto.ExchangeRateResponseDto;
import org.proj3.currency_exchange.entity.CurrencyEntity;
import org.proj3.currency_exchange.entity.ExchangeRateEntity;

import java.math.BigDecimal;

public class ExchangeRateMapper {
    private static final ExchangeRateMapper instance = new ExchangeRateMapper();

    private ExchangeRateMapper() {

    }

    public static ExchangeRateMapper getInstance() {
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

        CurrencyEntity baseCurrency = exchangeRateEntity.getBaseCurrency();
        CurrencyResponseDto baseCurrencyResponseDto = toDto(baseCurrency);

        CurrencyEntity targetCurrency = exchangeRateEntity.getTargetCurrency();
        CurrencyResponseDto targetCurrencyResponseDto = toDto(targetCurrency);

        dto.setId(exchangeRateEntity.getId());
        dto.setBaseCurrency(baseCurrencyResponseDto);
        dto.setTargetCurrency(targetCurrencyResponseDto);
        dto.setRate(exchangeRateEntity.getRate());

        return dto;
    }

    public CurrencyResponseDto toDto(CurrencyEntity entity) {
        CurrencyResponseDto dto = new CurrencyResponseDto();

        dto.setId(entity.getId());
        dto.setCode(entity.getCode());
        dto.setName(entity.getFullName());
        dto.setSign(entity.getSign());

        return dto;
    }
}
