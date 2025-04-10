package ru.arthu.currencyexchange.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.math.BigDecimal;

import ru.arthu.currencyexchange.dto.ErrorDto;
import ru.arthu.currencyexchange.dto.ExchangeRateRequestDto;
import ru.arthu.currencyexchange.exceptions.CannotUpdateException;
import ru.arthu.currencyexchange.exceptions.CurrencyCodeNotFoundException;
import ru.arthu.currencyexchange.exceptions.ExchangeRateNotFoundException;
import ru.arthu.currencyexchange.exceptions.db.DatabaseUnavailableException;
import ru.arthu.currencyexchange.exceptions.db.GeneralDatabaseException;
import ru.arthu.currencyexchange.service.ExchangeRateService;
import ru.arthu.currencyexchange.utils.ResponseUtil;

import java.io.IOException;

@WebServlet(urlPatterns = {"/exchangeRate/*"})
public class ExchangeRateServlet extends HttpServlet {

    private final ExchangeRateService exchangeRateService = ExchangeRateService.getInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        String pathInfo = req.getPathInfo();
        System.out.println(pathInfo);
        if (pathInfo == null || pathInfo.length() != 7) {
            ResponseUtil.writeJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, new ErrorDto(
                    "Коды валют пары отсутствуют в адресе или длина одной или двух валют не равна 3-м символам"
            ));
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
            ResponseUtil.writeJsonError(resp, HttpServletResponse.SC_NOT_FOUND, new ErrorDto(
                    "Обменный курс для пары не найден"
            ));
        } catch (DatabaseUnavailableException | GeneralDatabaseException e) {
            ResponseUtil.writeJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, new ErrorDto(
                    "Внутренняя ошибка сервера"
            ));
            throw new RuntimeException(e);
        }


    }

    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        String pathInfo = req.getPathInfo();
        String body = new String(req.getInputStream().readAllBytes());
        String[] params = body.split("=");
        if (pathInfo == null || pathInfo.length() != 7 || params.length != 2 || !params[0].equals("rate")) {
            ResponseUtil.writeJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, new ErrorDto(
                    "Переданы некорректные параметры"
            ));
            return;
        }
        double rate;
        try {
            rate = Double.parseDouble(params[1]);
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid rate value");
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
            ResponseUtil.writeJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, new ErrorDto(
                    "Некорректное значение переданного параметра курса. Курс должен быть > 0"
            ));
        } catch (ExchangeRateNotFoundException e) {
            ResponseUtil.writeJsonError(resp, HttpServletResponse.SC_NOT_FOUND, new ErrorDto(
                    "Валютная пара не найдена"
            ));
        } catch (CurrencyCodeNotFoundException e) {
            ResponseUtil.writeJsonError(resp, HttpServletResponse.SC_NOT_FOUND, new ErrorDto(
                    "Одна или более валют не найдены"
            ));
        } catch (CannotUpdateException e) {
            ResponseUtil.writeJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, new ErrorDto(
                    "Ошибка при обновлении курса валютной пары"
            ));
        } catch (DatabaseUnavailableException | GeneralDatabaseException e) {
            ResponseUtil.writeJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, new ErrorDto(
                    "Внутренняя ошибка сервера"
            ));
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
