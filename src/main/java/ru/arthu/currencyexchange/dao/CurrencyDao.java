package ru.arthu.currencyexchange.dao;

import ru.arthu.currencyexchange.exceptions.CannotSaveException;
import ru.arthu.currencyexchange.utils.mappers.SqlExceptionMapper;
import ru.arthu.currencyexchange.model.Currency;
import ru.arthu.currencyexchange.utils.ConnectionManager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class CurrencyDao implements Dao<Currency> {

    private static final CurrencyDao INSTANCE = new CurrencyDao();

    private static final String SAVE_SQL = """
            INSERT INTO Currencies (Code, FullName, Sign)
            VALUES (?, ?, ?);
            """;

    private static final String FIND_ALL_SQL = """
            SELECT Id, Code, FullName, Sign
            FROM Currencies
            """;

    private static final String FIND_BY_CODE_SQL = """
            SELECT Id, Code, FullName, Sign FROM main.Currencies
            WHERE Code = ?;
            """;

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

    private Optional<Currency> findOne(String sql, Consumer<PreparedStatement> preparer) {
        try (var connection = ConnectionManager.open();
             var statement = connection.prepareStatement(sql)) {

            preparer.accept(statement);

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

    public Optional<Currency> findByKey(String code) {
        return findOne(FIND_BY_CODE_SQL, stmt -> {
            try {
                stmt.setString(1, code);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public Currency save(Currency entity) {
        try (var connection = ConnectionManager.open();
             var statement = connection.prepareStatement(
                     SAVE_SQL, Statement.RETURN_GENERATED_KEYS)) {
            setStatementParams(statement, entity);
            statement.executeUpdate();

            try (var generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return new Currency(
                            generatedKeys.getLong(1),
                            entity.code(),
                            entity.fullName(),
                            entity.sign());
                }
            }
            throw new CannotSaveException();
        } catch (SQLException e) {
            throw SqlExceptionMapper.map(e);
        }
    }

    private Currency mapRow(ResultSet rs) throws SQLException {
        return new Currency(
                rs.getLong("Id"),
                rs.getString("Code"),
                rs.getString("FullName"),
                rs.getString("Sign")
        );
    }

    private static void setStatementParams(PreparedStatement statement,Currency entity) throws SQLException {
        statement.setString(1, entity.code());
        statement.setString(2, entity.fullName());
        statement.setString(3, entity.sign());
    }

    private CurrencyDao() {
    }

    public static CurrencyDao getInstance() {
        return INSTANCE;
    }
}
