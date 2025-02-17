package org.proj3.currency_exchange.service;

import org.proj3.currency_exchange.dao.CurrencyDao;
import org.proj3.currency_exchange.dto.CurrencyRequestDto;
import org.proj3.currency_exchange.dto.CurrencyResponseDto;
import org.proj3.currency_exchange.entity.CurrencyEntity;
import org.proj3.currency_exchange.exception.CurrencyServiceException;
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
        code = uppercaseAndTrimSlash(code);

        Optional<CurrencyEntity> optionalCurrency;
        System.out.println(code);
        System.out.println();

        if (isCurrencyCodeValid(code)) {
            optionalCurrency = currencyDao.findByCode(code);
        } else {
            throw new IllegalCurrencyCodeException("findByCode: >>>>> Invalid currency code. <<<<<");
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
        String code = uppercaseAndTrimSlash(currencyRequestDto.getCode());
        currencyRequestDto.setCode(code);

        if (!isCurrencyNameValid(currencyRequestDto.getCode(), currencyRequestDto.getName())) {
            throw new IllegalCurrencyNameException("Invalid currency name");
        }

        if (!isCurrencySignValid(currencyRequestDto.getCode(), currencyRequestDto.getSign())) {
            throw new IllegalCurrencySignException("Invalid currency sign");
        }

        try {
            CurrencyEntity entity = mapper.toEntity(currencyRequestDto);
            CurrencyEntity savedCurrency = currencyDao.save(entity);
            System.out.println("Saved currency :");
            System.out.println(savedCurrency.toString());

            CurrencyResponseDto responseDto = mapper.toDto(savedCurrency);
            return Optional.of(responseDto);
        } catch (Exception e) {
            throw new CurrencyServiceException("The currency was not saved.", e);
        }
    }

    public static CurrencyService getInstance() {
        return currencyService;
    }

    private boolean isCurrencyCodeValid(String currencyCode) throws IllegalArgumentException {

        Currency currency = Currency.getInstance(currencyCode);
//        try {
//            currency = Currency.getInstance(currencyCode);
//        } catch (Exception e) {
//            throw new IllegalCurrencyCodeException("isCurrencyCodeValid: >>>>> Invalid currency code <<<<<", e);
//        }
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

    private String uppercaseAndTrimSlash(String code) {
        if (code.charAt(0) == '/') {
            code = code.substring(1).toUpperCase();
        } else {
            code = code.toUpperCase();
        }
        return code;
    }
}
