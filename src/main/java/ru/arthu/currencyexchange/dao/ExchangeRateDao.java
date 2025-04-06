package ru.arthu.currencyexchange.dao;

import java.math.BigDecimal;

import org.sqlite.SQLiteException;
import ru.arthu.currencyexchange.dto.ExchangeRateFilter;
import ru.arthu.currencyexchange.exceptions.ExchangeAlreadyExistException;
import ru.arthu.currencyexchange.model.Currency;
import ru.arthu.currencyexchange.model.ExchangeRate;
import ru.arthu.currencyexchange.utils.ConnectionManager;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ExchangeRateDao implements Dao<Long, ExchangeRate> {

    private static final ExchangeRateDao INSTANCE = new ExchangeRateDao();

    private static final String SAVE_SQL = """
        INSERT INTO ExchangeRates (BaseCurrencyId, TargetCurrencyId, Rate)
        VALUES (?, ?, ?);
        """;

    private static final String DELETE_SQL = """
        DELETE FROM ExchangeRates
        WHERE ID = ?;
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

    private static final String FIND_BY_ID_SQL = FIND_ALL_SQL + " WHERE ExchangeRateId = ?";

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

    @Override
    public boolean update(ExchangeRate entity) {

        try (var connection = ConnectionManager.open();
            var statement = connection.prepareStatement(UPDATE_SQL)) {
            statement.setLong(1, entity.getBaseCurrency().getId());
            statement.setLong(2, entity.getTargetCurrency().getId());
            statement.setBigDecimal(3, entity.getRate());
            statement.setLong(4, entity.getId());
            System.out.println(statement);
            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<ExchangeRate> findAll(ExchangeRateFilter exchangeRateFilter) {

        List<Object> params = new ArrayList<>();
        List<String> whereSql = new ArrayList<>();

        if (exchangeRateFilter.baseCurrencyId() != null) {
            params.add(exchangeRateFilter.baseCurrencyId());
            whereSql.add("BaseCurrencyId = ?");
        }
        if (exchangeRateFilter.targetCurrencyId() != null) {
            params.add(exchangeRateFilter.targetCurrencyId());
            whereSql.add("TargetCurrencyId = ?");
        }
        params.add(exchangeRateFilter.limit());
        params.add(exchangeRateFilter.offset());

        String where = whereSql.stream().collect(Collectors.joining(
            " AND ",
            params.size() > 2 ? " WHERE " : " ",
            " LIMIT ? OFFSET ?"
        ));
        String sql = FIND_ALL_SQL + where;
        try (var connection = ConnectionManager.open();
            var statement = connection.prepareStatement(sql)) {
            List<ExchangeRate> exchangeRates = new ArrayList<>();
            for (int i = 0; i < params.size(); i++) {
                statement.setObject(i + 1, params.get(i));
            }
            return getExchangeRates(statement, exchangeRates);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private List<ExchangeRate> getExchangeRates(PreparedStatement statement,
        List<ExchangeRate> exchangeRates) throws SQLException, SQLiteException {
        try (var result = statement.executeQuery()) {
            while (result.next()) {
                exchangeRates.add(
                    new ExchangeRate(
                        result.getLong("ExchangeRateId"),
                        new Currency(
                            result.getLong("BaseCurrencyId"),
                            result.getString("BaseCurrencyCode"),
                            result.getString("BaseCurrencyName"),
                            result.getString("BaseCurrencySign")
                        ), new Currency(
                        result.getLong("TargetCurrencyId"),
                        result.getString("TargetCurrencyCode"),
                        result.getString("TargetCurrencyName"),
                        result.getString("TargetCurrencySign")
                    ),
                        result.getBigDecimal("Rate")
                    )
                );
            }
        }
        return exchangeRates;
    }

    @Override
    public List<ExchangeRate> findAll() {

        try (var connection = ConnectionManager.open();
            var statement = connection.prepareStatement(FIND_ALL_SQL)) {
            List<ExchangeRate> exchangeRates = new ArrayList<>();
            return getExchangeRates(statement, exchangeRates);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<ExchangeRate> findByKey(Long key) {
        try (var connection = ConnectionManager.open();
            var statement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            statement.setLong(1, key);

            try (var resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(new ExchangeRate(
                        resultSet.getLong("ExchangeRateId"),
                        new Currency(
                            resultSet.getLong("BaseCurrencyId"),
                            resultSet.getString("BaseCurrencyName"),
                            resultSet.getString("BaseCurrencyCode"),
                            resultSet.getString("BaseCurrencySign")
                        ),
                        new Currency(
                            resultSet.getLong("TargetCurrencyId"),
                            resultSet.getString("TargetCurrencyName"),
                            resultSet.getString("TargetCurrencyCode"),
                            resultSet.getString("TargetCurrencySign")
                        ),
                        resultSet.getBigDecimal("Rate")
                    ));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public ExchangeRate save(ExchangeRate entity) throws ExchangeAlreadyExistException {
        try (var connection = ConnectionManager.open();
            var statement = connection.prepareStatement(SAVE_SQL,
                Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, entity.getBaseCurrency().getId());
            statement.setLong(2, entity.getTargetCurrency().getId());
            statement.setBigDecimal(3, entity.getRate());
            statement.executeUpdate();
            try (var generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    entity.setId(generatedKeys.getLong(1));
                }
            }
            return entity;
        } catch (SQLException e) {
            throw new ExchangeAlreadyExistException(e);
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

    public BigDecimal getExchangeRate(Long baseCurrencyId, Long targetCurrencyId) throws SQLException {
        try (var connection = ConnectionManager.open()) {
            for (String sqlQuery : EXCHANGE_SCENARIOS) {
                try (var statement = connection.prepareStatement(sqlQuery)) {
                    statement.setLong(2, baseCurrencyId);
                    statement.setLong(1, targetCurrencyId);
                    try (var resultSet = statement.executeQuery()) {
                        if (resultSet.next()) {
                            System.out.println(statement);
                            System.out.println(resultSet.getBigDecimal(1));
                            return resultSet.getBigDecimal(1);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new SQLException(e);
        }
        return BigDecimal.valueOf(-1);
    }


    private ExchangeRateDao() {
    }

    public static ExchangeRateDao getInstance() {
        return INSTANCE;
    }
}
