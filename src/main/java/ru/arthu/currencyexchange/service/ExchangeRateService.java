package ru.arthu.currencyexchange.service;

import ru.arthu.currencyexchange.dao.ExchangeRateDao;
import ru.arthu.currencyexchange.dto.ExchangeRateDto;
import ru.arthu.currencyexchange.exceptions.ExchangeAlreadyExistException;
import ru.arthu.currencyexchange.exceptions.ExchangeRateNotFoundException;
import ru.arthu.currencyexchange.exceptions.db.UniqueConstraintViolationException;
import ru.arthu.currencyexchange.model.Currency;
import ru.arthu.currencyexchange.model.ExchangeRate;

import java.math.BigDecimal;
import java.util.List;

public class ExchangeRateService {
    private static final ExchangeRateService INSTANCE = new ExchangeRateService();
    private final ExchangeRateDao exchangeRateDao = ExchangeRateDao.getInstance();
    private final CurrencyService currencyService = CurrencyService.getInstance();

    public List<ExchangeRateDto> getAllExchangeRates() {
        return exchangeRateDao.findAll().stream()
                .map(ExchangeRateDto::fromModel).toList();
    }

    public ExchangeRateDto createExchangeRate(String baseCurrencyCode, String targetCurrencyCode, BigDecimal rate)
            throws ExchangeAlreadyExistException {

        if (rate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Exchange rate must be positive");
        }

        var baseCurrencyDto = currencyService.getCurrency(baseCurrencyCode);
        var targetCurrencyDto = currencyService.getCurrency(targetCurrencyCode);

        var exchangeRate = ExchangeRate.fromDto(baseCurrencyDto, targetCurrencyDto, rate);
        try {
            return ExchangeRateDto.fromModel(
                    exchangeRateDao.save(exchangeRate)
            );
        } catch (UniqueConstraintViolationException e) {
            throw new ExchangeAlreadyExistException(e);
        }
    }


    public ExchangeRateDto updateExchangeRate(
            String baseCurrencyCode, String targetCurrencyCode, BigDecimal exchangeRate) {

        if (exchangeRate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Exchange exchangeRate must be positive");
        }

        var baseCurrency = currencyService.getCurrency(baseCurrencyCode);
        var targetCurrency = currencyService.getCurrency(targetCurrencyCode);
        var existingExchangeRate = exchangeRateDao.findByKey(
                baseCurrencyCode, targetCurrencyCode);
        if (existingExchangeRate.isEmpty()) {
            throw new ExchangeRateNotFoundException("Валютная пара не найдена");
        } else {
            var updatedExchangeRate = new ExchangeRate(
                    existingExchangeRate.get().id(),
                    Currency.fromDto(baseCurrency),
                    Currency.fromDto(targetCurrency),
                    exchangeRate
            );
            exchangeRateDao.update(updatedExchangeRate);
            return ExchangeRateDto.fromModel(updatedExchangeRate);
        }
    }

    public ExchangeRateDto findExchangeRateByKey(String baseCode, String targetCode) {

        var exchangeRate = exchangeRateDao.findByKey(
                baseCode, targetCode);
        if (exchangeRate.isEmpty()) {
            throw new ExchangeRateNotFoundException("");
        } else {
            var foundExchangeRate = exchangeRate.get();
            return ExchangeRateDto.fromModel(foundExchangeRate);
        }
    }

    public static ExchangeRateService getInstance() {
        return INSTANCE;
    }

    private ExchangeRateService() {
    }

}
