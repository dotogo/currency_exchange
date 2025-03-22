package org.proj3.currency_exchange.dao;

import org.proj3.currency_exchange.exception.DaoException;
import org.proj3.currency_exchange.util.DatabaseConfig;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractDao <T> {
    protected static final DataSource dataSource = DatabaseConfig.getDataSource();

    protected List<T> findAll(String sql, String errorMessage) {
        List<T> entities = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                entities.add(mapRowToEntity(resultSet));
            }
        } catch (SQLException e) {
            throw new DaoException(errorMessage, e);
        }
        return entities;
    }

    protected abstract T mapRowToEntity(ResultSet resultSet) throws SQLException;
}
