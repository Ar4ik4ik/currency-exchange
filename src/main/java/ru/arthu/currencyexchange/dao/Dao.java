package ru.arthu.currencyexchange.dao;

import ru.arthu.currencyexchange.exceptions.ExchangeAlreadyExistException;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface Dao <K, E>{

    List<E> findAll();
    E save(E entity) throws SQLException, ExchangeAlreadyExistException;
}
