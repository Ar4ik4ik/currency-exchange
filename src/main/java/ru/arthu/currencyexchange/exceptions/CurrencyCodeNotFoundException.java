package ru.arthu.currencyexchange.exceptions;

public class CurrencyCodeNotFoundException extends Exception {
    public CurrencyCodeNotFoundException(String message) {
        super(message);
    }
}
