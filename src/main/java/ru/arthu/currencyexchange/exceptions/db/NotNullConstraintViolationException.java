package ru.arthu.currencyexchange.exceptions.db;

public class NotNullConstraintViolationException extends RuntimeException {
    public NotNullConstraintViolationException(String message) {
        super(message);
    }
}
