package org.proj3.currency_exchange.service;

import org.proj3.currency_exchange.dao.CurrencyDao;
import org.proj3.currency_exchange.dto.CurrencyRequestDto;
import org.proj3.currency_exchange.dto.CurrencyResponseDto;
import org.proj3.currency_exchange.entity.CurrencyEntity;
import org.proj3.currency_exchange.exception.*;
import org.proj3.currency_exchange.mapper.CurrencyMapper;
import org.proj3.currency_exchange.util.CurrencyUtil;

import java.util.*;

public class CurrencyService {

    private static final String ERROR_FINDING_BY_CODE = ">>> Something went wrong while finding for currency by code :( <<<";
    private static final String ERROR_FINDING_ALL_CURRENCIES = ">>> Something went wrong while finding all currencies :( <<<";
    private static final String ERROR_SAVING_CURRENCY = ">>> The currency was not saved <<<";
    private static final String VALID_CURRENCY_NAME = "Invalid currency name. The only correct name for this code is: ";
    private static final String VALID_CURRENCY_SIGN = "Invalid currency name. The only correct sign for this code is: ";

    private final CurrencyDao currencyDao;
    private final CurrencyMapper mapper;

    private CurrencyService(CurrencyDao currencyDao, CurrencyMapper mapper) {
        this.currencyDao = currencyDao;
        this.mapper = mapper;
    }

    public static CurrencyService createInstance(CurrencyDao currencyDao, CurrencyMapper mapper) {
        return new CurrencyService(currencyDao, mapper);
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
        code = CurrencyUtil.normalizeCurrencyCode(code);
        CurrencyUtil.validateCurrencyCode(code);

        Optional<CurrencyEntity> optionalCurrency;

        try {
            optionalCurrency = currencyDao.find(code);

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
        String code = CurrencyUtil.normalizeCurrencyCode(currencyRequestDto.getCode());

        Currency currency = Currency.getInstance(code);
        String displayName = currency.getDisplayName(Locale.US);
        String symbol = currency.getSymbol(Locale.US);

        if (isCurrencyNameInvalid(displayName, currencyRequestDto.getName())) {
            throw new IllegalCurrencyNameException(VALID_CURRENCY_NAME + displayName);
        }

        if (isCurrencySignInvalid(symbol, currencyRequestDto.getSign())) {
            throw new IllegalCurrencySignException(VALID_CURRENCY_SIGN + symbol);
        }

        try {
            currencyRequestDto.setCode(code);
            CurrencyEntity entity = mapper.toEntity(currencyRequestDto);
            CurrencyEntity savedCurrency = currencyDao.save(entity);
            CurrencyResponseDto responseDto = mapper.toDto(savedCurrency);
            return Optional.of(responseDto);

        } catch (Exception e) {
            throw new CurrencyServiceException(ERROR_SAVING_CURRENCY, e);
        }
    }

    private boolean isCurrencyNameInvalid(String displayName, String currencyName) {
        return !displayName.equals(currencyName);
    }

    private boolean isCurrencySignInvalid(String symbol, String currencySign) {
        return !symbol.equals(currencySign);
    }

}
