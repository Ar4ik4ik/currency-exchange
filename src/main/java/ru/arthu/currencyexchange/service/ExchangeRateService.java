package ru.arthu.currencyexchange.service;

import ru.arthu.currencyexchange.dao.ExchangeRateDao;
import ru.arthu.currencyexchange.dto.CurrencyDto;
import ru.arthu.currencyexchange.dto.ExchangeRateDto;
import ru.arthu.currencyexchange.exceptions.CurrencyCodeNotFoundException;
import ru.arthu.currencyexchange.exceptions.ExchangeAlreadyExistException;
import ru.arthu.currencyexchange.model.Currency;
import ru.arthu.currencyexchange.model.ExchangeRate;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ExchangeRateService {
    private static final ExchangeRateService INSTANCE = new ExchangeRateService();
    private final ExchangeRateDao exchangeRateDao = ExchangeRateDao.getInstance();
    private final CurrencyService currencyService = CurrencyService.getInstance();

    public List<ExchangeRateDto> getAllExchangeRates() {
        return exchangeRateDao.findAll().stream()
                .map(rate -> new ExchangeRateDto(
                        rate.getId(),
                        currencyService.getCurrencyById(rate.getBaseCurrency().getId()).get(),
                        currencyService.getCurrencyById(rate.getTargetCurrency().getId()).get(),
                        rate.getRate()
                )).toList();
    }


    public Optional<ExchangeRateDto> getExchangeRateById(Long id) {
        return exchangeRateDao.findByKey(id)
                .map(rate -> new ExchangeRateDto(id,
                        currencyService.getCurrencyById(rate.getBaseCurrency().getId()).get(),
                        currencyService.getCurrencyById(rate.getTargetCurrency().getId()).get(),
                        rate.getRate()
        ));
    }

    public ExchangeRateDto createExchangeRate(String baseCurrencyCode, String targetCurrencyCode, BigDecimal rate)
            throws ExchangeAlreadyExistException, CurrencyCodeNotFoundException {

        if (rate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Exchange rate must be positive");
        }

        var baseCurrencyOpt = currencyService.getCurrencyByCode(baseCurrencyCode);
        var targetCurrencyOpt = currencyService.getCurrencyByCode(targetCurrencyCode);

        if (baseCurrencyOpt.isEmpty() || targetCurrencyOpt.isEmpty()) {
            throw new CurrencyCodeNotFoundException();
        }

        var exchangeRate = new ExchangeRate(baseCurrencyOpt.get(), targetCurrencyOpt.get(), rate);
        var savedExchangeRate = exchangeRateDao.save(exchangeRate);
            return new ExchangeRateDto(savedExchangeRate.getId(),
                    currencyService.getCurrencyById(savedExchangeRate.getBaseCurrency().getId()).get(),
                    currencyService.getCurrencyById(savedExchangeRate.getTargetCurrency().getId()).get(),
                    savedExchangeRate.getRate());

    }


    public Optional<ExchangeRateDto> updateExchangeRate(ExchangeRateDto exchangeRateDto) {
        var existingExchangeRate = exchangeRateDao.findByKey(exchangeRateDto.id());
        if (existingExchangeRate.isEmpty()) {
            return Optional.empty();
        }

        var baseCurrencyOpt = currencyService.getCurrencyById(exchangeRateDto.baseCurrency().id());
        var targetCurrencyOpt = currencyService.getCurrencyById(exchangeRateDto.targetCurrency().id());

//        if (baseCurrencyOpt.isEmpty() || targetCurrencyOpt.isEmpty()) {
//            return Optional.empty();
//        }

        var updatedExchangeRate = new ExchangeRate(
            exchangeRateDto.id(),
                new Currency(baseCurrencyOpt.get()),
                new Currency(targetCurrencyOpt.get()),
                exchangeRateDto.rate()
        );
        if (exchangeRateDao.update(updatedExchangeRate)) {
            return Optional.of(new ExchangeRateDto(
                updatedExchangeRate.getId(),
                CurrencyDto.buildDto(updatedExchangeRate.getBaseCurrency()),
                CurrencyDto.buildDto(updatedExchangeRate.getTargetCurrency()),
                updatedExchangeRate.getRate()
            ));
        } else {
            return Optional.empty();
        }
    }

    public Optional<ExchangeRateDto> getExchangeRateDtoByCode(String baseCode, String targetCode) {
        List<ExchangeRateDto> exchangeRateDtoList = getAllExchangeRates();

        for (ExchangeRateDto exchangeRateDto : exchangeRateDtoList) {
            if (exchangeRateDto.baseCurrency().code().equals(baseCode)
                && exchangeRateDto.targetCurrency().code().equals(targetCode)) {
                return Optional.of(exchangeRateDto);
            }
        }
        return Optional.empty();
    }

    public BigDecimal getExchangeRateByCode(String baseCode, String targetCode) {
        List<ExchangeRateDto> exchangeRateDtoList = getAllExchangeRates();

        for (ExchangeRateDto exchangeRateDto : exchangeRateDtoList) {
            if (exchangeRateDto.baseCurrency().code().equals(baseCode)
                && exchangeRateDto.targetCurrency().code().equals(targetCode)) {
                return exchangeRateDto.rate();
            } else if (exchangeRateDto.baseCurrency().code().equals(targetCode)
            && exchangeRateDto.targetCurrency().code().equals(baseCode)) {
                return BigDecimal.valueOf(1.0).divide(exchangeRateDto.rate());
            }
        }
        return BigDecimal.ZERO;
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
