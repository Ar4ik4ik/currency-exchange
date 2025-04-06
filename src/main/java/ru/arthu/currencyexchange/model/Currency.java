package ru.arthu.currencyexchange.model;

import java.util.Objects;
import ru.arthu.currencyexchange.dto.CurrencyDto;

public class Currency {

    private Long id;
    private String code;
    private String fullName;
    private String sign;


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Currency currency = (Currency) o;
        return Objects.equals(id, currency.id) && Objects.equals(code, currency.code) && Objects.equals(fullName, currency.fullName) && Objects.equals(sign, currency.sign);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, code, fullName, sign);
    }


    public void setId(Long id) {
        this.id = id;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getSign() {
        return sign;
    }

    public long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getFullName() {
        return fullName;
    }

    public Currency(Long id, String code, String fullName, String sign) {
        this.sign = sign;
        this.fullName = fullName;
        this.code = code;
        this.id = id;
    }

    public Currency(CurrencyDto currencyDto) {
        this.id = currencyDto.id();
        this.code = currencyDto.code();
        this.fullName = currencyDto.name();
        this.sign = currencyDto.sign();
    }

    @Override
    public String toString() {
        return "Currency{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", fullName='" + fullName + '\'' +
                ", sign='" + sign + '\'' +
                '}';
    }
}
