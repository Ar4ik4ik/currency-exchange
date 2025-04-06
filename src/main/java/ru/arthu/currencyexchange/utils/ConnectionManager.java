package ru.arthu.currencyexchange.utils;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;


public final class ConnectionManager {

    public static Connection open() {
        try {
            Class.forName("org.sqlite.JDBC").newInstance();
            URL resource = ConnectionManager.class.getClassLoader().getResource("exchangeRate.db");
            String path = null;
            try {
                assert resource != null;
                path = new File(resource.toURI()).getAbsolutePath();
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
            return DriverManager.getConnection(String.format("jdbc:sqlite:%s", path));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
