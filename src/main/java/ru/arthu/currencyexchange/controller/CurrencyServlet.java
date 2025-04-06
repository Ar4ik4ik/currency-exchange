package ru.arthu.currencyexchange.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ru.arthu.currencyexchange.dto.CurrencyDto;
import ru.arthu.currencyexchange.service.CurrencyService;

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
                resp.setContentType("application/json");
                resp.getWriter().write(objectMapper.writeValueAsString(currencies));
                resp.setStatus(HttpServletResponse.SC_OK);
            } else {
                String currencyCode = pathInfo.substring(1);
                if (currencyCode.isEmpty()) {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                } else {
                    Optional<CurrencyDto> currency = currencyService.getCurrencyByCode(
                        currencyCode);
                    if (currency.isPresent()) {
                        resp.setContentType("application/json");
                        resp.getWriter().write(objectMapper.writeValueAsString(currency.get()));
                        resp.setStatus(HttpServletResponse.SC_OK);
                    } else {
                        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                    }
                }
            }
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            throw new RuntimeException(e);
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
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                "Missing required fields: name, code, sign");
            return;
        }
        Optional<CurrencyDto> existingCurrency = CurrencyService.getInstance()
            .getCurrencyByCode(code);
        if (existingCurrency.isPresent()) {
            resp.sendError(HttpServletResponse.SC_CONFLICT,
                "Currency with this code is already exist");
            return;
        }

        CurrencyDto newCurrency = currencyService.createCurrency(code, name, sign);
        resp.setContentType("application/json");
        resp.setStatus(HttpServletResponse.SC_CREATED);
        resp.getWriter().write(objectMapper.writeValueAsString(newCurrency));
    }
}
