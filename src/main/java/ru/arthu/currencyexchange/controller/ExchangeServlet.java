package ru.arthu.currencyexchange.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;

import ru.arthu.currencyexchange.dto.ErrorDto;
import ru.arthu.currencyexchange.dto.ExchangeDto;
import ru.arthu.currencyexchange.exceptions.CurrencyCodeNotFoundException;
import ru.arthu.currencyexchange.service.ExchangeService;
import ru.arthu.currencyexchange.utils.ResponseUtil;

@WebServlet(urlPatterns = {"/exchange"})
public class ExchangeServlet extends HttpServlet {

    private final ExchangeService exchangeService = ExchangeService.getInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws IOException {
        try {
            String fromCurrency = req.getParameter("from");
            String toCurrency = req.getParameter("to");
            String amount = req.getParameter("amount");

            if (fromCurrency == null || toCurrency == null || amount == null) {
                ResponseUtil.writeJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, new ErrorDto(
                        "Не указано одно из обязательных полей"
                ));
            } else {
                BigDecimal amountDecimal;
                try {
                    amountDecimal = new BigDecimal(amount);
                    ExchangeDto exchangeDto = exchangeService.exchange(fromCurrency, toCurrency,
                            amountDecimal);
                    ResponseUtil.writeJsonResponse(resp, exchangeDto);
                } catch (CurrencyCodeNotFoundException e) {
                    ResponseUtil.writeJsonError(resp, HttpServletResponse.SC_NOT_FOUND, new ErrorDto(
                            "Один или оба кода валюты не были найдены"));
                } catch (NumberFormatException e) {
                    ResponseUtil.writeJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, new ErrorDto(
                            "Некорректный формат суммы"));
                }
            }

        } catch (Exception e) {
            ResponseUtil.writeJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, new ErrorDto(
                    String.format("Внутренняя ошибка сервера: %s", e.getMessage())));
            throw new RuntimeException(e);
        }
    }
}
