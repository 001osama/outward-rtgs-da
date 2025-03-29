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
import model.SBPTranType;

@ApplicationScoped

public class SBPTranTypeService {
    @Inject
    Logger logger;

    @Inject
    Datasources dataSource;


    public List<SBPTranType> getAll() {
        List<SBPTranType> sbptrantype = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM SBP_TRANSACTION_TYPE_TL")) {

            while (resultSet.next()) {
                sbptrantype.add(new SBPTranType(resultSet.getInt("sbp_transaction_type_cd"), resultSet.getString("sbp_transaction_type")));
            }
        } catch (SQLException e) {
            logger.error("Unexpected error occurred while accessing transaction type", e);
        }
        return sbptrantype;
    }
    
}
