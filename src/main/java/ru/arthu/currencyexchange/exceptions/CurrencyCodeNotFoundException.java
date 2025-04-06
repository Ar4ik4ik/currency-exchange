package ru.arthu.currencyexchange.exceptions;

public class CurrencyCodeNotFoundException extends Exception {
    public CurrencyCodeNotFoundException() {
        super("Код валюты не найден");
    }
}
