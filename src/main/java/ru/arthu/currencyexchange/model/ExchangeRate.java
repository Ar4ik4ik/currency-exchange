package ru.arthu.currencyexchange.model;

import java.math.BigDecimal;
import java.util.Objects;

public class ExchangeRate {

    private Long id;
    private Currency baseCurrency;
    private Currency targetCurrency;
    private BigDecimal rate;

    public ExchangeRate(Long id, Currency baseCurrency, Currency targetCurrency, BigDecimal rate) {
        this.id = id;
        this.baseCurrency = baseCurrency;
        this.targetCurrency = targetCurrency;
        this.rate = rate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Currency getBaseCurrency() {
        return baseCurrency;
    }

    public void setBaseCurrency(Currency baseCurrency) {
        this.baseCurrency = baseCurrency;
    }

    public Currency getTargetCurrency() {
        return targetCurrency;
    }

    public void setTargetCurrency(Currency targetCurrency) {
        this.targetCurrency = targetCurrency;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ExchangeRate that = (ExchangeRate) o;
        return Objects.equals(id, that.id) && Objects.equals(baseCurrency, that.baseCurrency) && Objects.equals(targetCurrency, that.targetCurrency) && Objects.equals(rate, that.rate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, baseCurrency, targetCurrency, rate);
    }

    @Override
    public String toString() {
        return "ExchangeRate{" +
                "id=" + id +
                ", baseCurrency=" + baseCurrency +
                ", targetCurrency=" + targetCurrency +
                ", rate=" + rate +
                '}';
    }
}
