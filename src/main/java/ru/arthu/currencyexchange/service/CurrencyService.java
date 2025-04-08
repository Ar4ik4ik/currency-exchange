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
                .map(CurrencyDto::fromModel).toList();
    }

    public CurrencyDto createCurrency(String code, String fullName, String sign) {
        try {
            return CurrencyDto.fromModel(
                    currencyDao.save(
                            new Currency(null, code, fullName, sign)));

        } catch (UniqueConstraintViolationException e) {
            throw new CurrencyAlreadyExist();
        }
    }

    public CurrencyDto getCurrency(String code) throws CurrencyCodeNotFoundException {
        return currencyDao.findByKey(code)
                .map(CurrencyDto::fromModel)
                .orElseThrow(CurrencyCodeNotFoundException::new);
    }

    private CurrencyService() {
    }

    public static CurrencyService getInstance() {
        return INSTANCE;
    }
}
