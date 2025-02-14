package org.proj3.currency_exchange.service;

import org.proj3.currency_exchange.dao.CurrencyDao;
import org.proj3.currency_exchange.dto.CurrencyRequestDto;
import org.proj3.currency_exchange.dto.CurrencyResponseDto;
import org.proj3.currency_exchange.entity.CurrencyEntity;
import org.proj3.currency_exchange.exception.IllegalCurrencyCodeException;
import org.proj3.currency_exchange.exception.IllegalCurrencyNameException;
import org.proj3.currency_exchange.exception.IllegalCurrencySignException;
import org.proj3.currency_exchange.mapper.CurrencyMapper;

import java.util.*;

public class CurrencyService {
    private static final CurrencyService currencyService = new CurrencyService();
    private static final CurrencyDao currencyDao = CurrencyDao.getInstance();
    CurrencyMapper mapper = CurrencyMapper.getInstance();

    private CurrencyService() {
    }

    public List<CurrencyResponseDto> findAll() {
        List<CurrencyEntity> entities = currencyDao.findAll();

        List<CurrencyResponseDto> dtos = new ArrayList<>();
        for (CurrencyEntity entity : entities) {
            dtos.add(mapper.toDto(entity));
        }
        return dtos;
    }

    public Optional<CurrencyResponseDto> findByCode(String code) {
        if (code.charAt(0) == '/') {
            code = code.substring(1).toUpperCase();
        } else {
            code = code.toUpperCase();
        }

        Optional<CurrencyEntity> optionalCurrency;
        System.out.println(code);
        System.out.println();

        if (isCurrencyCodeValid(code)) {
            optionalCurrency = currencyDao.findByCode(code);
        } else {
            throw new IllegalCurrencyCodeException("Invalid currency code");
        }

        Optional<CurrencyResponseDto> dto = Optional.empty();
        if (optionalCurrency.isPresent()) {
            CurrencyEntity entity = optionalCurrency.get();
            CurrencyResponseDto responseDto = mapper.toDto(entity);
            dto = Optional.of(responseDto);
        }
        return dto;
    }

    public Optional<CurrencyResponseDto> save(CurrencyRequestDto currencyRequestDto) {
        if (!isCurrencyNameValid(currencyRequestDto.getCode(), currencyRequestDto.getName())) {
            throw new IllegalCurrencyNameException("Invalid currency name");
        }

        if (!isCurrencySignValid(currencyRequestDto.getCode(), currencyRequestDto.getSign())) {
            throw new IllegalCurrencySignException("Invalid currency sign");
        }

        CurrencyEntity entity = mapper.toEntity(currencyRequestDto);
        CurrencyEntity savedCurrency = currencyDao.save(entity);
        CurrencyResponseDto responseDto = mapper.toDto(savedCurrency);
        return Optional.of(responseDto);
    }

    public static CurrencyService getInstance() {
        return currencyService;
    }

    private boolean isCurrencyCodeValid(String currencyCode) {
        Currency currency = Currency.getInstance(currencyCode);
        return currency != null;
    }

    private boolean isCurrencyNameValid(String currencyCode, String currencyName) {
        Currency currency = Currency.getInstance(currencyCode);
        String displayName = currency.getDisplayName(Locale.US);
        return displayName.equals(currencyName);
    }

    private boolean isCurrencySignValid(String currencyCode, String currencySign) {
        Currency currency = Currency.getInstance(currencyCode);
        String symbol = currency.getSymbol(Locale.US);
        return symbol.equals(currencySign);
    }
}
