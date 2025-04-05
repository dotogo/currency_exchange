package org.proj3.currency_exchange.dao.impl;

import org.proj3.currency_exchange.entity.CurrencyEntity;
import org.proj3.currency_exchange.entity.ExchangeRateEntity;
import org.proj3.currency_exchange.exception.DaoException;
import org.proj3.currency_exchange.exception.EntityExistsException;
import org.sqlite.SQLiteErrorCode;
import org.sqlite.SQLiteException;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.util.Optional;

public class ExchangeRateDao extends AbstractDao<ExchangeRateEntity, String> {

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

    private static final String FIND_BY_CURRENCY_ID_WHERE = """
            WHERE bc.id = ? AND tc.id = ?
            """;

    private static final String SAVE_SQL = """
            INSERT INTO exchangeRates (base_currency_id, target_currency_id, rate)
            VALUES (?, ?, ?)
            """;

    private static final String UPDATE_SQL = """
            UPDATE exchangeRates
            SET rate = ?
            WHERE base_currency_id = ? AND target_currency_id = ?
            """;

    private static final String FINDING_ALL_ERROR = "Error while finding exchange rates.";
    private static final String FINDING_ERROR = "Error finding exchange rate by code pair.";
    private static final String NO_ROWS_AFFECTED_ERROR = "Saving exchange rate failed, no rows affected.";
    private static final String NO_EXCHANGE_RATE = "There is no exchange rate for the currency pair.";
    private static final String GENERATED_ID_RETRIEVING_ERROR = "Failed to retrieve generated ID.";
    private static final String SAVING_ERROR = "Error saving exchange rate.";
    private static final String UPDATE_ERROR_STATEMENT = "Failed to update exchange rate. PreparedStatement.";
    private static final String UPDATE_ERROR = "Failed to update exchange rate";

    private ExchangeRateDao(DataSource dataSource) {
        super(dataSource);
    }

    public static ExchangeRateDao createInstance(DataSource dataSource) {
        return new ExchangeRateDao(dataSource);
    }

    public Optional<ExchangeRateEntity> find(String currencyPair) {
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
            throw new DaoException(FINDING_ERROR, e);
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
                throw new DaoException(NO_ROWS_AFFECTED_ERROR);
            }

            String lastIdSQL = "SELECT last_insert_rowid()";

            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(lastIdSQL)) {

                if (resultSet.next()) {
                    exchangeRate.setId(resultSet.getInt(1));
                } else {
                    throw new DaoException(GENERATED_ID_RETRIEVING_ERROR);
                }
            }
            return exchangeRate;
        } catch (SQLException e) {
            if (e instanceof SQLiteException exception) {
                if (exception.getResultCode().code == SQLiteErrorCode.SQLITE_CONSTRAINT_UNIQUE.code) {
                    throw new EntityExistsException(
                            "Exchange rate '%s' to '%s' already exists"
                                    .formatted(exchangeRate.getBaseCurrency().getCode(),
                                            exchangeRate.getTargetCurrency().getCode()));
                }
            }
            throw new DaoException(SAVING_ERROR, e);
        }
    }

    public ExchangeRateEntity update(int baseCurrencyId, int targetCurrencyId, BigDecimal exchangeRate) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_SQL)) {

            preparedStatement.setBigDecimal(1, exchangeRate);
            preparedStatement.setInt(2, baseCurrencyId);
            preparedStatement.setInt(3, targetCurrencyId);

            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows == 0) {
                throw new DaoException(NO_EXCHANGE_RATE);
            }

            String selectSql = BASE_QUERY + FIND_BY_CURRENCY_ID_WHERE;

            try (PreparedStatement statement = connection.prepareStatement(selectSql)) {

                statement.setInt(1, baseCurrencyId);
                statement.setInt(2, targetCurrencyId);

                ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    return mapRowToEntity(resultSet);
                } else {
                    throw new DaoException(UPDATE_ERROR_STATEMENT);
                }
            }
        } catch (SQLException e) {
            throw new DaoException(UPDATE_ERROR, e);
        }
    }

//    private void printResultSet(ResultSet resultSet) throws SQLException {
//        ResultSetMetaData metaData = resultSet.getMetaData();
//        int columnCount = metaData.getColumnCount();
//
//        for (int i = 1; i <= columnCount; i++) {
//            System.out.println(metaData.getColumnName(i) + "\t" + resultSet.getObject(i));
//        }
//        System.out.println();
//    }

    protected ExchangeRateEntity mapRowToEntity(ResultSet resultSet) throws SQLException {
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

    protected String getFindAllQuery() {
        return BASE_QUERY;
    }

    protected String getFindAllErrorMessage() {
        return FINDING_ALL_ERROR;
    }

}
