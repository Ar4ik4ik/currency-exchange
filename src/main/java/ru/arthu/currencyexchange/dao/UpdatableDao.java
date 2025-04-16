package ru.arthu.currencyexchange.dao;

public interface UpdatableDao<E, K> extends Dao<E, K>, UpdateDao<E> {}
