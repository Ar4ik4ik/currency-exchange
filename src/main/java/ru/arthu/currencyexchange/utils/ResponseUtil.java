package ru.arthu.currencyexchange.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import ru.arthu.currencyexchange.dto.ErrorCode;
import ru.arthu.currencyexchange.dto.ErrorDto;

import java.io.IOException;
import java.util.logging.Logger;

public class ResponseUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void writeJsonResponse(HttpServletResponse resp, Object data, int statusCode) throws IOException {
        resp.setContentType("application/json");
        resp.getWriter().write(objectMapper.writeValueAsString(data));
        resp.setStatus(statusCode);
    }

    public static void writeJsonError(HttpServletResponse resp, int errorCode, ErrorDto errorDto) throws IOException {
        writeJsonResponse(resp, errorDto, errorCode);
    }

    public static void respondWithError(ErrorCode errorCode, HttpServletResponse resp, Class<?> clazz) throws IOException {
        Logger logger = Logger.getLogger(clazz.getName());
        logger.warning(errorCode.getMessage());
        ResponseUtil.writeJsonError(resp, errorCode.getHttpStatus(), new ErrorDto(errorCode));
    }

}
