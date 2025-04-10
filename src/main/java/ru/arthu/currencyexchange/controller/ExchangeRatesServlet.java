package ru.arthu.currencyexchange.controller;


import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import ru.arthu.currencyexchange.dto.ErrorDto;
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


@WebServlet(urlPatterns = {"/exchangeRates"})
public class ExchangeRatesServlet extends HttpServlet {

    private final ExchangeRateService exchangeRateService = ExchangeRateService.getInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws IOException {
        try {
            List<ExchangeRateDto> exchangeRates = exchangeRateService.getAllExchangeRates();
            ResponseUtil.writeJsonResponse(resp, exchangeRates, HttpServletResponse.SC_OK);
        } catch (DatabaseUnavailableException | GeneralDatabaseException e) {
            ResponseUtil.writeJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    new ErrorDto("Внутренняя ошибка сервера"));
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");

        String baseCurrencyCode = req.getParameter("baseCurrencyCode");
        String targetCurrencyCode = req.getParameter("targetCurrencyCode");
        String rateStr = req.getParameter("rate");

        if (baseCurrencyCode == null || targetCurrencyCode == null || rateStr == null
                || baseCurrencyCode.isBlank() || targetCurrencyCode.isBlank() || rateStr.isBlank()) {
            ResponseUtil.writeJsonError(resp, HttpServletResponse.SC_BAD_REQUEST,
                    new ErrorDto("Одно из полей пустое"));
            return;
        }

        BigDecimal rate;
        try {
            rate = new BigDecimal(rateStr);
        } catch (NumberFormatException e) {
            ResponseUtil.writeJsonError(resp, HttpServletResponse.SC_BAD_REQUEST,
                    new ErrorDto("Неверный формат курса"));
            return;
        }

        try {
            ExchangeRateDto createdExchangeRate = exchangeRateService.createExchangeRate(
                    ExchangeRateMapper.createRequestDto(baseCurrencyCode, targetCurrencyCode, rate));
            ResponseUtil.writeJsonResponse(resp, createdExchangeRate, HttpServletResponse.SC_CREATED);
        } catch (ObjectAlreadyExistException e) {
            ResponseUtil.writeJsonError(resp, HttpServletResponse.SC_CONFLICT,
                    new ErrorDto("Курс обмена уже существует"));
        } catch (CurrencyCodeNotFoundException | IllegalArgumentException e) {
            ResponseUtil.writeJsonError(resp, HttpServletResponse.SC_NOT_FOUND,
                    new ErrorDto("Базовая или целевая валюта не найдена, либо курс <= 0"));
        } catch (CheckConstraintViolationException e) {
            ResponseUtil.writeJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, new ErrorDto(
                    "Базовая и целевая валюты должны быть разные"
            ));
        } catch (DatabaseUnavailableException | GeneralDatabaseException e) {
            ResponseUtil.writeJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    new ErrorDto("Внутренняя ошибка сервера"));
            throw new RuntimeException(e);
        }
    }

}
