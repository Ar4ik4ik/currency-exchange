package ru.arthu.currencyexchange.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import ru.arthu.currencyexchange.dto.ExchangeRateDto;
import ru.arthu.currencyexchange.service.ExchangeRateService;

import java.io.IOException;

@WebServlet(urlPatterns = {"/exchangeRate/*"})
public class ExchangeRateServlet extends HttpServlet {

    private final ExchangeRateService exchangeRateService = ExchangeRateService.getInstance();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
        try {
            String pathInfo = req.getPathInfo();
            System.out.println(pathInfo);
            if (pathInfo == null || pathInfo.length() != 7) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            String baseCurrencyCode = pathInfo.substring(1, 4);
            String targetCurrencyCode = pathInfo.substring(4, 7);
            var optExchangeRateDto = exchangeRateService.getExchangeRateDtoByCode(
                baseCurrencyCode,
                targetCurrencyCode);
            if (optExchangeRateDto.isPresent()) {
                resp.setContentType("application/json");
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write(objectMapper.writeValueAsString(optExchangeRateDto.get()));
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }

        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            throw new RuntimeException(e);
        }
    }

    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setCharacterEncoding("UTF-8");
            String pathInfo = req.getPathInfo();
            String body = new String(req.getInputStream().readAllBytes());
            System.out.println("Raw body: " + body);
            String[] params = body.split("=");
            if (pathInfo == null || pathInfo.length() != 7 || params.length != 2 || !params[0].equals("rate")) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid form data format");
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
            var optExchangeRateDto = exchangeRateService.getExchangeRateDtoByCode(
                baseCurrencyCode,
                targetCurrencyCode);
            if (optExchangeRateDto.isPresent()) {
                var updatedExchangeRate = exchangeRateService.updateExchangeRate(
                    new ExchangeRateDto(
                        optExchangeRateDto.get().id(),
                        optExchangeRateDto.get().baseCurrency(),
                        optExchangeRateDto.get().targetCurrency(),
                        BigDecimal.valueOf(rate)
                    )
                    );
                resp.setContentType("application/json");
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write(objectMapper.writeValueAsString(updatedExchangeRate.get()));
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
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
