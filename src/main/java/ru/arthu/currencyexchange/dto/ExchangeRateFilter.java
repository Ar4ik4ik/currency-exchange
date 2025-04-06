package ru.arthu.currencyexchange.dto;

import java.math.BigDecimal;

public record ExchangeRateFilter(Long baseCurrencyId,
                                 Long targetCurrencyId,
                                 BigDecimal rate,
                                 Integer limit,
                                 Integer offset) {
}
