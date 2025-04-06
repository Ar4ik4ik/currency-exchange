package ru.arthu.currencyexchange.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ru.arthu.currencyexchange.dto.CurrencyDto;
import ru.arthu.currencyexchange.dto.ErrorDto;
import ru.arthu.currencyexchange.dto.ExchangeRateDto;
import ru.arthu.currencyexchange.exceptions.ExchangeAlreadyExistException;
import ru.arthu.currencyexchange.service.CurrencyService;
import ru.arthu.currencyexchange.service.ExchangeRateService;
import ru.arthu.currencyexchange.utils.ResponseUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@WebServlet(urlPatterns = {"/exchangeRates"})
public class ExchangeRatesServlet extends HttpServlet {

    private final ExchangeRateService exchangeRateService = ExchangeRateService.getInstance();
    private final CurrencyService currencyService = CurrencyService.getInstance();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws IOException {
        try {
            List<ExchangeRateDto> exchangeRates = exchangeRateService.getAllExchangeRates();
            ResponseUtil.writeJsonResponse(resp, exchangeRates);
        } catch (Exception e) {
            ResponseUtil.writeJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, new ErrorDto(
                    "Внутренняя ошибка сервера"
            ));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws IOException {
        req.setCharacterEncoding("UTF-8");
        String reqBaseCurrencyCode = req.getParameter("baseCurrencyCode");
        String reqTargetCurrencyCode = req.getParameter("targetCurrencyCode");
        String reqRate = req.getParameter("rate");
        if (reqBaseCurrencyCode == null || reqTargetCurrencyCode == null || reqRate == null
            || reqBaseCurrencyCode.isBlank() || reqTargetCurrencyCode.isBlank()
            || reqRate.isBlank()) {
            ResponseUtil.writeJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, new ErrorDto(
                    "Одно из полей пустое или не передано"
            ));
            return;
        }
        try {
            Optional<CurrencyDto> foundBaseCurrency = currencyService.getCurrencyByCode(
                reqBaseCurrencyCode);
            Optional<CurrencyDto> foundTargetCurrency = currencyService.getCurrencyByCode(
                reqTargetCurrencyCode);

            try {
                ExchangeRateDto createdExchangeRate = null;
                try {
                    createdExchangeRate = exchangeRateService.createExchangeRate(
                        foundBaseCurrency.isPresent() ? foundBaseCurrency.get().id() : -1L,
                        foundTargetCurrency.isPresent() ? foundTargetCurrency.get().id() : -1L,
                        BigDecimal.valueOf(Double.parseDouble(reqRate)));
                } catch (ExchangeAlreadyExistException e) {
                    resp.sendError(HttpServletResponse.SC_CONFLICT);
                }
                resp.setStatus(HttpServletResponse.SC_CREATED);
                resp.setContentType("application/json");
                resp.getWriter().write(objectMapper.writeValueAsString(createdExchangeRate));

            } catch (IllegalArgumentException e) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }

        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            throw new RuntimeException(e);
        }
    }
}
