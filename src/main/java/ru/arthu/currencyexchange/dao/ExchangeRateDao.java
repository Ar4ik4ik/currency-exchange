package ru.arthu.currencyexchange.dao;

import java.math.BigDecimal;

import org.apache.commons.lang3.tuple.Pair;
import ru.arthu.currencyexchange.exceptions.CannotUpdateException;
import ru.arthu.currencyexchange.exceptions.ExchangeRateNotFoundException;
import ru.arthu.currencyexchange.utils.mappers.SqlExceptionMapper;
import ru.arthu.currencyexchange.model.Currency;
import ru.arthu.currencyexchange.model.ExchangeRate;
import ru.arthu.currencyexchange.utils.ConnectionManager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;


public class ExchangeRateDao implements UpdatableDao<ExchangeRate, Pair<String, String>> {

    private static final ExchangeRateDao INSTANCE = new ExchangeRateDao();

    private static final String SAVE_SQL = """
        INSERT INTO ExchangeRates (BaseCurrencyId, TargetCurrencyId, Rate)
        VALUES (?, ?, ?);
        """;

    private static final String FIND_ALL_SQL = """
        SELECT e.Id AS ExchangeRateId,
               e.BaseCurrencyId, e.TargetCurrencyId, e.Rate,
               c.Code AS BaseCurrencyCode, c.FullName AS BaseCurrencyName,c.Sign AS BaseCurrencySign,
               c2.Code AS TargetCurrencyCode, c2.FullName AS TargetCurrencyName, c2.Sign AS TargetCurrencySign
        FROM ExchangeRates AS e
        JOIN Currencies AS c ON c.Id = e.BaseCurrencyId
        JOIN Currencies AS c2 ON c2.Id = e.TargetCurrencyId
        """;

    private static final String FIND_BY_CODE_PAIR = FIND_ALL_SQL +
            " WHERE BaseCurrencyCode = ? AND TargetCurrencyCode = ?";

    private static final String UPDATE_SQL = """
        UPDATE OR REPLACE ExchangeRates SET BaseCurrencyId = ?, TargetCurrencyId = ?, Rate = ?
                             WHERE ID = ?;
        """;

    private static final String[] EXCHANGE_SCENARIOS = new String[]{
        "SELECT Rate FROM ExchangeRates WHERE TargetCurrencyId = ? AND BaseCurrencyId = ?;",
        "SELECT 1.0 / Rate FROM ExchangeRates WHERE BaseCurrencyId = ? AND TargetCurrencyId = ?;",
        "SELECT e1.Rate / e2.Rate FROM ExchangeRates e1"
            + " JOIN ExchangeRates e2 ON e1.BaseCurrencyId = e2.BaseCurrencyId"
            + " WHERE e1.TargetCurrencyId = ? AND e2.TargetCurrencyId = ?;"
    };

    public boolean update(ExchangeRate entity) {

        try (var connection = ConnectionManager.open();
            var statement = connection.prepareStatement(UPDATE_SQL)) {
            setStatementParams(statement, entity);
            statement.setLong(4, entity.id());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw SqlExceptionMapper.map(e);
        }
    }

    @Override
    public List<ExchangeRate> findAll() {

        try (var connection = ConnectionManager.open();
            var statement = connection.prepareStatement(FIND_ALL_SQL)) {
            return mapResultSetToExchangeRates(statement);
        } catch (SQLException e) {
            throw SqlExceptionMapper.map(e);
        }
    }

    public Optional<ExchangeRate> findByKey(Pair<String, String> codePair) {
        return findOne(FIND_BY_CODE_PAIR, prepareByCodes(codePair.getLeft(), codePair.getRight()));
    }

    private List<ExchangeRate> mapResultSetToExchangeRates(PreparedStatement statement) throws SQLException {
        List<ExchangeRate> exchangeRates = new ArrayList<>();
        try (var result = statement.executeQuery()) {
            while (result.next()) {
                exchangeRates.add(mapRow(result));
            }
        }
        return exchangeRates;
    }

    private Consumer<PreparedStatement> prepareByCodes(String baseCode, String targetCode) {
        return statement -> {
            try {
                statement.setString(1, baseCode);
                statement.setString(2, targetCode);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };
    }

    private Optional<ExchangeRate> findOne(String sql, Consumer<PreparedStatement> preparer) {
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


    @Override
    public ExchangeRate save(ExchangeRate entity) {
        try (var connection = ConnectionManager.open();
            var statement = connection.prepareStatement(SAVE_SQL,
                Statement.RETURN_GENERATED_KEYS)) {

            setStatementParams(statement, entity);
            statement.executeUpdate();

            try (var generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return new ExchangeRate(
                            generatedKeys.getLong(1),
                            entity.baseCurrency(),
                            entity.targetCurrency(),
                            entity.exchangeRate());
                }
            }
            throw new CannotUpdateException();
        } catch (SQLException e) {
            throw SqlExceptionMapper.map(e);
        }
    }

    public BigDecimal getExchangeRate(Long baseCurrencyId, Long targetCurrencyId) {
        try (var connection = ConnectionManager.open()) {
            // SCENARIOS = AB; BA; BASE-A and BASE-B = AB
            for (String sqlQuery : EXCHANGE_SCENARIOS) {
                try (var statement = connection.prepareStatement(sqlQuery)) {
                    // Target and base are swapped to simplify the use of SQL
                    statement.setLong(1, targetCurrencyId);
                    statement.setLong(2, baseCurrencyId);
                    try (var resultSet = statement.executeQuery()) {
                        if (resultSet.next()) {
                            return resultSet.getBigDecimal(1);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw SqlExceptionMapper.map(e);
        }
        throw new ExchangeRateNotFoundException("");
    }

    private ExchangeRate mapRow(ResultSet rs) throws SQLException {
        return new ExchangeRate(
                rs.getLong("ExchangeRateId"),
                new Currency(
                        rs.getLong("BaseCurrencyId"),
                        rs.getString("BaseCurrencyCode"),
                        rs.getString("BaseCurrencyName"),
                        rs.getString("BaseCurrencySign")
                ),
                new Currency(
                        rs.getLong("TargetCurrencyId"),
                        rs.getString("TargetCurrencyCode"),
                        rs.getString("TargetCurrencyName"),
                        rs.getString("TargetCurrencySign")
                ),
                rs.getBigDecimal("Rate")
        );
    }

    private static void setStatementParams(PreparedStatement statement, ExchangeRate entity) throws SQLException {
        statement.setLong(1, entity.baseCurrency().id());
        statement.setLong(2, entity.targetCurrency().id());
        statement.setBigDecimal(3, entity.exchangeRate());
    }

    private ExchangeRateDao() {
    }

    public static ExchangeRateDao getInstance() {
        return INSTANCE;
    }
}
