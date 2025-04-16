package ru.arthu.currencyexchange.dao;

public interface Dao<E, K> extends ReadOnlyDao<E, K>, SaveDao<E> {}
