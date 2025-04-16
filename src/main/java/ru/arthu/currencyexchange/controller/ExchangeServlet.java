package ru.arthu.currencyexchange.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;

import ru.arthu.currencyexchange.dto.ErrorCode;
import ru.arthu.currencyexchange.dto.ExchangeDto;
import ru.arthu.currencyexchange.dto.ExchangeRequestDto;
import ru.arthu.currencyexchange.exceptions.CurrencyCodeNotFoundException;
import ru.arthu.currencyexchange.exceptions.ExchangeRateNotFoundException;
import ru.arthu.currencyexchange.service.ExchangeService;
import ru.arthu.currencyexchange.utils.ResponseUtil;

import static ru.arthu.currencyexchange.utils.ResponseUtil.respondWithError;

@WebServlet(urlPatterns = {"/exchange"})
public class ExchangeServlet extends HttpServlet {

    private final ExchangeService exchangeService = ExchangeService.getInstance();
    private final Class<?> clazz = getClass();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws IOException {
        try {
            String fromCurrency = req.getParameter("from");
            String toCurrency = req.getParameter("to");
            String amount = req.getParameter("amount");

            if (isBlank(fromCurrency) || isBlank(toCurrency) || isBlank(amount)) {
                respondWithError(ErrorCode.MISSING_REQUIRED_PARAMS, resp, clazz);
            } else {
                BigDecimal amountDecimal;
                try {
                    amountDecimal = new BigDecimal(amount);
                    ExchangeDto exchangeDto = exchangeService.exchange(
                            new ExchangeRequestDto(
                                    fromCurrency, toCurrency, amountDecimal));
                    ResponseUtil.writeJsonResponse(resp, exchangeDto, HttpServletResponse.SC_OK);
                } catch (CurrencyCodeNotFoundException e) {
                    respondWithError(ErrorCode.CURRENCY_CODE_NOT_FOUND, resp, clazz);
                } catch (NumberFormatException e) {
                    respondWithError(ErrorCode.INVALID_EXCHANGE_RATE_PARAM, resp, clazz);
                } catch (ExchangeRateNotFoundException e) {
                    respondWithError(ErrorCode.EXCHANGE_RATE_NOT_FOUND, resp, clazz);
                }
            }

        } catch (Exception e) {
            respondWithError(ErrorCode.INTERNAL_SERVER_ERROR, resp, clazz);
            throw new RuntimeException(e);
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

}
