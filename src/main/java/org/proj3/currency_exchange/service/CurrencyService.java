package org.proj3.currency_exchange.service;

import org.proj3.currency_exchange.dao.CurrencyDao;
import org.proj3.currency_exchange.dto.CurrencyResponseDto;
import org.proj3.currency_exchange.entity.CurrencyEntity;
import org.proj3.currency_exchange.mapper.CurrencyMapper;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

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
        Optional<CurrencyResponseDto> dto = Optional.empty();
        Optional<CurrencyEntity> optionalCurrency = Optional.empty();

        if (isCurrencyCodeExist(code)) {
            optionalCurrency = currencyDao.findByCode(code);
        }

        if (optionalCurrency.isPresent()) {
            CurrencyEntity entity = optionalCurrency.get();
            CurrencyResponseDto responseDto = mapper.toDto(entity);
            dto = Optional.of(responseDto);
        }
        return dto;
    }

//    public CurrencyEntity save(CurrencyRequestDto currencyRequestDto) {
//        CurrencyEntity entity = CurrencyMapper.getInstance().toEntity(currencyRequestDto);
//        return currencyDao.save(entity);
//    }

    public static CurrencyService getInstance() {
        return currencyService;
    }

    private boolean isCurrencyCodeExist(String currencyCode) {
        Currency currency = Currency.getInstance(currencyCode);
        return currency != null;
    }
}
