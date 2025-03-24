package database;

import io.agroal.api.AgroalDataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import java.sql.Connection;
import java.sql.SQLException;

@ApplicationScoped  
public class Datasources {

    @Inject
    AgroalDataSource conventionalDataSource;

    @Inject
    Logger logger;

    public Connection getConnection() throws SQLException {
        if (conventionalDataSource == null) {
            logger.error("Database source is not available.");
            throw new SQLException("AgroalDataSource is null. Database connection cannot be established.");
        }

        Connection connection = conventionalDataSource.getConnection();
        if (connection == null) {
            logger.error("Failed to retrieve a database connection.");
            throw new SQLException("Failed to retrieve a database connection.");
        }

        return connection;
    }

}
