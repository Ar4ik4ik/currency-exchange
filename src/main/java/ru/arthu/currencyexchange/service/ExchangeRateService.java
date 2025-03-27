package ru.arthu.currencyexchange.service;

import ru.arthu.currencyexchange.dao.ExchangeRateDao;
import ru.arthu.currencyexchange.dto.ExchangeRateDto;
import ru.arthu.currencyexchange.model.Currency;
import ru.arthu.currencyexchange.model.ExchangeRate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class ExchangeRateService {
    private static final ExchangeRateService INSTANCE = new ExchangeRateService();
    private final ExchangeRateDao exchangeRateDao = ExchangeRateDao.getInstance();

    public List<ExchangeRateDto> getAllExchangeRates() {
        return exchangeRateDao.findAll().stream()
                .map(rate -> new ExchangeRateDto(
                        rate.getBaseCurrency().getId(),
                        rate.getTargetCurrency().getId(),
                        rate.getRate()
                )).toList();
    }

    public Optional<ExchangeRateDto> getExchangeRateById(Long id) {
        return exchangeRateDao.findByKey(id)
                .map(rate -> new ExchangeRateDto(
                rate.getBaseCurrency().getId(),rate.getTargetCurrency().getId(), rate.getRate()
        ));
    }

    public ExchangeRateDto createExchangeRate(Long baseCurrencyId, Long targetCurrencyId, BigDecimal rate) {
        if (rate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Exchange rate must be positive");
        }

        var baseCurrencyOpt = exchangeRateDao.findByKey(baseCurrencyId);
        var targetCurrencyOpt = exchangeRateDao.findByKey(targetCurrencyId);

        if (baseCurrencyOpt.isEmpty() || targetCurrencyOpt.isEmpty()) {
            throw new IllegalArgumentException("Base or target currency not found");
        }

        var exchangeRate = new ExchangeRate(null, baseCurrencyOpt.get().getBaseCurrency(), targetCurrencyOpt.get().getTargetCurrency(), rate);
        var savedExchangeRate = exchangeRateDao.save(exchangeRate);

        return new ExchangeRateDto(
                savedExchangeRate.getBaseCurrency().getId(),
                savedExchangeRate.getTargetCurrency().getId(),
                savedExchangeRate.getRate()
        );
    }


    public boolean updateExchangeRate(Long id, ExchangeRateDto exchangeRateDto) {
        var existingExchangeRate = exchangeRateDao.findByKey(id);
        if (existingExchangeRate.isEmpty()) {
            return false;
        }

        var baseCurrencyOpt = exchangeRateDao.findByKey(exchangeRateDto.getBaseCurrencyId());
        var targetCurrencyOpt = exchangeRateDao.findByKey(exchangeRateDto.getTargetCurrencyId());

        if (baseCurrencyOpt.isEmpty() || targetCurrencyOpt.isEmpty()) {
            return false;
        }

        var updatedExchangeRate = new ExchangeRate(
                id,
                baseCurrencyOpt.get().getBaseCurrency(),
                targetCurrencyOpt.get().getTargetCurrency(),
                exchangeRateDto.getRate()
        );

        return exchangeRateDao.update(updatedExchangeRate);
    }

    public boolean deleteExchangeRate(Long id) {
        if (exchangeRateDao.findByKey(id).isEmpty()) {
            return false;
        }
        return exchangeRateDao.delete(id);
    }

    public static ExchangeRateService getInstance() {
        return INSTANCE;
    }

    private ExchangeRateService() {
    }
}
