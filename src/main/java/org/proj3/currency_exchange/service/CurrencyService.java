package org.proj3.currency_exchange.service;

import org.proj3.currency_exchange.dao.CurrencyDao;
import org.proj3.currency_exchange.dto.CurrencyRequestDto;
import org.proj3.currency_exchange.dto.CurrencyResponseDto;
import org.proj3.currency_exchange.entity.CurrencyEntity;
import org.proj3.currency_exchange.mapper.CurrencyMapper;

import java.util.ArrayList;
import java.util.List;

public class CurrencyService {
    private static final CurrencyService currencyService = new CurrencyService();
    private static final CurrencyDao currencyDao = CurrencyDao.getInstance();

    private CurrencyService() {
    }

    public List<CurrencyResponseDto> findAll() {
        List<CurrencyEntity> entities = currencyDao.findAll();
        CurrencyMapper mapper = CurrencyMapper.getInstance();

        List<CurrencyResponseDto> dtos = new ArrayList<>();
        for (CurrencyEntity entity : entities) {
            dtos.add(mapper.toDto(entity));
        }
        return dtos;
    }

    public CurrencyEntity save(CurrencyRequestDto currencyRequestDto) {
        CurrencyEntity entity = CurrencyMapper.getInstance().toEntity(currencyRequestDto);
        return currencyDao.save(entity);
    }

    public static CurrencyService getInstance() {
        return currencyService;
    }
}
