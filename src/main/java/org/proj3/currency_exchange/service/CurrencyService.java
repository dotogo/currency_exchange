package org.proj3.currency_exchange.service;

import org.proj3.currency_exchange.dao.impl.CurrencyDao;
import org.proj3.currency_exchange.dto.CurrencyRequestDto;
import org.proj3.currency_exchange.dto.CurrencyResponseDto;
import org.proj3.currency_exchange.entity.CurrencyEntity;
import org.proj3.currency_exchange.exception.*;
import org.proj3.currency_exchange.mapper.CurrencyMapper;
import org.proj3.currency_exchange.util.CurrencyUtil;

import java.util.*;

public class CurrencyService {

    private static final String VALID_CURRENCY_NAME = "Invalid currency name. The only correct name for this code is: ";
    private static final String VALID_CURRENCY_SIGN = "Invalid currency sign. The only correct sign for this code is: ";

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
        List<CurrencyEntity> entities = currencyDao.findAll();

        List<CurrencyResponseDto> dtos = new ArrayList<>();
        for (CurrencyEntity entity : entities) {
            dtos.add(mapper.toDto(entity));
        }
        return dtos;
    }

    public Optional<CurrencyResponseDto> findByCode(String code) {
        code = CurrencyUtil.normalizeCurrencyCode(code);
        CurrencyUtil.validateCurrencyCode(code);

        Optional<CurrencyEntity> currency = currencyDao.find(code);

        Optional<CurrencyResponseDto> response = Optional.empty();
        if (currency.isPresent()) {
            CurrencyEntity entity = currency.get();
            CurrencyResponseDto responseDto = mapper.toDto(entity);
            return Optional.of(responseDto);
        }
        return response;
    }

    public CurrencyResponseDto save(CurrencyRequestDto currencyRequestDto) {
        String code = CurrencyUtil.normalizeCurrencyCode(currencyRequestDto.getCode());
        CurrencyUtil.validateCurrencyCode(code);

        Currency currency = Currency.getInstance(code);
        String displayName = currency.getDisplayName(Locale.US);
        String symbol = currency.getSymbol(Locale.US);

        String dtoName = normalizeName(currencyRequestDto.getName());
        String dtoSign = currencyRequestDto.getSign().trim().toUpperCase();

        if (isCurrencyNameInvalid(displayName, dtoName)) {
            throw new IllegalCurrencyNameException(VALID_CURRENCY_NAME + displayName);
        }

        if (isCurrencySignInvalid(symbol, dtoSign)) {
            throw new IllegalCurrencySignException(VALID_CURRENCY_SIGN + symbol);
        }

        currencyRequestDto.setCode(code);
        currencyRequestDto.setName(dtoName);
        currencyRequestDto.setSign(dtoSign);

        CurrencyEntity entity = mapper.toEntity(currencyRequestDto);
        CurrencyEntity savedCurrency = currencyDao.save(entity);

        return mapper.toDto(savedCurrency);
    }

    private String normalizeName(String name) {
        name = removeExtraSpaces(name);
        return capitalizeFirstLetters(name);
    }

    private String removeExtraSpaces(String name) {
        return name.trim().replaceAll("\\s+", " ");
    }

    private String capitalizeFirstLetters(String name) {
        String[] split = name.toLowerCase().split(" ");

        for (int i = 0; i < split.length; i++) {
            split[i] = split[i].substring(0, 1).toUpperCase() + split[i].substring(1);
        }
        return String.join(" ", split);
    }

    private boolean isCurrencyNameInvalid(String displayName, String currencyName) {
        return !displayName.equals(currencyName);
    }

    private boolean isCurrencySignInvalid(String symbol, String currencySign) {
        return !symbol.equals(currencySign);
    }

}
