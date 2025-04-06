package ru.arthu.currencyexchange.dao;

import ru.arthu.currencyexchange.exceptions.db.SqlExceptionMapper;
import ru.arthu.currencyexchange.model.Currency;
import ru.arthu.currencyexchange.utils.ConnectionManager;

import java.sql.ResultSet;
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
            FROM Currencies
            """;

    private static final String FIND_BY_ID_SQL = """
            SELECT Id, Code, FullName, Sign FROM main.Currencies
            WHERE Id = ?;
            """;

    private static final String UPDATE_SQL = """
            UPDATE Currencies SET Code = ?, FullName = ?, Sign = ?
            WHERE Id = ?;
            """;

    private static final String FIND_BY_CODE_SQL = """
            SELECT Id, Code, FullName, Sign FROM main.Currencies
            WHERE Code = ?;
            """;

    @Override
    public boolean update(Currency entity) {
        try (var connection = ConnectionManager.open();
             var statement = connection.prepareStatement(UPDATE_SQL)) {
            statement.setString(1, entity.getCode());
            statement.setString(2, entity.getFullName());
            statement.setString(3, entity.getSign());
            statement.setLong(4, entity.getId());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw SqlExceptionMapper.map(e);
        }
    }

    @Override
    public List<Currency> findAll() {
        List<Currency> currencyList = new ArrayList<>();
        try (var connection = ConnectionManager.open();
             var statement = connection.prepareStatement(FIND_ALL_SQL);
             var resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                currencyList.add(mapRow(resultSet));
            }
        } catch (SQLException e) {
            throw SqlExceptionMapper.map(e);
        }
        return currencyList;
    }

    @Override
    public Optional<Currency> findByKey(Long key) {
        try (var connection = ConnectionManager.open();
             var statement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            statement.setLong(1, key);
            try (var resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw SqlExceptionMapper.map(e);
        }
    }

    @Override
    public Currency save(Currency entity) {
        try (var connection = ConnectionManager.open();
             var statement = connection.prepareStatement(
                     SAVE_SQL, Statement.RETURN_GENERATED_KEYS)) {
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
            throw SqlExceptionMapper.map(e);
        }
    }

    @Override
    public boolean delete(Long id) {
        try (var connection = ConnectionManager.open();
             var statement = connection.prepareStatement(DELETE_SQL)) {
            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<Currency> findByKey(String currencyCode) {
        try (var connection = ConnectionManager.open();
        var statement = connection.prepareStatement(FIND_BY_CODE_SQL)) {
            statement.setString(1, currencyCode);

            try (var resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
            }
        } catch (SQLException e) {
            throw SqlExceptionMapper.map(e);
        }
        return Optional.empty();
    }

    private Currency mapRow(ResultSet rs) throws SQLException {
        return new Currency(
                rs.getLong("Id"),
                rs.getString("Code"),
                rs.getString("FullName"),
                rs.getString("Sign")
        );
    }

    private CurrencyDao() {
    }

    public static CurrencyDao getInstance() {
        return INSTANCE;
    }
}
