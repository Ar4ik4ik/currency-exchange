package ru.arthu.currencyexchange.exceptions;

public class CurrencyCodeNotFoundException extends RuntimeException {
    public CurrencyCodeNotFoundException() {
        super("Код валюты не найден");
    }
}
