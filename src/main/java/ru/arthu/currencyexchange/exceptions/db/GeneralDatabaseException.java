package ru.arthu.currencyexchange.exceptions.db;

public class GeneralDatabaseException extends RuntimeException {
    public GeneralDatabaseException(String message) {
        super(message);
    }
}
