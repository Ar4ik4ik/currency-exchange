package ru.arthu.currencyexchange.dao;

public interface SaveDao<E> {
    E save(E entity);
}
