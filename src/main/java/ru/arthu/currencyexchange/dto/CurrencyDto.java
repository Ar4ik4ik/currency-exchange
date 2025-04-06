package ru.arthu.currencyexchange.dto;

import ru.arthu.currencyexchange.model.Currency;

public record CurrencyDto(Long id, String code, String name, String sign) {

    public static CurrencyDto buildDto(Currency currency) {
        return new CurrencyDto(
            currency.getId(),
            currency.getCode(),
            currency.getFullName(),
            currency.getSign()
        );
    }
}
