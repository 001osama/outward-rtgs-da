package service;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import com.oracle.svm.core.annotate.Inject;
import database.Datasources;
import dto.AccountDetailDto;
import dto.AccountNumberDto;
import dto.BranchDto;
import dto.CurrencyDto;
import dto.SBPTranTypeDto;
import dto.UserDto;

import org.jboss.logging.Logger;

public class MainService {
    
    @Inject 
    Datasources datasource;

    @Inject
    Logger logger;

    public UserDto getUser(String loginId) {
        try (Connection connection = datasource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM USER_TL WHERE login_id = ?")) {

            preparedStatement.setString(1, loginId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) 
                {
                    return new UserDto(
                        resultSet.getString("login_id"),
                        resultSet.getInt("role_id"), 
                        resultSet.getString("branch_cd"),
                        resultSet.getString("password")
                    );
                }
            }
        }  
        catch (Exception e) {
            logger.error("Unexpected error occurred while retrieving user with loginId " + loginId, e);
        }
        return null; 
    }

    public UserDto createUser(UserDto user) {
        try (Connection connection = datasource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO USER_TL(login_id, role_id, branch_cd, password) VALUES (?, ?, ?, ?)")) {

            preparedStatement.setString(1, user.getLoginId());
            preparedStatement.setInt(2, user.getRoleId());
            preparedStatement.setBigDecimal(3, new BigDecimal(user.getBranchCode()));
            preparedStatement.setString(4, user.getPassword());

            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows > 0) {
                return user;
            }
        } catch (SQLException ex) {
            logger.error("Unexpected error occurred while creating user", ex);
        }
        return null;
    }


    public List<BranchDto> getAllBranches() {
        List<BranchDto> branches = new ArrayList<>();
        try (Connection connection = datasource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM BRANCH_TL")) {

            while (resultSet.next()) {
                branches.add(new BranchDto(resultSet.getInt("branch_cd"), resultSet.getString("branch_name")));
            }
        } catch (SQLException e) {
            logger.error("Unexpected error occurred while retrieving branches", e);
        }
        return branches;
    }


    public List<CurrencyDto> getAllCurrencies() {
        List<CurrencyDto> currency = new ArrayList<>();
        try (Connection connection = datasource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM CURRENCY_TL")) {

            while (resultSet.next()) {
                currency.add(new CurrencyDto(resultSet.getString("currency_cd"), resultSet.getString("currency_type")));
            }
        } catch (SQLException e) {
            logger.error("Unexpected error occurred while accessing currency", e);
        }
        return currency;
    }

    public List<SBPTranTypeDto> getAllSBPTranTypes() {
        List<SBPTranTypeDto> sbptrantype = new ArrayList<>();
        try (Connection connection = datasource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM SBP_TRANSACTION_TYPE_TL")) {

            while (resultSet.next()) {
                sbptrantype.add(new SBPTranTypeDto(resultSet.getInt("sbp_transaction_type_cd"), resultSet.getString("sbp_transaction_type")));
            }
        } catch (SQLException e) {
            logger.error("Unexpected error occurred while accessing transaction type", e);
        }
        return sbptrantype;
    }


    public List<String> getAllAccounts(String customerNo) {
        try (Connection connection = datasource.getConnection();
             PreparedStatement preparedStatement = connection
             //For ibm db2 testing remains
                // .prepareStatement("SELECT" + 
                //                         "LPAD(branch_cd, 4, '0') || '-' ||" + 
                //                         "LPAD(account_type_cd, 4, '0') || '-' ||" + 
                //                         "LPAD(customer_no, 6, '0') || '-' ||" + 
                //                         "LPAD(run_no, 4, '0') || '-' ||" + 
                //                         "check_digit AS account_number" +
                //                         "FROM account_tl;"))

                .prepareStatement("SELECT" +  
                                    "CONCAT(FORMAT(branch_cd, '0000'), '-', FORMAT(account_type_cd, '0000'), '-', FORMAT(customer_no, '000000'), '-', FORMAT(run_no, '0000'), '-', check_digit)" + 
                                    "AS account_number FROM account_tl where customer_no = ?;")
        ){
            preparedStatement.setString(1, customerNo);

            List<String> result = new ArrayList<String>();
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    result.add(resultSet.getString("account_no")) ;
                }
            }
            return result;
        }  
        catch (Exception e) {
            logger.error("Unexpected error occurred while retrieving branch with CD ", e);
        }
        return null; 
    }



    public AccountDetailDto getAccountDetails(AccountNumberDto accountNo) {
        StringBuilder query = new StringBuilder();
        query.append("SELECT ")
            .append("CONCAT(acctype.account_type, '-', acctype.account_type_cd) AS acc_type, ")
            .append("CONCAT('PK65BAHL', acc.branch_cd, acc.account_type_cd, acc.customer_no, acc.run_no, acc.check_digit) AS iban, ")
            .append("CONCAT(acc.branch_cd, '-', acc.account_type_cd, '-', acc.customer_no, '-', acc.run_no, '-', acc.check_digit, '-', acc.account_title) AS acc_name, ")
            .append("acc.available_bal ")
            .append("FROM account_tl AS acc ")
            .append("LEFT JOIN account_type_tl AS acctype ")
            .append("ON acc.account_type_cd = acctype.account_type_cd ")
            .append("WHERE acc.branch_cd = ? AND ")
            .append("acc.account_type_cd = ? AND ")
            .append("acc.customer_no = ? AND ")
            .append("acc.run_no = ? AND ")
            .append("acc.check_digit = ?;");

        String sqlQuery = query.toString();

        try (Connection connection = datasource.getConnection();
             PreparedStatement preparedStatement = connection
             .prepareStatement(sqlQuery))                                                  
        {    
            preparedStatement.setString(1, accountNo.getBranchCode());
            preparedStatement.setString(2, accountNo.getAccountType());
            preparedStatement.setString(3, accountNo.getCustomerNo());
            preparedStatement.setString(4, accountNo.getRunNo());
            preparedStatement.setString(5, accountNo.getCheckDigit());

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                   return new AccountDetailDto(
                        resultSet.getString("acc_type"),
                        resultSet.getString("iban"),
                        resultSet.getString("acc_name"),
                        resultSet.getString("available_bal")
                    );
                }
            }
            return null;
        }  
        catch (Exception e) {
            logger.error("Unexpected error occurred while retrieving branch with CD " + accountNo, e);
        }
        return null; 
    }

}
