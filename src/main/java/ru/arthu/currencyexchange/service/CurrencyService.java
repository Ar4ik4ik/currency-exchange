package ru.arthu.currencyexchange.service;

import ru.arthu.currencyexchange.dao.CurrencyDao;
import ru.arthu.currencyexchange.dto.CurrencyDto;
import ru.arthu.currencyexchange.dto.CurrencyRequestDto;
import ru.arthu.currencyexchange.exceptions.ObjectAlreadyExistException;
import ru.arthu.currencyexchange.exceptions.CurrencyCodeNotFoundException;
import ru.arthu.currencyexchange.exceptions.db.UniqueConstraintViolationException;
import ru.arthu.currencyexchange.model.Currency;
import ru.arthu.currencyexchange.utils.mappers.CurrencyMapper;

import java.util.List;

public class CurrencyService {
    private final CurrencyDao currencyDao;

    public CurrencyService(CurrencyDao currencyDao) {
        this.currencyDao = currencyDao;
    }

    public static CurrencyService getInstance() {
        return new CurrencyService(CurrencyDao.getInstance());
    }

    public List<CurrencyDto> getAllCurrencies() {
        return currencyDao.findAll().stream()
                .map(CurrencyMapper::fromModel).toList();
    }

    public CurrencyDto createCurrency(CurrencyRequestDto requestDto) {
        // When code is duplicated, an exception is thrown
        try {
            Currency newCurrency = CurrencyMapper.fromRequestDto(requestDto);
            return CurrencyMapper.fromModel(
                    currencyDao.save(newCurrency));

        } catch (UniqueConstraintViolationException e) {
            throw new ObjectAlreadyExistException();
        }
    }

    public CurrencyDto getCurrency(String code) throws CurrencyCodeNotFoundException {
        return currencyDao.findByKey(code)
                .map(CurrencyMapper::fromModel)
                .orElseThrow(CurrencyCodeNotFoundException::new);
    }
}
