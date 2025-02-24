package org.proj3.currency_exchange.service;

import org.proj3.currency_exchange.dao.CurrencyDao;
import org.proj3.currency_exchange.dao.ExchangeRateDao;
import org.proj3.currency_exchange.dto.ExchangeRateRequestDto;
import org.proj3.currency_exchange.dto.ExchangeRateResponseDto;
import org.proj3.currency_exchange.entity.CurrencyEntity;
import org.proj3.currency_exchange.entity.ExchangeRateEntity;
import org.proj3.currency_exchange.exception.*;
import org.proj3.currency_exchange.mapper.ExchangeRateMapper;
import org.proj3.currency_exchange.util.CurrencyUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExchangeRateService {
    private static final ExchangeRateService instance = new ExchangeRateService();
    private static final ExchangeRateDao exchangeRateDao = ExchangeRateDao.getInstance();
    private static final CurrencyDao currencyDao = CurrencyDao.getInstance();

    private static final String ERROR_FINDING_ALL_EXCHANGE_RATES = ">>> Something went wrong while finding all exchange rates :( <<<";
    private static final String INVALID_CURRENCY_PAIR_LENGTH = "Invalid length of currency pair code. " +
                                                              "Please enter exactly 6 characters with real currency codes.";
    private static final String INVALID_EXCHANGE_RATE = "Invalid exchange rate. " +
                                                        "Please enter a positive decimal number with no more than 6 decimal places.";
    private static final String ERROR_FINDING_BASE_CURRENCY = "Database error. Base currency is not available.";
    private static final String ERROR_FINDING_TARGET_CURRENCY = "Database error. Target currency is not available.";

    private final ExchangeRateMapper mapper = ExchangeRateMapper.getInstance();

    private ExchangeRateService() {

    }

    public static ExchangeRateService getInstance() {
        return instance;
    }

    public List<ExchangeRateResponseDto> findAll() {
        try {
            List<ExchangeRateEntity> rateEntities = exchangeRateDao.findAll();

            List<ExchangeRateResponseDto> dtos = new ArrayList<>();
            for (ExchangeRateEntity rateEntity : rateEntities) {
                dtos.add(mapper.toDto(rateEntity));
            }
            return dtos;
        } catch (DaoException e) {
            throw new ExchangeRateServiceException(ERROR_FINDING_ALL_EXCHANGE_RATES, e);
        }
    }

    public Optional<ExchangeRateResponseDto> findByCode(String currencyPair) {
        currencyPair = CurrencyUtil.normalizeCurrencyCode(currencyPair);
        validatePairCodeLength(currencyPair);

        String baseCurrencyCode = currencyPair.substring(0, 3);
        String targetCurrencyCode = currencyPair.substring(3);

        CurrencyUtil.validateCurrencyCode(baseCurrencyCode);
        CurrencyUtil.validateCurrencyCode(targetCurrencyCode);

        try {
            Optional<ExchangeRateEntity> optionalRate = exchangeRateDao.findByCode(currencyPair);
            return optionalRate.map(mapper::toDto);
        } catch (DaoException e) {
            throw new CurrencyServiceException(e);
        }
    }

    public Optional<ExchangeRateResponseDto> save(ExchangeRateRequestDto requestDto) {
        BigDecimal exchangeRate = requestDto.getRate();

        if (exchangeRate.scale() > 6 || exchangeRate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalExchangeRateException(INVALID_EXCHANGE_RATE);
        }

        String baseCode = CurrencyUtil.normalizeCurrencyCode(requestDto.getBaseCurrencyCode());
        String targetCurrencyCode = CurrencyUtil.normalizeCurrencyCode(requestDto.getTargetCurrencyCode());

        CurrencyEntity baseCurrency = currencyDao.findByCode(baseCode)
                .orElseThrow(() -> new ExchangeRateServiceException(ERROR_FINDING_BASE_CURRENCY));

        CurrencyEntity targetCurrency = currencyDao.findByCode(targetCurrencyCode)
                .orElseThrow(() -> new ExchangeRateServiceException(ERROR_FINDING_TARGET_CURRENCY));

        ExchangeRateEntity rateToSave = new ExchangeRateEntity(baseCurrency, targetCurrency, exchangeRate);
        ExchangeRateEntity savedRate = exchangeRateDao.save(rateToSave);
        return Optional.of(mapper.toDto(savedRate));
    }

    private void validatePairCodeLength(String pairCode) {
        if (pairCode.length() != 6) {
            throw new IllegalCurrencyCodeException(INVALID_CURRENCY_PAIR_LENGTH);
        }
    }

}
