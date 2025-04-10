package ru.arthu.currencyexchange.utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

public final class DataSourceProvider {

    private static final HikariDataSource dataSource;

    static {
        try {
            Class.forName("org.sqlite.JDBC").newInstance();
            URL resource = DataSourceProvider.class.getClassLoader().getResource("exchangeRate.db");
            assert resource != null;
            String path = new File(resource.toURI()).getAbsolutePath();

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:sqlite:" + path);
            config.setMaximumPoolSize(5);
            config.setPoolName("CurrencyExchangePool");
            config.setConnectionTestQuery("SELECT 1");

            dataSource = new HikariDataSource(config);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Ошибка при инициализации пула соединений", e);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static DataSource getDataSource() {
        return dataSource;
    }
}
