package ru.arthu.currencyexchange.dao;

import ru.arthu.currencyexchange.model.Currency;
import ru.arthu.currencyexchange.model.ExchangeRate;
import ru.arthu.currencyexchange.utils.ConnectionManager;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
            UPDATE ExchangeRates SET BaseCurrencyId = ?, TargetCurrencyId = ?, Rate = ?
                                 WHERE ID = ?;
            """;

    @Override
    public boolean update(ExchangeRate entity) {

        try (var connection = ConnectionManager.open();
        var statement = connection.prepareStatement(UPDATE_SQL)) {
            statement.setLong(1, entity.getBaseCurrency().getId());
            statement.setLong(2, entity.getTargetCurrency().getId());
            statement.setBigDecimal(3, entity.getRate());
            statement.setLong(4, entity.getId());
            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ExchangeRate> findAll() {

        try (var connection = ConnectionManager.open();
             var statement = connection.prepareStatement(FIND_ALL_SQL)) {
            List<ExchangeRate> exchangeRates = new ArrayList<>();
            try (var result = statement.executeQuery()) {
                while (result.next()) {
                    exchangeRates.add(
                            new ExchangeRate(
                                    result.getLong("ExchangeRateId"),
                                    new Currency(
                                            result.getLong("BaseCurrencyId"),
                                            result.getString("BaseCurrencyCode"), result.getString("BaseCurrencyName"),
                                            result.getString("BaseCurrencySign")
                                    ), new Currency(
                                    result.getLong("TargetCurrencyId"),
                                    result.getString("TargetCurrencyCode"), result.getString("TargetCurrencyName"),
                                    result.getString("TargetCurrencySign")
                            ),
                                    result.getBigDecimal("Rate")
                            )
                    );
                }
            }
            return exchangeRates;
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
                                    resultSet.getString("BaseCurrencyName"), resultSet.getString("BaseCurrencyCode"),
                                    resultSet.getString("BaseCurrencySign")
                            ),
                            new Currency(
                                    resultSet.getLong("TargetCurrencyId"),
                                    resultSet.getString("TargetCurrencyName"), resultSet.getString("TargetCurrencyCode"),
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
    public ExchangeRate save(ExchangeRate entity) {
        try (var connection = ConnectionManager.open();
             var statement = connection.prepareStatement(SAVE_SQL, Statement.RETURN_GENERATED_KEYS)) {
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
            throw new RuntimeException(e);
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

    private ExchangeRateDao() {
    }

    public static ExchangeRateDao getInstance() {
        return INSTANCE;
    }
}
