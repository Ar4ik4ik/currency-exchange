package ru.arthu.currencyexchange.dto;

public class CurrencyDto {

    private final String code;
    private final String fullName;
    private final String Sign;

    public CurrencyDto(String code, String fullName, String sign) {
        this.code = code;
        this.fullName = fullName;
        Sign = sign;
    }

    public String getCode() {
        return code;
    }

    public String getFullName() {
        return fullName;
    }

    public String getSign() {
        return Sign;
    }
}
