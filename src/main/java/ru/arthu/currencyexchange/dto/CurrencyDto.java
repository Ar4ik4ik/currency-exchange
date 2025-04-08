package ru.arthu.currencyexchange.dto;

import ru.arthu.currencyexchange.model.Currency;

public record CurrencyDto(Long id, String code, String name, String sign) {

    public static CurrencyDto fromModel(Currency currencyModel) {
        return new CurrencyDto(
                currencyModel.id(),
                currencyModel.code(),
                currencyModel.fullName(),
                currencyModel.sign()
        );
    }
}

