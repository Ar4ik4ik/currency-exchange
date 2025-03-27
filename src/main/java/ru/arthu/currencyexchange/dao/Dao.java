package ru.arthu.currencyexchange.dao;

import java.util.List;
import java.util.Optional;

public interface Dao <K, E>{

    boolean update(E entity);
    List<E> findAll();
    Optional<E> findByKey(K key);
    E save(E entity);
    boolean delete(K key);
}
