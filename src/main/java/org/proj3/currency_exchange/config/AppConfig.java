package org.proj3.currency_exchange.config;

import org.proj3.currency_exchange.dao.impl.CurrencyDao;
import org.proj3.currency_exchange.dao.impl.ExchangeRateDao;
import org.proj3.currency_exchange.mapper.CurrencyMapper;
import org.proj3.currency_exchange.mapper.ExchangeRateMapper;
import org.proj3.currency_exchange.service.CurrencyService;
import org.proj3.currency_exchange.service.ExchangeRateService;
import org.proj3.currency_exchange.service.ExchangeService;

import javax.sql.DataSource;

public class AppConfig {
    private static final DataSource DATA_SOURCE = DatabaseConfig.getDataSource();

    private static final CurrencyDao CURRENCY_DAO = CurrencyDao.createInstance(DATA_SOURCE);
    private static final ExchangeRateDao EXCHANGE_RATE_DAO = ExchangeRateDao.createInstance(DATA_SOURCE);

    private static final CurrencyMapper CURRENCY_MAPPER = CurrencyMapper.getInstance();
    private static final ExchangeRateMapper EXCHANGE_RATE_MAPPER = ExchangeRateMapper.getInstance();

    private static final CurrencyService CURRENCY_SERVICE =
            CurrencyService.createInstance(CURRENCY_DAO,CURRENCY_MAPPER);

    private static final ExchangeRateService EXCHANGE_RATE_SERVICE =
            ExchangeRateService.createInstance(EXCHANGE_RATE_DAO, CURRENCY_DAO, EXCHANGE_RATE_MAPPER);

    private static final ExchangeService EXCHANGE_SERVICE =
            ExchangeService.createInstance(EXCHANGE_RATE_DAO, CURRENCY_MAPPER);

    public static CurrencyService getCurrencyService() {
        return CURRENCY_SERVICE;
    }

    public static ExchangeRateService getExchangeRateService() {
        return EXCHANGE_RATE_SERVICE;
    }

    public static ExchangeService getExchangeService() {
        return EXCHANGE_SERVICE;
    }

}
