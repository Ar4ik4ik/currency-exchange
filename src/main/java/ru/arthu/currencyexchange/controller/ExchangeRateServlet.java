package ru.arthu.currencyexchange.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.math.BigDecimal;

import ru.arthu.currencyexchange.dto.ErrorCode;
import ru.arthu.currencyexchange.dto.ExchangeRateRequestDto;
import ru.arthu.currencyexchange.exceptions.CannotUpdateException;
import ru.arthu.currencyexchange.exceptions.CurrencyCodeNotFoundException;
import ru.arthu.currencyexchange.exceptions.ExchangeRateNotFoundException;
import ru.arthu.currencyexchange.exceptions.db.DatabaseUnavailableException;
import ru.arthu.currencyexchange.exceptions.db.GeneralDatabaseException;
import ru.arthu.currencyexchange.service.ExchangeRateService;
import ru.arthu.currencyexchange.utils.ResponseUtil;

import java.io.IOException;

import static ru.arthu.currencyexchange.utils.ResponseUtil.respondWithError;

@WebServlet(urlPatterns = {"/exchangeRate/*"})
public class ExchangeRateServlet extends HttpServlet {

    private final Class<?> clazz = getClass();
    private final ExchangeRateService exchangeRateService = ExchangeRateService.getInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.length() != 7) {
            respondWithError(ErrorCode.INVALID_CURRENCY_CODE_LENGTH, resp, clazz);
            return;
        }
        String baseCurrencyCode = pathInfo.substring(1, 4);
        String targetCurrencyCode = pathInfo.substring(4, 7);

        try {
            var exchangeRate = exchangeRateService.getExchangeRate(
                    new ExchangeRateRequestDto(
                            baseCurrencyCode, targetCurrencyCode, null
                    ));
            ResponseUtil.writeJsonResponse(resp, exchangeRate, HttpServletResponse.SC_OK);

        } catch (ExchangeRateNotFoundException e) {
            respondWithError(ErrorCode.EXCHANGE_RATE_NOT_FOUND, resp, clazz);

        } catch (DatabaseUnavailableException | GeneralDatabaseException e) {
            respondWithError(ErrorCode.INTERNAL_SERVER_ERROR, resp, clazz);
        }
    }

    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        String pathInfo = req.getPathInfo();
        String body = new String(req.getInputStream().readAllBytes());
        String[] params = body.split("=");

        if (pathInfo == null || pathInfo.length() != 7 || params.length != 2 || !params[0].equals("rate")) {
            respondWithError(ErrorCode.INVALID_PATCH_PARAMS, resp, clazz);
            return;
        }
        double rate;
        try {
            rate = Double.parseDouble(params[1]);
        } catch (NumberFormatException e) {
            respondWithError(ErrorCode.INVALID_PATCH_PARAMS, resp, clazz);
            return;
        }

        String baseCurrencyCode = pathInfo.substring(1, 4);
        String targetCurrencyCode = pathInfo.substring(4, 7);

        try {
            var updatedExchangeRate = exchangeRateService.updateExchangeRate(
                    new ExchangeRateRequestDto(
                            baseCurrencyCode, targetCurrencyCode, BigDecimal.valueOf(rate)
                    ));
            ResponseUtil.writeJsonResponse(resp, updatedExchangeRate, HttpServletResponse.SC_OK);
        } catch (IllegalArgumentException e) {
            respondWithError(ErrorCode.INVALID_EXCHANGE_RATE, resp, clazz);

        } catch (ExchangeRateNotFoundException e) {
            respondWithError(ErrorCode.EXCHANGE_RATE_NOT_FOUND, resp, clazz);

        } catch (CurrencyCodeNotFoundException e) {
            respondWithError(ErrorCode.CURRENCY_CODE_NOT_FOUND, resp, clazz);

        } catch (DatabaseUnavailableException | GeneralDatabaseException | CannotUpdateException e) {
            respondWithError(ErrorCode.INTERNAL_SERVER_ERROR, resp, clazz);
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String method = req.getMethod();
        if (!method.equals("PATCH")) {
            super.service(req, resp);
            return;
        }
        this.doPatch(req, resp);
    }
}
