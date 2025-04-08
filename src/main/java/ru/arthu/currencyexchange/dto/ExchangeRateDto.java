package ru.arthu.currencyexchange.dto;

import ru.arthu.currencyexchange.model.ExchangeRate;

import java.math.BigDecimal;

public record ExchangeRateDto(Long id, CurrencyDto baseCurrency, CurrencyDto targetCurrency, BigDecimal rate) {

    public static ExchangeRateDto fromModel(ExchangeRate exchangeRateModel) {
        return new ExchangeRateDto(
                exchangeRateModel.id(),
                CurrencyDto.fromModel(exchangeRateModel.baseCurrency()),
                CurrencyDto.fromModel(exchangeRateModel.targetCurrency()),
                exchangeRateModel.exchangeRate()
                );
    }
}
