package ru.arthu.currencyexchange.model;

import java.math.BigDecimal;

public record ExchangeRate(Long id, Currency baseCurrency, Currency targetCurrency, BigDecimal exchangeRate) {
}
