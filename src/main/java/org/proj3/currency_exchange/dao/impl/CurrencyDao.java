package org.proj3.currency_exchange.dao.impl;

import org.proj3.currency_exchange.entity.CurrencyEntity;
import org.proj3.currency_exchange.exception.DaoException;
import org.proj3.currency_exchange.exception.EntityExistsException;
import org.sqlite.SQLiteErrorCode;
import org.sqlite.SQLiteException;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Optional;

public class CurrencyDao extends AbstractDao<CurrencyEntity, String> {
    private static final String FINDING_ALL_ERROR = "Error while fetching currencies";
    private static final String FINDING_ERROR = "Error finding currency by code";
    private static final String NO_ROWS_AFFECTED = "Saving currency failed, no rows affected.";
    private static final String RETRIEVING_ID_FAILED = "Failed to retrieve generated ID.";
    private static final String ERROR_SAVING_CURRENCY = "Error saving currency to the database";

    private static final String FIND_ALL_SQL = """
                SELECT id, code, full_name, sign
                FROM currencies
                """;

    private static final String SAVE_SQL = """
            INSERT INTO currencies (code, full_name, sign)
            VALUES (?, ?, ?)
            """;

    private CurrencyDao(DataSource dataSource) {
        super(dataSource);
    }

    public static CurrencyDao createInstance(DataSource dataSource) {
        return new CurrencyDao(dataSource);
    }

    public Optional<CurrencyEntity> find(String code) {
        Optional<CurrencyEntity> currency = Optional.empty();
        String sql = """
                SELECT id, code, full_name, sign
                FROM currencies WHERE code = ?
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, code);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                currency = Optional.of(mapRowToEntity(resultSet));
            }

        } catch (SQLException e) {
            throw new DaoException(FINDING_ERROR, e);
        }
        return currency;
    }

    public CurrencyEntity save(CurrencyEntity currency) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SAVE_SQL)) {

            preparedStatement.setString(1, currency.getCode());
            preparedStatement.setString(2, currency.getFullName());
            preparedStatement.setString(3, currency.getSign());

            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows == 0) {
                throw new DaoException(NO_ROWS_AFFECTED);
            }

            String lastIdSQL = "SELECT last_insert_rowid()";

            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(lastIdSQL)) {
                if (resultSet.next()) {
                    int generatedId = resultSet.getInt(1);
                    currency.setId(generatedId);
                } else {
                    throw new DaoException(RETRIEVING_ID_FAILED);
                }
            }
            return currency;

        } catch (SQLException e) {
            if (e instanceof SQLiteException exception) {
                if (exception.getResultCode().code == SQLiteErrorCode.SQLITE_CONSTRAINT_UNIQUE.code) {
                    throw new EntityExistsException("Currency with code '" + currency.getCode() + "' already exists");
                }
            }
            throw new DaoException(ERROR_SAVING_CURRENCY, e);
        }
    }

    protected CurrencyEntity mapRowToEntity(ResultSet resultSet) throws SQLException {
        CurrencyEntity currency = new CurrencyEntity(
                resultSet.getString("code"),
                resultSet.getString("full_name"),
                resultSet.getString("sign"));
        currency.setId(resultSet.getInt("id"));
        return currency;
    }

    protected String getFindAllQuery() {
        return FIND_ALL_SQL;
    }

    protected String getFindAllErrorMessage() {
        return FINDING_ALL_ERROR;
    }
}
