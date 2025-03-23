package org.proj3.currency_exchange.dao.impl;

import org.proj3.currency_exchange.dao.Dao;
import org.proj3.currency_exchange.exception.DaoException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class AbstractDao <T, P> implements Dao<T, P> {
    protected final DataSource dataSource;

    protected AbstractDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<T> findAll() {
        return findAll(getFindAllQuery(), getFindAllErrorMessage());
    }

    public abstract Optional<T> find(P param);

    public abstract T save(T entity);

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
    protected abstract String getFindAllQuery();
    protected abstract String getFindAllErrorMessage();
}
