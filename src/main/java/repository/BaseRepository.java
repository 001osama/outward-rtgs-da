package repository;

import database.Datasources;
import jakarta.inject.Inject;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public abstract class BaseRepository<T> {

    @Inject
    protected Datasources dataSource;

    protected abstract T mapResultSet(ResultSet resultSet) throws SQLException;

    protected List<T> executeQuery(String query, Function<PreparedStatement, Void> paramSetter) {
        List<T> results = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            if (paramSetter != null) paramSetter.apply(preparedStatement);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    results.add(mapResultSet(resultSet));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

    protected boolean executeUpdate(String query, Function<PreparedStatement, Void> paramSetter) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                
            if (paramSetter != null) paramSetter.apply(preparedStatement);

            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
