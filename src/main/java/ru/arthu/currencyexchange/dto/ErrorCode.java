package ru.arthu.currencyexchange.dto;

import jakarta.servlet.http.HttpServletResponse;

public enum ErrorCode {
    CURRENCY_CODE_NOT_FOUND("Код валюты не найден", HttpServletResponse.SC_NOT_FOUND),
    MISSING_REQUIRED_PARAMS("Отсутствуют обязательные параметры", HttpServletResponse.SC_BAD_REQUEST),
    INVALID_CURRENCY_CODE_LENGTH("Длина кода должна быть равна 3-м символам", HttpServletResponse.SC_BAD_REQUEST),
    CURRENCY_ALREADY_EXISTS("Валюта с таким кодом уже существует", HttpServletResponse.SC_CONFLICT),
    EXCHANGE_RATE_ALREADY_EXISTS("Валютная пара с такими кодами уже существует", HttpServletResponse.SC_CONFLICT),
    INTERNAL_SERVER_ERROR("Внутренняя ошибка сервера", HttpServletResponse.SC_INTERNAL_SERVER_ERROR),
    INVALID_EXCHANGE_RATE("Курс должен быть положительным", HttpServletResponse.SC_BAD_REQUEST),
    EXCHANGE_RATE_NOT_FOUND("Валютная пара не найдена", HttpServletResponse.SC_NOT_FOUND),
    IDENTICAL_TARGET_BASE("Базовая и Целевая валюты должны быть разными", HttpServletResponse.SC_BAD_REQUEST),
    INVALID_EXCHANGE_RATE_PARAM("Некорректное значение переданного параметра курса. Курс должен быть > 0", HttpServletResponse.SC_BAD_REQUEST),
    INVALID_PATCH_PARAMS("Переданы некорректные параметры", HttpServletResponse.SC_BAD_REQUEST);

    private final String message;
    private final int httpStatus;

    ErrorCode(String message, int httpStatus) {
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public String getMessage() {
        return message;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
