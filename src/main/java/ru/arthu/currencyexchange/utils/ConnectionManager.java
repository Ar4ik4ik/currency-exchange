package ru.arthu.currencyexchange.utils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;


public final class ConnectionManager {

    private static final DataSource dataSource = DataSourceProvider.getDataSource();

    public static Connection open() throws SQLException {
        return dataSource.getConnection();
    }
}

