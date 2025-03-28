package service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
//import java.sql.SQLException;
//import java.sql.Statement;
//import java.util.ArrayList;
//import java.util.List;
import java.util.List;

//import javax.print.DocFlavor.STRING;

import org.jboss.logging.Logger;
import database.Datasources;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
//import model.Account;


@ApplicationScoped
public class AccountService {
    @Inject
    Logger logger;

    @Inject
    Datasources dataSource;

    public List<String> getAccountNo(int customerNo) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement
             ("SELECT branch_cd || '-' || account_type_cd || '-' || customer_no || '-' || run_no || '-' || check_digit AS account_no FROM ACCOUNT_TL WHERE customer_no = ?")) {

            preparedStatement.setInt(1, customerNo);

            List<String> result = new ArrayList<String>();
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    result.add(resultSet.getString("account_no")) ;
                }
            }
            return result;
        }  
        catch (Exception e) {
            logger.error("Unexpected error occurred while retrieving branch with CD " + customerNo, e);
        }
        return null; 
    }
    


    // public List<String> getAccountDetails(String accountNo) {
    //     try (Connection connection = dataSource.getConnection();
    //          PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM ACCOUNT_TL" +
    //           "WHERE branch_cd = ? AND account_type_cd = ? AND customer_no = ? AND run_no = ? AND check_digit = ?")) {

    //         preparedStatement.setInt(1, accountNo);

    //         List<String> result = new ArrayList<String>();
    //         try (ResultSet resultSet = preparedStatement.executeQuery()) {
    //             while (resultSet.next()) {
    //                 result.add(resultSet.getString("account_no")) ;
    //             }
    //         }
    //         return result;
    //     }  
    //     catch (Exception e) {
    //         logger.error("Unexpected error occurred while retrieving branch with CD " + customerNo, e);
    //     }
    //     return null; 
    // }
}
