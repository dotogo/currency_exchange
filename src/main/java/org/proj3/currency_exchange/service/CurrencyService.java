package org.proj3.currency_exchange.service;

import org.proj3.currency_exchange.dao.CurrencyDao;
import org.proj3.currency_exchange.dto.CurrencyRequestDto;
import org.proj3.currency_exchange.dto.CurrencyResponseDto;
import org.proj3.currency_exchange.entity.CurrencyEntity;
import org.proj3.currency_exchange.exception.*;
import org.proj3.currency_exchange.mapper.CurrencyMapper;

import java.util.*;

public class CurrencyService {
    private static final CurrencyService currencyService = new CurrencyService();
    private static final CurrencyDao currencyDao = CurrencyDao.getInstance();

    private static final String INVALID_CURRENCY_CODE = "\n>>> Invalid currency code <<< \nOnly real currency codes can be entered.";
    private static final String ERROR_FINDING_BY_CODE = "\n>>> Something went wrong while finding for currency by code :( <<<";
    private static final String ERROR_FINDING_ALL_CURRENCIES = "\n>>> Something went wrong while finding all currencies :( <<<";
    private static final String ERROR_SAVING_CURRENCY = ">>> The currency was not saved <<<";

    private final CurrencyMapper mapper = CurrencyMapper.getInstance();

    private CurrencyService() {
    }

    public List<CurrencyResponseDto> findAll() {
        try {
            List<CurrencyEntity> entities = currencyDao.findAll();

            List<CurrencyResponseDto> dtos = new ArrayList<>();
            for (CurrencyEntity entity : entities) {
                dtos.add(mapper.toDto(entity));
            }
            return dtos;
        } catch (DaoException e) {
            throw new CurrencyServiceException(ERROR_FINDING_ALL_CURRENCIES, e);
        }
    }

    public Optional<CurrencyResponseDto> findByCode(String code) {
        code = uppercaseAndTrimSlash(code);
        Optional<CurrencyEntity> optionalCurrency;

        validateCurrencyCode(code);

        try {
            optionalCurrency = currencyDao.findByCode(code);

            Optional<CurrencyResponseDto> dto = Optional.empty();
            if (optionalCurrency.isPresent()) {
                CurrencyEntity entity = optionalCurrency.get();
                CurrencyResponseDto responseDto = mapper.toDto(entity);
                dto = Optional.of(responseDto);
            }
            return dto;
        } catch (Exception e) {
            throw new CurrencyServiceException(ERROR_FINDING_BY_CODE, e);
        }
    }

    public Optional<CurrencyResponseDto> save(CurrencyRequestDto currencyRequestDto) {
        String code = uppercaseAndTrimSlash(currencyRequestDto.getCode());
        currencyRequestDto.setCode(code);

        if (!isCurrencyNameValid(currencyRequestDto.getCode(), currencyRequestDto.getName())) {
            throw new IllegalCurrencyNameException("Invalid currency name: " + currencyRequestDto.getName());
        }

        if (!isCurrencySignValid(currencyRequestDto.getCode(), currencyRequestDto.getSign())) {
            throw new IllegalCurrencySignException("Invalid currency sign : " + currencyRequestDto.getSign());
        }

        try {
            CurrencyEntity entity = mapper.toEntity(currencyRequestDto);
            CurrencyEntity savedCurrency = currencyDao.save(entity);
            CurrencyResponseDto responseDto = mapper.toDto(savedCurrency);
            return Optional.of(responseDto);
        } catch (Exception e) {
            throw new CurrencyServiceException(ERROR_SAVING_CURRENCY, e);
        }
    }

    public static CurrencyService getInstance() {
        return currencyService;
    }

    private void validateCurrencyCode(String currencyCode) {
        try {
            Currency.getInstance(currencyCode);
        } catch (IllegalArgumentException e) {
            throw new IllegalCurrencyCodeException(INVALID_CURRENCY_CODE, e);
        }
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
