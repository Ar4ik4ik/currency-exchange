package ru.arthu.currencyexchange.utils.mappers;

import ru.arthu.currencyexchange.dto.CurrencyDto;
import ru.arthu.currencyexchange.dto.ExchangeRateDto;
import ru.arthu.currencyexchange.dto.ExchangeRateRequestDto;
import ru.arthu.currencyexchange.model.ExchangeRate;

import java.math.BigDecimal;

public class ExchangeRateMapper {
    public static ExchangeRateRequestDto createRequestDto(String baseCurrencyCode,
                                                          String targetCurrencyCode,
                                                          BigDecimal rate) {
        return new ExchangeRateRequestDto(baseCurrencyCode, targetCurrencyCode, rate);
    }

    public static ExchangeRateDto fromModel(ExchangeRate exchangeRateModel) {
        return new ExchangeRateDto(
                exchangeRateModel.id(),
                CurrencyMapper.fromModel(exchangeRateModel.baseCurrency()),
                CurrencyMapper.fromModel(exchangeRateModel.targetCurrency()),
                exchangeRateModel.exchangeRate()
        );
    }

    public static ExchangeRate fromDto(CurrencyDto baseCurrencyDto,
                                       CurrencyDto targetCurrencyDto,
                                       BigDecimal exchangeRate) {
        return new ExchangeRate(
                null, CurrencyMapper.fromDto(baseCurrencyDto),
                CurrencyMapper.fromDto(targetCurrencyDto),
                exchangeRate
        );
    }

    public static ExchangeRate fromDto(CurrencyDto base, CurrencyDto target, BigDecimal rate, Long id) {
        return new ExchangeRate(id, CurrencyMapper.fromDto(base), CurrencyMapper.fromDto(target), rate);
    }

}
