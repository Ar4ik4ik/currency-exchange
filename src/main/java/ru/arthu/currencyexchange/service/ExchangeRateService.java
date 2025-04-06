package ru.arthu.currencyexchange.service;

import ru.arthu.currencyexchange.dao.ExchangeRateDao;
import ru.arthu.currencyexchange.dto.CurrencyDto;
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
                .map(rate -> new ExchangeRateDto(
                        rate.getId(),
                        currencyService.getCurrency(rate.getBaseCurrency().getId()),
                        currencyService.getCurrency(rate.getTargetCurrency().getId()),
                        rate.getRate()
                )).toList();
    }

    public ExchangeRateDto createExchangeRate(String baseCurrencyCode, String targetCurrencyCode, BigDecimal rate)
            throws ExchangeAlreadyExistException {

        if (rate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Exchange rate must be positive");
        }

        var baseCurrencyDto = currencyService.getCurrency(baseCurrencyCode);
        var targetCurrencyDto = currencyService.getCurrency(targetCurrencyCode);

        var exchangeRate = new ExchangeRate(baseCurrencyDto, targetCurrencyDto, rate);
        ExchangeRate savedExchangeRate;
        try {
            savedExchangeRate = exchangeRateDao.save(exchangeRate);
        } catch (UniqueConstraintViolationException e) {
            throw new ExchangeAlreadyExistException(e);
        }
        return new ExchangeRateDto(savedExchangeRate.getId(),
                    baseCurrencyDto,
                    targetCurrencyDto,
                    savedExchangeRate.getRate());

    }


    public ExchangeRateDto updateExchangeRate(
            String baseCurrencyCode, String targetCurrencyCode, BigDecimal rate) {

        if (rate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Exchange rate must be positive");
        }

        var baseCurrency = currencyService.getCurrency(baseCurrencyCode);
        var targetCurrency = currencyService.getCurrency(targetCurrencyCode);
        var existingExchangeRate = exchangeRateDao.findByKey(
                baseCurrencyCode, targetCurrencyCode);
        if (existingExchangeRate.isEmpty()) {
            throw new ExchangeRateNotFoundException("Валютная пара не найдена");
        } else {
            var updatedExchangeRate = new ExchangeRate(
                    existingExchangeRate.get().getId(),
                    new Currency(baseCurrency),
                    new Currency(targetCurrency),
                    rate
            );
            exchangeRateDao.update(updatedExchangeRate);
            return new ExchangeRateDto(
                    updatedExchangeRate.getId(),
                    CurrencyDto.buildDto(updatedExchangeRate.getBaseCurrency()),
                    CurrencyDto.buildDto(updatedExchangeRate.getTargetCurrency()),
                    updatedExchangeRate.getRate()
            );
        }
    }

    public ExchangeRateDto findExchangeRateByKey(String baseCode, String targetCode) {

        var exchangeRate = exchangeRateDao.findByKey(
                baseCode, targetCode);
        if (exchangeRate.isEmpty()) {
            throw new ExchangeRateNotFoundException("");
        } else {
            var foundExchangeRate = exchangeRate.get();
            return new ExchangeRateDto(
                    foundExchangeRate.getId(),
                    CurrencyDto.buildDto(foundExchangeRate.getBaseCurrency()),
                    CurrencyDto.buildDto(foundExchangeRate.getTargetCurrency()),
                    foundExchangeRate.getRate()
            );
        }
    }

    public static ExchangeRateService getInstance() {
        return INSTANCE;
    }

    private ExchangeRateService() {
    }

}
