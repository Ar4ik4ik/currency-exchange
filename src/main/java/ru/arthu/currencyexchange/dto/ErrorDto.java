package ru.arthu.currencyexchange.dto;

public class ErrorDto {
    private final String message;

    public ErrorDto(ErrorCode code) {
        this.message = code.getMessage();
    }

    public String getMessage() {
        return message;
    }
}
