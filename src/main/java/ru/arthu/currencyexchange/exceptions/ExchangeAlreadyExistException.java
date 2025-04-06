package ru.arthu.currencyexchange.exceptions;

public class ExchangeAlreadyExistException extends Exception {
    public ExchangeAlreadyExistException(Exception message) {
        super(message);
    }
}
