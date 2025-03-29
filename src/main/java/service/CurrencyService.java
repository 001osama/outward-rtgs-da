package service;

import java.sql.Connection;
//import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.jboss.logging.Logger;
import database.Datasources;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import model.Currency;

@ApplicationScoped
public class CurrencyService {
    @Inject
    Logger logger;

    @Inject
    Datasources dataSource;


    public List<Currency> getAll() {
        List<Currency> currency = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM CURRENCY_TL")) {

            while (resultSet.next()) {
                currency.add(new Currency(resultSet.getString("currency_cd"), resultSet.getString("currency_type")));
            }
        } catch (SQLException e) {
            logger.error("Unexpected error occurred while accessing currency", e);
        }
        return currency;
    }
    
}
