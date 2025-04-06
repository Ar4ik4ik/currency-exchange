package ru.arthu.currencyexchange.exceptions;

public class CurrencyAlreadyExist extends RuntimeException {
    public CurrencyAlreadyExist() {
        super("Такая валюта уже существует");
    }
}
