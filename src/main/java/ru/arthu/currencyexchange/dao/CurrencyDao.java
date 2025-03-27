package ru.arthu.currencyexchange.dao;

import ru.arthu.currencyexchange.model.Currency;
import ru.arthu.currencyexchange.utils.ConnectionManager;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CurrencyDao implements Dao<Long, Currency> {

    private static final CurrencyDao INSTANCE = new CurrencyDao();

    private static final String SAVE_SQL = """
            INSERT INTO Currencies (Code, FullName, Sign)
            VALUES (?, ?, ?);
            """;

    private static final String DELETE_SQL = """
            DELETE FROM main.Currencies
            WHERE ID = ?;
            """;


    private static final String FIND_ALL_SQL = """
            SELECT Id, Code, FullName, Sign
            FROM main.Currencies
            """;

    private static final String FIND_BY_ID_SQL = FIND_ALL_SQL + " WHERE Id = ?";

    private static final String UPDATE_SQL = """
            UPDATE Currencies SET Code = ?, FullName = ?, Sign = ?
            WHERE Id = ?;
            """;

    @Override
    public boolean update(Currency entity) {
        try (var connection = ConnectionManager.open(); var statement = connection.prepareStatement(UPDATE_SQL)) {
            statement.setString(1, entity.getCode());
            statement.setString(2, entity.getFullName());
            statement.setString(3, entity.getSign());
            statement.setLong(4, entity.getId());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Currency> findAll() {
        List<Currency> currencyList = new ArrayList<>();
        try (var connection = ConnectionManager.open();
             var statement = connection.prepareStatement(FIND_ALL_SQL);
             var resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                currencyList.add(new Currency(
                        resultSet.getLong("Id"),
                        resultSet.getString("FullName"), resultSet.getString("Code"),
                        resultSet.getString("Sign")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении списка валют", e);
        }
        return currencyList;
    }

    @Override
    public Optional<Currency> findByKey(Long key) {
        try (var connection = ConnectionManager.open(); var statement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            statement.setLong(1, key);
            try (var resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(new Currency(resultSet.getLong("Id"), resultSet.getString("FullName"), resultSet.getString("Code"), resultSet.getString("Sign")));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Currency save(Currency entity) {
        try (var connection = ConnectionManager.open(); var statement = connection.prepareStatement(SAVE_SQL, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, entity.getCode());
            statement.setString(2, entity.getFullName());
            statement.setString(3, entity.getSign());
            statement.executeUpdate();

            try (var generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    entity.setId(generatedKeys.getLong(1));
                }
            }
            return entity;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean delete(Long key) {
        try (var connection = ConnectionManager.open(); var statement = connection.prepareStatement(DELETE_SQL)) {
            statement.setLong(1, key);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private CurrencyDao() {
    }

    public static CurrencyDao getInstance() {
        return INSTANCE;
    }
}
