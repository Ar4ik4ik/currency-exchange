package ru.arthu.currencyexchange.controller;


import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import ru.arthu.currencyexchange.dto.ErrorCode;
import ru.arthu.currencyexchange.dto.ExchangeRateDto;
import ru.arthu.currencyexchange.exceptions.CurrencyCodeNotFoundException;
import ru.arthu.currencyexchange.exceptions.ObjectAlreadyExistException;
import ru.arthu.currencyexchange.exceptions.db.CheckConstraintViolationException;
import ru.arthu.currencyexchange.exceptions.db.DatabaseUnavailableException;
import ru.arthu.currencyexchange.exceptions.db.GeneralDatabaseException;
import ru.arthu.currencyexchange.service.ExchangeRateService;
import ru.arthu.currencyexchange.utils.ResponseUtil;
import ru.arthu.currencyexchange.utils.mappers.ExchangeRateMapper;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import static ru.arthu.currencyexchange.utils.ResponseUtil.respondWithError;


@WebServlet(urlPatterns = {"/exchangeRates"})
public class ExchangeRatesServlet extends HttpServlet {
    private final Class<?> clazz = getClass();
    private final ExchangeRateService exchangeRateService = ExchangeRateService.getInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        try {
            List<ExchangeRateDto> exchangeRates = exchangeRateService.getAllExchangeRates();
            ResponseUtil.writeJsonResponse(resp, exchangeRates, HttpServletResponse.SC_OK);
        } catch (DatabaseUnavailableException | GeneralDatabaseException e) {
            respondWithError(ErrorCode.INTERNAL_SERVER_ERROR, resp, clazz);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");

        String baseCurrencyCode = req.getParameter("baseCurrencyCode");
        String targetCurrencyCode = req.getParameter("targetCurrencyCode");
        String rateStr = req.getParameter("rate");

        if (isInvalidParam(baseCurrencyCode) || isInvalidParam(targetCurrencyCode) || isInvalidParam(rateStr)) {
            respondWithError(ErrorCode.MISSING_REQUIRED_PARAMS, resp, clazz);
        }

        BigDecimal rate;
        try {
            rate = new BigDecimal(rateStr);
        } catch (NumberFormatException e) {
            respondWithError(ErrorCode.INVALID_EXCHANGE_RATE_PARAM, resp, clazz);
            return;
        }

        try {
            ExchangeRateDto createdExchangeRate = exchangeRateService.createExchangeRate(
                    ExchangeRateMapper.createRequestDto(baseCurrencyCode, targetCurrencyCode, rate));
            ResponseUtil.writeJsonResponse(resp, createdExchangeRate, HttpServletResponse.SC_CREATED);
        } catch (ObjectAlreadyExistException e) {
            respondWithError(ErrorCode.EXCHANGE_RATE_ALREADY_EXISTS, resp, clazz);

        } catch (CurrencyCodeNotFoundException e) {
            respondWithError(ErrorCode.CURRENCY_CODE_NOT_FOUND, resp, clazz);
        } catch (IllegalArgumentException e) {
            respondWithError(ErrorCode.INVALID_EXCHANGE_RATE, resp, clazz);
        } catch (CheckConstraintViolationException e) {
            respondWithError(ErrorCode.IDENTICAL_TARGET_BASE, resp, clazz);
        } catch (DatabaseUnavailableException | GeneralDatabaseException e) {
            respondWithError(ErrorCode.INTERNAL_SERVER_ERROR, resp, clazz);
            throw new RuntimeException(e);
        }
    }

    private boolean isInvalidParam(String value) {
        return value == null || value.isBlank();
    }
}
