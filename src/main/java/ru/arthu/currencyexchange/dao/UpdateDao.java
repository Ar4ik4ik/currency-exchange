package ru.arthu.currencyexchange.dao;

public interface UpdateDao<E> {
    boolean update(E entity);
}
