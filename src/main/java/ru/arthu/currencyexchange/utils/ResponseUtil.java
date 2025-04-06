package ru.arthu.currencyexchange.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import ru.arthu.currencyexchange.dto.ErrorDto;

import java.io.IOException;

public class ResponseUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void writeJsonResponse(HttpServletResponse resp, Object data, int statusCode) throws IOException {
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        resp.getWriter().write(objectMapper.writeValueAsString(data));
        resp.setStatus(statusCode);
    }

    public static void writeJsonError(HttpServletResponse resp, int errorCode, ErrorDto errorDto) throws IOException {
        writeJsonResponse(resp, errorDto, errorCode);
    }

}
