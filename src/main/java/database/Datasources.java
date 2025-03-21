package database;

import io.agroal.api.AgroalDataSource;
import io.quarkus.agroal.DataSource;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import java.sql.Connection;
import java.sql.SQLException;

@RequestScoped
public class Datasources {

    @Inject
    AgroalDataSource conventionalDataSource;

    @Inject
    Logger logger;


    public Connection getConnection() {
        try
        {
            return conventionalDataSource.getConnection();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}
