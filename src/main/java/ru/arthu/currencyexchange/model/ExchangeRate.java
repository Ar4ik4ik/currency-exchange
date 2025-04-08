package ru.arthu.currencyexchange.model;

import ru.arthu.currencyexchange.dto.CurrencyDto;

import java.math.BigDecimal;


public record ExchangeRate(Long id, Currency baseCurrency, Currency targetCurrency, BigDecimal exchangeRate) {

    public static ExchangeRate fromDto(CurrencyDto baseCurrencyDto,
                                       CurrencyDto targetCurrencyDto,
                                       BigDecimal exchangeRate) {
        return new ExchangeRate(
                null, Currency.fromDto(baseCurrencyDto),
                Currency.fromDto(targetCurrencyDto),
                exchangeRate
        );
    }
}
