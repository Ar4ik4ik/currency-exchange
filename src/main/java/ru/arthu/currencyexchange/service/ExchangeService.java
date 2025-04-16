package ru.arthu.currencyexchange.service;

import java.math.RoundingMode;

import ru.arthu.currencyexchange.dao.ExchangeRateDao;
import ru.arthu.currencyexchange.dto.ExchangeDto;
import ru.arthu.currencyexchange.dto.ExchangeRequestDto;
import ru.arthu.currencyexchange.exceptions.CurrencyCodeNotFoundException;

public class ExchangeService {
    private static final ExchangeService INSTANCE = new ExchangeService();
    private final ExchangeRateDao exchangeRateDao = ExchangeRateDao.getInstance(); // TODO: RESOLVE DIP
    private final CurrencyService currencyService = CurrencyService.getInstance();


    public ExchangeDto exchange(ExchangeRequestDto requestDto)
            throws CurrencyCodeNotFoundException {

        var baseCurrencyDto = currencyService.getCurrency(requestDto.baseCurrencyCode());
        var targetCurrencyDto = currencyService.getCurrency(requestDto.targetCurrencyCode());

        var rate = exchangeRateDao.getExchangeRate(
                baseCurrencyDto.id(), targetCurrencyDto.id());
        return new ExchangeDto(
                baseCurrencyDto, targetCurrencyDto, rate, requestDto.amount(),
                requestDto.amount().multiply(rate)
                        .setScale(2, RoundingMode.HALF_UP));
    }

    public static ExchangeService getInstance() {
        return INSTANCE;
    }

    private ExchangeService() {
    }
}
