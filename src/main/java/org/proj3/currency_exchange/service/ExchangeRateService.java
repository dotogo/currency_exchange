package org.proj3.currency_exchange.service;

import org.proj3.currency_exchange.dao.ExchangeRateDao;
import org.proj3.currency_exchange.dto.ExchangeRateResponseDto;
import org.proj3.currency_exchange.entity.ExchangeRateEntity;
import org.proj3.currency_exchange.exception.CurrencyServiceException;
import org.proj3.currency_exchange.exception.DaoException;
import org.proj3.currency_exchange.exception.ExchangeRateServiceException;
import org.proj3.currency_exchange.exception.IllegalCurrencyCodeException;
import org.proj3.currency_exchange.mapper.ExchangeRateMapper;
import org.proj3.currency_exchange.util.CurrencyUtil;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

public class ExchangeRateService {
    private static final ExchangeRateService instance = new ExchangeRateService();
    private static final ExchangeRateDao exchangeRateDao = ExchangeRateDao.getInstance();

    private static final String ERROR_FINDING_ALL_EXCHANGE_RATES = "\n>>> Something went wrong while finding all exchange rates :( <<<";
    private static final String ERROR_FINDING_BY_CODE = "\n>>> Something went wrong while finding for currency by code :( <<<";
    private static final String INVALID_CURRENCY_PAIR_LENGTH ="{\"message\": \"Invalid length of currency pair code. " +
                                                              "Please enter exactly 6 characters with real currency codes.\"}";

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

        Optional<ExchangeRateEntity> optionalRate;

        try {
            optionalRate = exchangeRateDao.findByCode(currencyPair);

            Optional<ExchangeRateResponseDto> dto = Optional.empty();
            if (optionalRate.isPresent()) {
                ExchangeRateEntity entity = optionalRate.get();
                ExchangeRateResponseDto responseDto = mapper.toDto(entity);
                dto = Optional.of(responseDto);
            }
            return dto;
        } catch (Exception e) {
            throw new CurrencyServiceException(ERROR_FINDING_BY_CODE, e);
        }
    }

    private void validatePairCodeLength(String pairCode) {
        if (pairCode.length() != 6) {
            throw new IllegalCurrencyCodeException(INVALID_CURRENCY_PAIR_LENGTH);
        }
    }

}
