package ru.arthu.currencyexchange.service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Optional;

import ru.arthu.currencyexchange.dao.ExchangeRateDao;
import ru.arthu.currencyexchange.dto.CurrencyDto;
import ru.arthu.currencyexchange.dto.ExchangeDto;
import ru.arthu.currencyexchange.exceptions.CurrencyCodeNotFoundException;

public class ExchangeService {
    private static final ExchangeService INSTANCE = new ExchangeService();
    private final ExchangeRateDao exchangeRateDao = ExchangeRateDao.getInstance();
    private final CurrencyService currencyService = CurrencyService.getInstance();


    public ExchangeDto exchange(String baseCurrency, String targetCurrency, BigDecimal amount) throws CurrencyCodeNotFoundException, SQLException {

        Optional<CurrencyDto> baseOpt = currencyService.getCurrencyByCode(baseCurrency);
        Optional<CurrencyDto> targetOpt = currencyService.getCurrencyByCode(targetCurrency);

        if (baseOpt.isEmpty() || targetOpt.isEmpty()) {
            throw new CurrencyCodeNotFoundException("Currency code not found");
        }

        CurrencyDto base = baseOpt.get();
        CurrencyDto target = targetOpt.get();

        BigDecimal rate = exchangeRateDao.getExchangeRate(base.id(), target.id());
        return new ExchangeDto(base, target, rate, amount, amount.multiply(rate));

    }

    public static ExchangeService getInstance() {
        return INSTANCE;
    }

    private ExchangeService(){
    }
}
