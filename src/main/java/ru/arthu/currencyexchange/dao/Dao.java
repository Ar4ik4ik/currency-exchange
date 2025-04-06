package ru.arthu.currencyexchange.dao;

import ru.arthu.currencyexchange.exceptions.ExchangeAlreadyExistException;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface Dao <K, E>{

    boolean update(E entity);
    List<E> findAll();
    Optional<E> findByKey(K key);
    E save(E entity) throws SQLException, ExchangeAlreadyExistException;
    boolean delete(K key);
}
