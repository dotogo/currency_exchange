package org.proj3.currency_exchange.dao;

import org.proj3.currency_exchange.entity.CurrencyEntity;
import org.proj3.currency_exchange.exception.DaoException;
import org.proj3.currency_exchange.util.DatabaseConfig;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CurrencyDao {
    private static final CurrencyDao instance = new CurrencyDao();
    private static final DataSource dataSource = DatabaseConfig.getDataSource();

    private static final String SAVE_SQL = """
            INSERT INTO currencies (code, full_name, sign)
            VALUES (?, ?, ?)
            """;

    private CurrencyDao() {
    }

    public static CurrencyDao getInstance() {
        return CurrencyDao.instance;
    }

    public List<CurrencyEntity> findAll() throws DaoException {
        List<CurrencyEntity> currencies = new ArrayList<>();
        String sql = """
                SELECT id, code, full_name, sign
                FROM currencies
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                currencies.add(mapRowToEntity(resultSet));
            }

        } catch (SQLException e) {
            throw new DaoException("Error while fetching currencies", e);
        }
        return currencies;
    }

    public Optional<CurrencyEntity> findByCode(String code) {
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
            throw new RuntimeException(e);
        }
        return currency;
    }

    public CurrencyEntity save(CurrencyEntity currency) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SAVE_SQL, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1, currency.getCode());
            preparedStatement.setString(2, currency.getFullName());
            preparedStatement.setString(3, currency.getSign());

            System.out.println();

            preparedStatement.executeUpdate();

            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            System.out.println(generatedKeys);
            if (generatedKeys.next()) {
                int generatedId = generatedKeys.getInt("id");
                currency.setId(generatedId);
            }
            return currency;

        } catch (SQLException e) {
            throw new DaoException("Error saving currency", e);
        }
    }

    private CurrencyEntity mapRowToEntity(ResultSet resultSet) throws SQLException {
        CurrencyEntity currency = new CurrencyEntity(
                resultSet.getString("code"),
                resultSet.getString("full_name"),
                resultSet.getString("sign"));
        currency.setId(resultSet.getInt("id"));
        return currency;
    }
}
