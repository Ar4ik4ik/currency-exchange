package ru.arthu.currencyexchange.service;

import ru.arthu.currencyexchange.dao.CurrencyDao;
import ru.arthu.currencyexchange.dto.CurrencyDto;
import ru.arthu.currencyexchange.model.Currency;

import java.util.List;
import java.util.Optional;

public class CurrencyService {
    private static final CurrencyService INSTANCE = new CurrencyService();
    private final CurrencyDao currencyDao = CurrencyDao.getInstance();

    public List<CurrencyDto> getAllCurrencies() {
        return currencyDao.findAll().stream()
                .map(currency -> new CurrencyDto(currency.getId(),
                        currency.getCode(),
                        currency.getFullName(),
                        currency.getSign())).toList();
    }

    public Optional<CurrencyDto> getCurrencyById(Long id) {
        return currencyDao.findByKey(id)
                .map(currency -> new CurrencyDto(currency.getId(), currency.getCode(),
                        currency.getFullName(),
                        currency.getSign()));
    }

    public CurrencyDto createCurrency(String code, String fullName, String sign) {
        var currency = new Currency(null, code, fullName, sign);
        Currency savedCurrency = null;
        try {
            savedCurrency = currencyDao.save(currency);
        } catch (java.sql.SQLException throwables) {
            throw new RuntimeException(throwables);
        }
        return new CurrencyDto(savedCurrency.getId(), savedCurrency.getCode(),
                savedCurrency.getFullName(),
                savedCurrency.getSign());
    }

    public boolean updateCurrency(Long id, CurrencyDto currencyDto) {
        var existingCurrency = currencyDao.findByKey(id);
        if (existingCurrency.isEmpty()) {
            return false;
        }
        var updatedCurrency = new Currency(id, currencyDto.code(), currencyDto.name(), currencyDto.sign());
        return currencyDao.update(updatedCurrency);
    }

    public boolean deleteCurrency(Long id) {
        if (currencyDao.findByKey(id).isEmpty()) {
            return false;
        }
        return currencyDao.delete(id);
    }


    private CurrencyService() {
    }

    public static CurrencyService getInstance() {
        return INSTANCE;
    }

    public Optional<CurrencyDto> getCurrencyByCode(String currencyCode) {
        return currencyDao.findByCode(currencyCode)
                .map(currency -> new CurrencyDto(
                        currency.getId(),
                        currency.getCode(),
                        currency.getFullName(),
                        currency.getSign())
                );

    }
}
