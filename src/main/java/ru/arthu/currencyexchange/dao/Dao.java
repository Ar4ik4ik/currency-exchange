package ru.arthu.currencyexchange.dao;

import ru.arthu.currencyexchange.exceptions.ExchangeAlreadyExistException;

import java.sql.SQLException;
import java.util.List;

public interface Dao <E>{

    List<E> findAll();
    E save(E entity) throws SQLException, ExchangeAlreadyExistException;
}
