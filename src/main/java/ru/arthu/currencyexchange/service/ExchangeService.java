package ru.arthu.currencyexchange.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import ru.arthu.currencyexchange.dao.ExchangeRateDao;
import ru.arthu.currencyexchange.dto.ExchangeDto;
import ru.arthu.currencyexchange.exceptions.CurrencyCodeNotFoundException;

public class ExchangeService {
    private static final ExchangeService INSTANCE = new ExchangeService();
    private final ExchangeRateDao exchangeRateDao = ExchangeRateDao.getInstance();
    private final CurrencyService currencyService = CurrencyService.getInstance();


    public ExchangeDto exchange(String baseCurrency, String targetCurrency, BigDecimal amount)
            throws CurrencyCodeNotFoundException {

        var baseCurrencyDto = currencyService.getCurrency(baseCurrency);
        var targetCurrencyDto = currencyService.getCurrency(targetCurrency);

        var rate = exchangeRateDao.getExchangeRate(
                baseCurrencyDto.id(), targetCurrencyDto.id());
        return new ExchangeDto(
                baseCurrencyDto, targetCurrencyDto, rate, amount, amount.multiply(
                        rate).setScale(2, RoundingMode.HALF_UP));
    }

    public static ExchangeService getInstance() {
        return INSTANCE;
    }

    private ExchangeService(){
    }
}
