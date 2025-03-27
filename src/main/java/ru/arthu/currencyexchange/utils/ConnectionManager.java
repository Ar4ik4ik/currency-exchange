package ru.arthu.currencyexchange.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static ru.arthu.currencyexchange.utils.PropertiesUtil.getProperty;


public final class ConnectionManager {

    private static final String URL_KEY = "db.url";
    private static final String USERNAME_KEY = "db.username";
    private static final String PASSWORD_KEY = "db.password";
    private static final String POOL_SIZE_KEY = "db.pool.size";
    private static final int DEFAULT_POOL_SIZE = 10;

    public static Connection open() {
        try {
            return DriverManager.getConnection(
                    getProperty(URL_KEY),
                    getProperty(USERNAME_KEY),
                    getProperty(PASSWORD_KEY)
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
