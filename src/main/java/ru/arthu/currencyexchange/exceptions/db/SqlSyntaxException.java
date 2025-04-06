package ru.arthu.currencyexchange.exceptions.db;

public class SqlSyntaxException extends RuntimeException {
    public SqlSyntaxException(String message) {
        super(message);
    }
}
