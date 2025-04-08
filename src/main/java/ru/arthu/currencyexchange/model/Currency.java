package ru.arthu.currencyexchange.model;

import ru.arthu.currencyexchange.dto.CurrencyDto;

public record Currency(Long id, String code, String fullName, String sign) {

    public static Currency fromDto(CurrencyDto currencyDto) {
        return new Currency(
                currencyDto.id(),
                currencyDto.code(),
                currencyDto.name(),
                currencyDto.sign()
        );
    }
}
