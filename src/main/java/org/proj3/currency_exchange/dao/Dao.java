package org.proj3.currency_exchange.dao;

import java.util.List;
import java.util.Optional;

public interface Dao <T, P>{
    List<T> findAll();
    Optional<T> find(P param);
    T save(T entity);
}
