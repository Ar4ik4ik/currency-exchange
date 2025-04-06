package ru.arthu.currencyexchange.service;

import ru.arthu.currencyexchange.dao.CurrencyDao;
import ru.arthu.currencyexchange.dto.CurrencyDto;
import ru.arthu.currencyexchange.exceptions.CurrencyAlreadyExist;
import ru.arthu.currencyexchange.exceptions.CurrencyCodeNotFoundException;
import ru.arthu.currencyexchange.exceptions.db.UniqueConstraintViolationException;
import ru.arthu.currencyexchange.model.Currency;

import java.util.List;

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

    public CurrencyDto createCurrency(String code, String fullName, String sign) {
        try {
            var currency = new Currency(null, code, fullName, sign);
            var savedCurrency = currencyDao.save(currency);
            return new CurrencyDto(savedCurrency.getId(), savedCurrency.getCode(),
                    savedCurrency.getFullName(),
                    savedCurrency.getSign());
        } catch (UniqueConstraintViolationException e) {
            throw new CurrencyAlreadyExist();
        }
    }

    private CurrencyService() {
    }

    public static CurrencyService getInstance() {
        return INSTANCE;
    }

    public CurrencyDto getCurrency(Long id) throws CurrencyCodeNotFoundException {
        return currencyDao.findByKey(id)
                .map(this::mapToDto)
                .orElseThrow(CurrencyCodeNotFoundException::new);
    }

    public CurrencyDto getCurrency(String code) throws CurrencyCodeNotFoundException {
        return currencyDao.findByKey(code)
                .map(this::mapToDto)
                .orElseThrow(CurrencyCodeNotFoundException::new);
    }

    private CurrencyDto mapToDto(Currency currency) {
        return new CurrencyDto(
                currency.getId(),
                currency.getCode(),
                currency.getFullName(),
                currency.getSign()
        );
    }
}
