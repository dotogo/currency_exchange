package org.proj3.currency_exchange.mapper;

import org.proj3.currency_exchange.dto.CurrencyRequestDto;
import org.proj3.currency_exchange.dto.CurrencyResponseDto;
import org.proj3.currency_exchange.entity.CurrencyEntity;

public class CurrencyMapper {
    private static final CurrencyMapper INSTANCE = new CurrencyMapper();

    private CurrencyMapper() {
    }

    public static CurrencyMapper getInstance() {
        return INSTANCE;
    }

    public CurrencyEntity toEntity(CurrencyRequestDto currencyRequestDto) {
        String code = currencyRequestDto.getCode();
        String fullName = currencyRequestDto.getName();
        String sign = currencyRequestDto.getSign();
        return new CurrencyEntity(code, fullName, sign);

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
