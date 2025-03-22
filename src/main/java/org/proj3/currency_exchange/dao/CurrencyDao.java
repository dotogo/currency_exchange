package org.proj3.currency_exchange.dao;

import org.proj3.currency_exchange.entity.CurrencyEntity;
import org.proj3.currency_exchange.exception.DaoException;

import java.sql.*;
import java.util.List;
import java.util.Optional;

public class CurrencyDao extends AbstractDao<CurrencyEntity> {
    private static final String FINDING_ALL_ERROR = "Error while fetching currencies";
    private static final String FINDING_ERROR = "Error finding currency by code";
    private static final String NO_ROWS_AFFECTED = "Saving currency failed, no rows affected.";
    private static final String RETRIEVING_ID_FAILED = "Failed to retrieve generated ID.";
    private static final String ERROR_SAVING_CURRENCY = "Error saving currency";

    private static final CurrencyDao instance = new CurrencyDao();

    private static final String FIND_ALL_SQL = """
                SELECT id, code, full_name, sign
                FROM currencies
                """;

    private static final String SAVE_SQL = """
            INSERT INTO currencies (code, full_name, sign)
            VALUES (?, ?, ?)
            """;

    private CurrencyDao() {
    }

    public static CurrencyDao getInstance() {
        return CurrencyDao.instance;
    }

    public List<CurrencyEntity> findAll() {
        return findAll(FIND_ALL_SQL, FINDING_ALL_ERROR);
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
             PreparedStatement preparedStatement = connection.prepareStatement(SAVE_SQL, Statement.RETURN_GENERATED_KEYS)) {

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
}
