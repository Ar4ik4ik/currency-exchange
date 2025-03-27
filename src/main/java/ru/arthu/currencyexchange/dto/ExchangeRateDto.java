package ru.arthu.currencyexchange.dto;

import java.math.BigDecimal;

public class ExchangeRateDto {

    private final Long baseCurrencyId;
    private final Long targetCurrencyId;
    private final BigDecimal rate;

    public ExchangeRateDto(Long baseCurrencyId, Long targetCurrencyId, BigDecimal rate) {
        this.baseCurrencyId = baseCurrencyId;
        this.targetCurrencyId = targetCurrencyId;
        this.rate = rate;
    }

    public Long getBaseCurrencyId() {
        return baseCurrencyId;
    }

    public Long getTargetCurrencyId() {
        return targetCurrencyId;
    }

    public BigDecimal getRate() {
        return rate;
    }
}
