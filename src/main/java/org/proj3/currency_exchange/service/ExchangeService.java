package org.proj3.currency_exchange.service;

import org.proj3.currency_exchange.dto.ExchangeRateResponseDto;
import org.proj3.currency_exchange.exception.ExchangeServiceException;
import org.proj3.currency_exchange.util.ExchangeUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

public class ExchangeService {
    private static final ExchangeService instance = new ExchangeService();

    private static final String AMOUNT_ERROR_MESSAGE = "Please enter a valid amount: a number greater than 0," +
                                                       " less than a billion, no more than 6 decimal places (will be rounded to 2 digits).";
    private static final String USD = "USD";

    private static final int MAX_AMOUNT_INTEGER_DIGITS = 9;
    private static final int MAX_AMOUNT_FRACTIONAL_DIGITS = 6;

    private static final int CONVERTED_AMOUNT_SCALE = 2;
    private static final int DECIMAL_PLACES = 6;

    private final ExchangeRateService rateService = ExchangeRateService.getInstance();

    private ExchangeService() {

    }

    public static ExchangeService getInstance() {
        return instance;
    }

    public BigDecimal validateAmount(String amount) {
        try {
            return ExchangeUtil.validatePositiveNumber(amount,
                            MAX_AMOUNT_INTEGER_DIGITS,
                            MAX_AMOUNT_FRACTIONAL_DIGITS,
                            AMOUNT_ERROR_MESSAGE)
                    .setScale(2, RoundingMode.HALF_EVEN);

        } catch (IllegalArgumentException e) {
            throw new ExchangeServiceException(e.getMessage());
        }
    }

    public BigDecimal calculateConvertedAmount(BigDecimal rate, BigDecimal validatedAmount) {
        return new BigDecimal(
                rate.multiply(validatedAmount)
                        .setScale(CONVERTED_AMOUNT_SCALE, RoundingMode.HALF_EVEN)
                        .stripTrailingZeros()
                        .toPlainString()
        );
    }

    public Optional<BigDecimal> getDirectRate(String from, String to) {
        // A--->B
        // In the ExchangeRates table there is a currency pair AB - we take its rate
        Optional<ExchangeRateResponseDto> fromTo = rateService.findByCode(from + to);
        if (fromTo.isPresent()) {
            ExchangeRateResponseDto rateResponseDto = fromTo.get();
            BigDecimal rate = rateResponseDto.getRate();
            return Optional.of(rate);
        } else {
            return Optional.empty();
        }
    }

    public Optional<BigDecimal> getReverseRate(String from, String to) {
        // A--->B
        // In the ExchangeRates table there is a currency pair BA - we take its rate and calculate the reverse to get AB
        Optional<ExchangeRateResponseDto> toFrom = rateService.findByCode(to + from);
        if (toFrom.isPresent()) {
            ExchangeRateResponseDto rateResponseDto = toFrom.get();
            BigDecimal rate = BigDecimal.ONE.divide(rateResponseDto.getRate(), DECIMAL_PLACES, RoundingMode.HALF_EVEN);
            return Optional.of(rate);
        } else {
            return Optional.empty();
        }
    }

    public Optional<BigDecimal> getCrossRate(String from, String to) {
        // A--->B
        // In the ExchangeRates table there are currency pairs USD-A and USD-B - we calculate the AB rate from these rates
        Optional<ExchangeRateResponseDto> usdFrom = rateService.findByCode(USD + from);
        Optional<ExchangeRateResponseDto> usdTo = rateService.findByCode(USD + to);
        if (usdFrom.isPresent() && usdTo.isPresent()) {
            ExchangeRateResponseDto usdFromRateDto = usdFrom.get();
            BigDecimal usdFromRate = usdFromRateDto.getRate();

            ExchangeRateResponseDto usdToRateDto = usdTo.get();
            BigDecimal usdToRate = usdToRateDto.getRate();

            BigDecimal rate = usdToRate.divide(usdFromRate, 6, RoundingMode.HALF_EVEN);
            return Optional.of(rate);
        } else {
            return Optional.empty();
        }
    }
}
