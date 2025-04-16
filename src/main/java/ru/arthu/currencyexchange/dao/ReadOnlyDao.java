package ru.arthu.currencyexchange.dao;

import java.util.List;
import java.util.Optional;

public interface ReadOnlyDao<E, K> {
    List<E> findAll();
    Optional<E> findByKey(K key);
}

