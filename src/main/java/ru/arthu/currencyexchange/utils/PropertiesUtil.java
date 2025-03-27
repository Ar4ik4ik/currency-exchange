package ru.arthu.currencyexchange.utils;

import java.io.IOException;
import java.util.Properties;

public final class PropertiesUtil {

    private static final Properties PROPERTIES = new Properties();

    static  {
        loadProperties();
    }

    private static void loadProperties() {
        try (var inputStream = PropertiesUtil.class.getClassLoader().getResourceAsStream("jdbcsettings.properties")) {
            PROPERTIES.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getProperty(String key) {
        return PROPERTIES.getProperty(key);
    }
}
