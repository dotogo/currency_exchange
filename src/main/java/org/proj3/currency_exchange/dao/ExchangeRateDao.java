package org.proj3.currency_exchange.dao;

import org.proj3.currency_exchange.entity.CurrencyEntity;
import org.proj3.currency_exchange.entity.ExchangeRateEntity;
import org.proj3.currency_exchange.exception.DaoException;
import org.proj3.currency_exchange.util.DatabaseConfig;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExchangeRateDao {
    private static final  ExchangeRateDao instance = new ExchangeRateDao();
    private static final DataSource dataSource = DatabaseConfig.getDataSource();

    private static final String BASE_QUERY = """
            SELECT
                er.id AS exchange_rate_id,
                er.rate,
                bc.id AS base_currency_id,
                bc.code AS base_currency_code,
                bc.full_name AS base_currency_name,
                bc.sign AS base_currency_sign,
                tc.id AS target_currency_id,
                tc.code AS target_currency_code,
                tc.full_name AS target_currency_name,
                tc.sign AS target_currency_sign
            FROM exchangeRates er
            JOIN currencies bc ON er.base_currency_id = bc.id
            JOIN currencies tc ON er.target_currency_id = tc.id
            """;

    private static final String FIND_BY_CODE_WHERE = """
            WHERE bc.code = ? AND tc.code = ?
            """;

    private static final String SAVE_SQL = """
            INSERT INTO exchangeRates (base_currency_id, target_currency_id, rate)
            VALUES (?, ?, ?)
            """;
    private static final String FINDING_ALL_ERROR = "Error while finding exchange rates.";
    private static final String FINDING_BY_CODE_PAIR_ERROR = "Error finding exchange rate by code pair.";
//    private static final String SAVING_ERROR = "Error saving exchange rate.";

    private ExchangeRateDao() {
    }

    public static ExchangeRateDao getInstance() {
        return instance;
    }

    public List<ExchangeRateEntity> findAll() {
        List<ExchangeRateEntity> rateEntities = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(BASE_QUERY)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                rateEntities.add(mapRowToEntity(resultSet));
            }
        } catch (SQLException e) {
            throw new DaoException(FINDING_ALL_ERROR, e);
        }
        return rateEntities;
    }


    public Optional<ExchangeRateEntity> findByCode(String currencyPair) {
        String baseCurrencyCode = currencyPair.substring(0, 3);
        String targetCurrencyCode = currencyPair.substring(3);
        String sql = BASE_QUERY + FIND_BY_CODE_WHERE;

        Optional<ExchangeRateEntity> exchangeRate = Optional.empty();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, baseCurrencyCode);
            preparedStatement.setString(2, targetCurrencyCode);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                exchangeRate = Optional.of(mapRowToEntity(resultSet));
            }

        } catch (SQLException e) {
            throw new DaoException(FINDING_BY_CODE_PAIR_ERROR, e);
        }
        return exchangeRate;
    }

    public ExchangeRateEntity save(ExchangeRateEntity exchangeRate) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SAVE_SQL)) {

            preparedStatement.setInt(1, exchangeRate.getBaseCurrency().getId());
            preparedStatement.setInt(2, exchangeRate.getTargetCurrency().getId());
            preparedStatement.setBigDecimal(3, exchangeRate.getRate());

            int affectedRows = preparedStatement.executeUpdate();

            if (affectedRows == 0) {
                throw new DaoException("Saving exchange rate failed, no rows affected.");
            }

            String lastIdSQL = "SELECT last_insert_rowid()";

            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(lastIdSQL)) {

                if (resultSet.next()) {
                    exchangeRate.setId(resultSet.getInt(1));
                } else {
                    throw new DaoException("Failed to retrieve generated ID.");
                }
            }
            return exchangeRate;
        } catch (SQLException e) {
            throw new DaoException("Error saving exchange rate.", e);
        }
    }


    private ExchangeRateEntity mapRowToEntity(ResultSet resultSet) throws SQLException {
        ExchangeRateEntity rateEntity = new ExchangeRateEntity();
        rateEntity.setId(resultSet.getInt("exchange_rate_id"));
        rateEntity.setRate(resultSet.getBigDecimal("rate"));

        CurrencyEntity baseCurrency = new CurrencyEntity(
                resultSet.getString("base_currency_code"),
                resultSet.getString("base_currency_name"),
                resultSet.getString("base_currency_sign")
        );
        baseCurrency.setId(resultSet.getInt("base_currency_id"));

        CurrencyEntity targetCurrency = new CurrencyEntity(
                resultSet.getString("target_currency_code"),
                resultSet.getString("target_currency_name"),
                resultSet.getString("target_currency_sign")
        );
        targetCurrency.setId(resultSet.getInt("target_currency_id"));

        rateEntity.setBaseCurrency(baseCurrency);
        rateEntity.setTargetCurrency(targetCurrency);

        return rateEntity;
    }

}
