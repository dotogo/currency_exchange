package org.proj3.currency_exchange.service;

import org.proj3.currency_exchange.dao.impl.CurrencyDao;
import org.proj3.currency_exchange.dao.impl.ExchangeRateDao;
import org.proj3.currency_exchange.dto.ExchangeRateRequestDto;
import org.proj3.currency_exchange.dto.ExchangeRateResponseDto;
import org.proj3.currency_exchange.entity.CurrencyEntity;
import org.proj3.currency_exchange.entity.ExchangeRateEntity;
import org.proj3.currency_exchange.exception.*;
import org.proj3.currency_exchange.mapper.ExchangeRateMapper;
import org.proj3.currency_exchange.util.CurrencyUtil;
import org.proj3.currency_exchange.util.ExchangeUtil;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ExchangeRateService {

    private static final String INVALID_CURRENCY_PAIR_LENGTH = "Invalid length of currency pair code. " +
                                                              "Please enter exactly 6 characters with real currency codes.";

    private static final String NO_BASE_CURRENCY = "There is no base currency in the database";
    private static final String NO_TARGET_CURRENCY = "There is no target currency in the database";

    private static final String RATE_ERROR_MESSAGE = "Please enter a valid rate: a number greater than 0," +
                                                     " less than a million, no more than 6 decimal places.";

    private static final String USE_DIFFERENT_CURRENCIES = "Use different currencies to create a new exchange rate.";

    private static final int MAX_RATE_INTEGER_DIGITS = 6;
    private static final int MAX_RATE_FRACTIONAL_DIGITS = 6;

    private final ExchangeRateDao exchangeRateDao;
    private final CurrencyDao currencyDao;
    private final ExchangeRateMapper mapper;

    private ExchangeRateService(ExchangeRateDao exchangeRateDao, CurrencyDao currencyDao, ExchangeRateMapper mapper) {
        this.exchangeRateDao = exchangeRateDao;
        this.currencyDao = currencyDao;
        this.mapper = mapper;

    }

    public static ExchangeRateService createInstance(ExchangeRateDao exchangeRateDao, CurrencyDao currencyDao, ExchangeRateMapper mapper) {
        return new ExchangeRateService(exchangeRateDao, currencyDao, mapper);
    }

    public List<ExchangeRateResponseDto> findAll() {
        List<ExchangeRateEntity> rateEntities = exchangeRateDao.findAll();

        return rateEntities.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    public Optional<ExchangeRateResponseDto> findByCode(String currencyPair) {
        currencyPair = CurrencyUtil.normalizeCurrencyCode(currencyPair);
        validatePairCodeLength(currencyPair);

        String baseCurrencyCode = currencyPair.substring(0, 3);
        String targetCurrencyCode = currencyPair.substring(3);

        CurrencyUtil.validateCurrencyCode(baseCurrencyCode);
        CurrencyUtil.validateCurrencyCode(targetCurrencyCode);

        Optional<ExchangeRateEntity> exchangeRate = exchangeRateDao.find(currencyPair);
        return exchangeRate.map(mapper::toDto);
    }

    public ExchangeRateResponseDto save(ExchangeRateRequestDto requestDto) {
        String baseCurrencyCode = CurrencyUtil.normalizeCurrencyCode(requestDto.getBaseCurrencyCode());
        CurrencyUtil.validateCurrencyCode(baseCurrencyCode);

        String targetCurrencyCode = CurrencyUtil.normalizeCurrencyCode(requestDto.getTargetCurrencyCode());
        CurrencyUtil.validateCurrencyCode(targetCurrencyCode);

        if (baseCurrencyCode.equals(targetCurrencyCode)) {
            throw new IllegalPararmeterException(USE_DIFFERENT_CURRENCIES);
        }

        BigDecimal exchangeRate = requestDto.getRate();
        validateExchangeRate(exchangeRate);

        CurrencyEntity baseCurrency = currencyDao.find(baseCurrencyCode)
                .orElseThrow(() -> new NotFoundException(NO_BASE_CURRENCY + ": " + baseCurrencyCode));

        CurrencyEntity targetCurrency = currencyDao.find(targetCurrencyCode)
                .orElseThrow(() -> new NotFoundException(NO_TARGET_CURRENCY + ": " + targetCurrencyCode));

        ExchangeRateEntity rate = new ExchangeRateEntity(baseCurrency, targetCurrency, exchangeRate);
        ExchangeRateEntity savedRate = exchangeRateDao.save(rate);

        return mapper.toDto(savedRate);
    }

    public ExchangeRateResponseDto update(String currencyPair, BigDecimal exchangeRate) {
        currencyPair = CurrencyUtil.normalizeCurrencyCode(currencyPair);

        validatePairCodeLength(currencyPair);
        validateExchangeRate(exchangeRate);

        String baseCurrencyCode = currencyPair.substring(0, 3);
        String targetCurrencyCode = currencyPair.substring(3);

        CurrencyUtil.validateCurrencyCode(baseCurrencyCode);
        CurrencyUtil.validateCurrencyCode(targetCurrencyCode);

        CurrencyEntity baseCurrency = currencyDao.find(baseCurrencyCode)
                .orElseThrow(() -> new NotFoundException(NO_BASE_CURRENCY));

        CurrencyEntity targetCurrency = currencyDao.find(targetCurrencyCode)
                .orElseThrow(() -> new NotFoundException(NO_TARGET_CURRENCY));

        int baseCurrencyId = baseCurrency.getId();
        int targetCurrencyId = targetCurrency.getId();

        ExchangeRateEntity updatedRate = exchangeRateDao.update(baseCurrencyId, targetCurrencyId, exchangeRate);
        return mapper.toDto(updatedRate);
    }

    private void validateExchangeRate(BigDecimal rate) {
            ExchangeUtil.validatePositiveNumber(
                    rate, MAX_RATE_INTEGER_DIGITS, MAX_RATE_FRACTIONAL_DIGITS, RATE_ERROR_MESSAGE);
    }

    private void validatePairCodeLength(String pairCode) {
        if (pairCode.length() != 6) {
            throw new IllegalCurrencyCodeException(INVALID_CURRENCY_PAIR_LENGTH);
        }
    }

}
