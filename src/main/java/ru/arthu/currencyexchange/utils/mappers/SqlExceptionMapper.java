package ru.arthu.currencyexchange.utils.mappers;

import ru.arthu.currencyexchange.exceptions.db.*;

import java.sql.SQLException;

public class SqlExceptionMapper {

    public static RuntimeException map(SQLException e) {
        int code = e.getErrorCode();
        String message = e.getMessage().toLowerCase();

        // SQLite: https://www.sqlite.org/rescode.html
        switch (code) {
            case 19: // SQLITE_CONSTRAINT
                if (message.contains("unique")) {
                    return new UniqueConstraintViolationException("Нарушено уникальное ограничение: " + message);
                } else if (message.contains("check")) {
                    return new CheckConstraintViolationException("Нарушено CHECK ограничение: " + message);
                } else if (message.contains("foreign key")) {
                    return new ForeignKeyConstraintViolationException("Нарушено внешнее ограничение: " + message);
                } else if (message.contains("not null")) {
                    return new NotNullConstraintViolationException("NOT NULL ограничение: " + message);
                }
                break;
            case 1: // SQLITE_ERROR (SYNTAX)
                return new SqlSyntaxException("Синтаксическая ошибка: " + message);
            case 14:
            case 5:
                return new DatabaseUnavailableException("База данных недоступна: " + message);
        }
        return new GeneralDatabaseException("Неизвестная ошибка БД: " + message);
    }
}
