package service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
// import java.sql.SQLException;
// import java.sql.Statement;
// import java.util.ArrayList;
// //import java.util.List;

import org.jboss.logging.Logger;
import database.Datasources;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import model.Customer;

@ApplicationScoped
public class CustomerService {
    @Inject
    Logger logger;

    @Inject
    Datasources dataSource;

    public Customer get(int customerNo) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM CUSTOMER_TL WHERE customer_no = ?")) {

            preparedStatement.setInt(1, customerNo);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return new Customer(resultSet.getInt("customer_no"), resultSet.getString("customer_name"),resultSet.getString("address"),resultSet.getString("mobile"));
                }
            }
        }  
        catch (Exception e) {
            logger.error("Unexpected error occurred while retrieving account with customer no. " + customerNo, e);
        }
        return null; 
    }
}
