package org.proj3.currency_exchange.service;

import org.proj3.currency_exchange.dao.impl.ExchangeRateDao;
import org.proj3.currency_exchange.dto.CurrencyResponseDto;
import org.proj3.currency_exchange.dto.ExchangeRequestDto;
import org.proj3.currency_exchange.dto.ExchangeResponseDto;
import org.proj3.currency_exchange.entity.ExchangeRateEntity;
import org.proj3.currency_exchange.exception.IllegalPararmeterException;
import org.proj3.currency_exchange.mapper.CurrencyMapper;
import org.proj3.currency_exchange.util.CurrencyUtil;
import org.proj3.currency_exchange.util.ExchangeUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

public class ExchangeService {

    private static final String AMOUNT_ERROR_MESSAGE = "Please enter a valid amount: a number greater than 0," +
                                                       " less than a billion, no more than 6 decimal places (will be rounded to 2 digits).";
    private static final String USE_DIFFERENT_CURRENCIES = "Use different currencies for conversion.";
    private static final String NO_EXCHANGE_RATES_IN_DATABASE = "Currency exchange is not available. " +
                                                                "There is no direct, reverse or cross exchange rate (via USD) in the database.";
    private static final String USD = "USD";

    private static final int MAX_AMOUNT_INTEGER_DIGITS = 9;
    private static final int MAX_AMOUNT_FRACTIONAL_DIGITS = 6;

    private static final int CONVERTED_AMOUNT_SCALE = 2;
    private static final int DECIMAL_PLACES = 6;

    private final ExchangeRateDao rateDao;
    private final CurrencyMapper mapper;

    private ExchangeService(ExchangeRateDao rateDao, CurrencyMapper mapper) {
        this.rateDao = rateDao;
        this.mapper = mapper;
    }

    public static ExchangeService createInstance(ExchangeRateDao rateDao, CurrencyMapper mapper) {
        return new ExchangeService(rateDao, mapper);
    }

    public ExchangeResponseDto exchange(ExchangeRequestDto request) {
        String baseCurrencyCode = CurrencyUtil.normalizeCurrencyCode(request.getBaseCurrencyCode());
        CurrencyUtil.validateCurrencyCode(baseCurrencyCode);

        String targetCurrencyCode = CurrencyUtil.normalizeCurrencyCode(request.getTargetCurrencyCode());
        CurrencyUtil.validateCurrencyCode(targetCurrencyCode);

        BigDecimal amount = request.getAmount()
                .setScale(2, RoundingMode.HALF_EVEN);

        validateAmount(amount);

        if (baseCurrencyCode.equals(targetCurrencyCode)) {
            throw new IllegalPararmeterException(USE_DIFFERENT_CURRENCIES);
        }

        Optional<ExchangeRateEntity> rateOptional = getDirectRate(baseCurrencyCode, targetCurrencyCode)
                         .or(() -> getReverseRate(baseCurrencyCode, targetCurrencyCode)
                         .or(() -> getCrossRate(baseCurrencyCode, targetCurrencyCode)));

        if (rateOptional.isEmpty()) {
            throw new IllegalPararmeterException(NO_EXCHANGE_RATES_IN_DATABASE);
        }

        ExchangeRateEntity rateEntity = rateOptional.get();

        BigDecimal convertedAmount = calculateConvertedAmount(rateEntity.getRate(), amount);

        CurrencyResponseDto baseCurrencyResponseDto = mapper.toDto(rateEntity.getBaseCurrency());
        CurrencyResponseDto targetCurrencyResponseDto = mapper.toDto(rateEntity.getTargetCurrency());

        return new ExchangeResponseDto(
                baseCurrencyResponseDto, targetCurrencyResponseDto, rateEntity.getRate(), amount, convertedAmount);
    }

    public void validateAmount(BigDecimal amount) {
        ExchangeUtil.validatePositiveNumber(amount,
                MAX_AMOUNT_INTEGER_DIGITS,
                MAX_AMOUNT_FRACTIONAL_DIGITS,
                AMOUNT_ERROR_MESSAGE);
    }

    public BigDecimal calculateConvertedAmount(BigDecimal rate, BigDecimal validatedAmount) {
        return new BigDecimal(
                rate.multiply(validatedAmount)
                        .setScale(CONVERTED_AMOUNT_SCALE, RoundingMode.HALF_EVEN)
                        .stripTrailingZeros()
                        .toPlainString()
        );
    }

    public Optional<ExchangeRateEntity> getDirectRate(String from, String to) {
        // A--->B
        // In the ExchangeRates table there is a currency pair AB - we take its rate
        return rateDao.find(from + to);
    }

    public Optional<ExchangeRateEntity> getReverseRate(String from, String to) {
        // A--->B
        // In the ExchangeRates table there is a currency pair BA - we take its rate and calculate the reverse to get AB
        Optional<ExchangeRateEntity> toFrom = rateDao.find(to + from);
        if (toFrom.isPresent()) {
            ExchangeRateEntity rateEntity = toFrom.get();
            BigDecimal rate = BigDecimal.ONE.divide(rateEntity.getRate(), DECIMAL_PLACES, RoundingMode.HALF_EVEN);
            rateEntity.setRate(rate);
            return Optional.of(rateEntity);
        } else {
            return Optional.empty();
        }
    }

    public Optional<ExchangeRateEntity> getCrossRate(String from, String to) {
        // A--->B
        // In the ExchangeRates table there are currency pairs USD-A and USD-B - we calculate the AB rate from these rates
        Optional<ExchangeRateEntity> usdFrom = rateDao.find(USD + from);
        Optional<ExchangeRateEntity> usdTo = rateDao.find(USD + to);

        if (usdFrom.isPresent() && usdTo.isPresent()) {
            ExchangeRateEntity usdFromEntity = usdFrom.get();
            BigDecimal usdFromValue = usdFromEntity.getRate();

            ExchangeRateEntity usdToEntity = usdTo.get();
            BigDecimal usdToValue = usdToEntity.getRate();

            BigDecimal rate = usdToValue.divide(usdFromValue, 6, RoundingMode.HALF_EVEN);
            return Optional.of(
                    new ExchangeRateEntity(
                            usdFromEntity.getTargetCurrency(),
                            usdToEntity.getTargetCurrency(),
                            rate));
        } else {
            return Optional.empty();
        }
    }
}
