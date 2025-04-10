package ru.arthu.currencyexchange.utils.mappers;

import ru.arthu.currencyexchange.dto.CurrencyDto;
import ru.arthu.currencyexchange.dto.CurrencyRequestDto;
import ru.arthu.currencyexchange.model.Currency;

public class CurrencyMapper {

    public static CurrencyRequestDto createRequestDto(String code, String fullName, String sign) {
        return new CurrencyRequestDto(code, fullName, sign);
    }

    public static Currency fromRequestDto(CurrencyRequestDto requestDto) {
        return new Currency(
                null, requestDto.code(), requestDto.fullName(), requestDto.sign()
        );
    }

    public static CurrencyDto fromModel(Currency currencyModel) {
        return new CurrencyDto(
                currencyModel.id(),
                currencyModel.code(),
                currencyModel.fullName(),
                currencyModel.sign()
        );
    }

    public static Currency fromDto(CurrencyDto currencyDto) {
        return new Currency(
                currencyDto.id(),
                currencyDto.code(),
                currencyDto.name(),
                currencyDto.sign()
        );
    }
}
