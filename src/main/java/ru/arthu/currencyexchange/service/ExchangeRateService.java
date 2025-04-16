package ru.arthu.currencyexchange.service;

import org.apache.commons.lang3.tuple.Pair;
import ru.arthu.currencyexchange.dao.ExchangeRateDao;
import ru.arthu.currencyexchange.dao.UpdatableDao;
import ru.arthu.currencyexchange.dto.CurrencyDto;
import ru.arthu.currencyexchange.dto.ExchangeRateDto;
import ru.arthu.currencyexchange.dto.ExchangeRateRequestDto;
import ru.arthu.currencyexchange.exceptions.ExchangeRateNotFoundException;
import ru.arthu.currencyexchange.exceptions.ObjectAlreadyExistException;
import ru.arthu.currencyexchange.exceptions.db.UniqueConstraintViolationException;
import ru.arthu.currencyexchange.model.ExchangeRate;
import ru.arthu.currencyexchange.utils.mappers.ExchangeRateMapper;

import java.math.BigDecimal;
import java.util.List;

public class ExchangeRateService {

    private final UpdatableDao<ExchangeRate, Pair<String, String>> exchangeRateDao;
    private final CurrencyService currencyService = CurrencyService.getInstance();

    public ExchangeRateService(UpdatableDao<ExchangeRate, Pair<String, String>> exchangeRateDao) {
        this.exchangeRateDao = exchangeRateDao;
    }

    public static ExchangeRateService getInstance() {
        return new ExchangeRateService(ExchangeRateDao.getInstance());
    }

    public List<ExchangeRateDto> getAllExchangeRates() {
        return exchangeRateDao.findAll().stream()
                .map(ExchangeRateMapper::fromModel).toList();
    }

    public ExchangeRateDto createExchangeRate(ExchangeRateRequestDto requestDto)
            throws ObjectAlreadyExistException {

        validateRate(requestDto.rate());

        var currencyPair = getCurrencyPair(requestDto);

        var exchangeRate = ExchangeRateMapper.fromDto(
                currencyPair.getLeft(), currencyPair.getRight(), requestDto.rate());
        try {
            return ExchangeRateMapper.fromModel(
                    exchangeRateDao.save(exchangeRate)
            );
        } catch (UniqueConstraintViolationException e) {
            throw new ObjectAlreadyExistException();
        }
    }


    public ExchangeRateDto updateExchangeRate(ExchangeRateRequestDto requestDto) {

        validateRate(requestDto.rate());

        var currencyPair = getCurrencyPair(requestDto);
        var existingExchangeRate = exchangeRateDao.findByKey(
                Pair.of(requestDto.baseCurrencyCode(), requestDto.targetCurrencyCode()));
        if (existingExchangeRate.isEmpty()) {
            throw new ExchangeRateNotFoundException("Валютная пара не найдена");
        } else {
            var updatedExchangeRate = ExchangeRateMapper.fromDto(
                    currencyPair.getLeft(), currencyPair.getRight(),
                    requestDto.rate(), existingExchangeRate.get().id()
            );
            exchangeRateDao.update(updatedExchangeRate);
            return ExchangeRateMapper.fromModel(updatedExchangeRate);
        }
    }

    public ExchangeRateDto getExchangeRate(ExchangeRateRequestDto requestDto) {
        return exchangeRateDao.findByKey(
                Pair.of(requestDto.baseCurrencyCode(), requestDto.targetCurrencyCode()))
                .map(ExchangeRateMapper::fromModel)
                .orElseThrow(() -> new ExchangeRateNotFoundException("..."));

    }

    private void validateRate(BigDecimal exchangeRate) {
        if (exchangeRate == null || exchangeRate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Курс обмена должен быть больше нуля");
        }
    }

    private Pair<CurrencyDto, CurrencyDto> getCurrencyPair(ExchangeRateRequestDto requestDto) {
        var base = currencyService.getCurrency(requestDto.baseCurrencyCode());
        var target = currencyService.getCurrency(requestDto.targetCurrencyCode());
        return Pair.of(base, target);
    }
}
