package ru.arthu.currencyexchange.controller;


import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ru.arthu.currencyexchange.dto.CurrencyDto;
import ru.arthu.currencyexchange.dto.ErrorCode;
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
import java.util.logging.Logger;

import static ru.arthu.currencyexchange.utils.ResponseUtil.respondWithError;

@WebServlet(urlPatterns = {"/currencies", "/currency/*"})
public class CurrencyServlet extends HttpServlet {

    private final Class<?> clazz = getClass();
    private final CurrencyService currencyService = CurrencyService.getInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        req.setCharacterEncoding("UTF-8");
        try {
            String servletPath = req.getServletPath();
            String pathInfo = req.getPathInfo();

            if (servletPath.equals("/currencies")) {
                handleAllCurrencies(resp);
                return;
            }

            if (pathInfo == null || pathInfo.equals("/") || pathInfo.trim().isEmpty()) {
                respondWithError(ErrorCode.MISSING_REQUIRED_PARAMS, resp, clazz);
                return;
            }

            String currencyCode = pathInfo.substring(1);
            handleCurrencyByCode(resp, currencyCode);
        } catch (CurrencyCodeNotFoundException e) {
            respondWithError(ErrorCode.CURRENCY_CODE_NOT_FOUND, resp, clazz);
        } catch (DatabaseUnavailableException | GeneralDatabaseException e) {
            respondWithError(ErrorCode.INTERNAL_SERVER_ERROR, resp, clazz);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        req.setCharacterEncoding("UTF-8");
        String name = req.getParameter("name");
        String code = req.getParameter("code");
        String sign = req.getParameter("sign");

        if (isInvalidParam(name) || isInvalidParam(code) || isInvalidParam(sign)) {
            respondWithError(ErrorCode.MISSING_REQUIRED_PARAMS, resp, clazz);
            return;
        }
        if (code.length() != 3) {
            respondWithError(ErrorCode.INVALID_CURRENCY_CODE_LENGTH, resp, clazz);
            return;
        }
        try {
            CurrencyDto newCurrency = currencyService.createCurrency(
                    CurrencyMapper.createRequestDto(code, name, sign));
            ResponseUtil.writeJsonResponse(resp, newCurrency, HttpServletResponse.SC_CREATED);
        } catch (ObjectAlreadyExistException e) {
            respondWithError(ErrorCode.CURRENCY_ALREADY_EXISTS, resp, clazz);
        } catch (DatabaseUnavailableException | GeneralDatabaseException | CannotSaveException e) {
            respondWithError(ErrorCode.INTERNAL_SERVER_ERROR, resp, clazz);
        }
    }

    private void handleAllCurrencies(HttpServletResponse resp) throws IOException {
        List<CurrencyDto> currencies = currencyService.getAllCurrencies();
        ResponseUtil.writeJsonResponse(resp, currencies, HttpServletResponse.SC_OK);
    }

    private void handleCurrencyByCode(HttpServletResponse resp, String code) throws IOException {
        var currency = currencyService.getCurrency(code);
        ResponseUtil.writeJsonResponse(resp, currency, HttpServletResponse.SC_OK);
    }

    private boolean isInvalidParam(String value) {
        return value == null || value.isBlank();
    }
}
