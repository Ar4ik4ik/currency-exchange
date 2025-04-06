package ru.arthu.currencyexchange.exceptions.db;

public class CheckConstraintViolationException extends RuntimeException {
    public CheckConstraintViolationException(String message) {
        super(message);
    }
}
