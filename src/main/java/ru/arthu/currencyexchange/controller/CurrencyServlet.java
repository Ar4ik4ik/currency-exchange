package ru.arthu.currencyexchange.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ru.arthu.currencyexchange.dto.CurrencyDto;
import ru.arthu.currencyexchange.dto.ErrorDto;
import ru.arthu.currencyexchange.exceptions.CurrencyAlreadyExist;
import ru.arthu.currencyexchange.exceptions.CurrencyCodeNotFoundException;
import ru.arthu.currencyexchange.exceptions.db.DatabaseUnavailableException;
import ru.arthu.currencyexchange.exceptions.db.GeneralDatabaseException;
import ru.arthu.currencyexchange.exceptions.db.UniqueConstraintViolationException;
import ru.arthu.currencyexchange.service.CurrencyService;
import ru.arthu.currencyexchange.utils.ResponseUtil;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@WebServlet(urlPatterns = {"/currencies", "/currency/*"})
public class CurrencyServlet extends HttpServlet {

    private final CurrencyService currencyService = CurrencyService.getInstance();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
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
                    Optional<CurrencyDto> currency = currencyService.getCurrencyByCode(
                            currencyCode);
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
        throws ServletException, IOException {
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
        try {
            CurrencyDto newCurrency = currencyService.createCurrency(code, name, sign);
            ResponseUtil.writeJsonResponse(resp, newCurrency, HttpServletResponse.SC_CREATED);
        } catch (CurrencyAlreadyExist e) {
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
