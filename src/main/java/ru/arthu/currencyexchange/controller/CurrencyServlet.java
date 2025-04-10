package ru.arthu.currencyexchange.controller;


import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ru.arthu.currencyexchange.dto.CurrencyDto;
import ru.arthu.currencyexchange.dto.ErrorDto;
import ru.arthu.currencyexchange.exceptions.CannotSaveException;
import ru.arthu.currencyexchange.exceptions.ObjectAlreadyExistException;
import ru.arthu.currencyexchange.exceptions.CurrencyCodeNotFoundException;
import ru.arthu.currencyexchange.exceptions.db.DatabaseUnavailableException;
import ru.arthu.currencyexchange.exceptions.db.GeneralDatabaseException;
import ru.arthu.currencyexchange.service.CurrencyService;
import ru.arthu.currencyexchange.utils.ResponseUtil;
import ru.arthu.currencyexchange.utils.mappers.CurrencyMapper;

import java.io.IOException;
import java.util.List;

@WebServlet(urlPatterns = {"/currencies", "/currency/*"})
public class CurrencyServlet extends HttpServlet {

    private final CurrencyService currencyService = CurrencyService.getInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws IOException {
        try {
            String pathInfo = req.getPathInfo();
            if (pathInfo == null) {
                List<CurrencyDto> currencies = currencyService.getAllCurrencies();
                ResponseUtil.writeJsonResponse(resp, currencies, HttpServletResponse.SC_OK);
            } else {
                String currencyCode = pathInfo.substring(1);
                if (currencyCode.isEmpty()) {
                    ResponseUtil.writeJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, new ErrorDto(
                            "Не передан параметр кода валюты"
                    ));
                } else {
                    var currency = currencyService.getCurrency(
                            currencyCode);
                    ResponseUtil.writeJsonResponse(resp, currency, HttpServletResponse.SC_OK);
                }
            }
        } catch (CurrencyCodeNotFoundException e) {
            ResponseUtil.writeJsonError(resp, HttpServletResponse.SC_NOT_FOUND, new ErrorDto(
                    "Код валюты не найден"
            ));
        } catch (DatabaseUnavailableException | GeneralDatabaseException e) {
            ResponseUtil.writeJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, new ErrorDto(
                    "Внутренняя ошибка сервера"
            ));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws IOException {
        req.setCharacterEncoding("UTF-8");
        String name = req.getParameter("name");
        String code = req.getParameter("code");
        String sign = req.getParameter("sign");

        if (name == null || code == null || sign == null || name.isBlank() || code.isBlank()
            || sign.isBlank()) {
            ResponseUtil.writeJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, new ErrorDto(
                    "Отсутствуют обязательные параметры"
            ));
            return;
        }
        if (code.length() != 3) {
            ResponseUtil.writeJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, new ErrorDto(
                    "Длина кода должна быть равна 3-м символам"
            ));
            return;
        }
        try {
            CurrencyDto newCurrency = currencyService.createCurrency(
                    CurrencyMapper.createRequestDto(code, name, sign));
            ResponseUtil.writeJsonResponse(resp, newCurrency, HttpServletResponse.SC_CREATED);
        } catch (CannotSaveException e) {
            ResponseUtil.writeJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, new ErrorDto(
                    "Ошибка при сохранении валюты"
            ));
        }
        catch (ObjectAlreadyExistException e) {
            ResponseUtil.writeJsonError(resp, HttpServletResponse.SC_CONFLICT, new ErrorDto(
                    "Валюта с таким кодом уже существует"
            ));
        } catch (DatabaseUnavailableException | GeneralDatabaseException e) {
            ResponseUtil.writeJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, new ErrorDto(
                    "Внутренняя ошибка сервера"
            ));
        }
    }
}
