package service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.jboss.logging.Logger;
import database.Datasources;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import model.User;


@ApplicationScoped
public class UserService {
    
    @Inject
    Logger logger;

    @Inject
    Datasources dataSource;


    public User get(String userName) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM USER_TL WHERE user_name = ?")) {

            preparedStatement.setString(1, userName);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) 
                {
                    return new User(
                        resultSet.getString("user_name"),
                        resultSet.getInt("role_id"), 
                        resultSet.getString("branch_cd"),
                        resultSet.getString("password")
                    );
                }
            }
        }  
        catch (Exception e) {
            logger.error("Unexpected error occurred while retrieving user with username " + userName, e);
        }
        return null; 
    }

    public User create(User user) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO USER_TL(user_name, role_id, branch_cd, password) VALUES (?, ?, ?, ?)")) {

            preparedStatement.setString(1, user.getUsername());
            preparedStatement.setInt(2, user.getRoleId());
            preparedStatement.setBigDecimal(3, new BigDecimal(user.getBranchCode()));
            preparedStatement.setString(4, user.getPassword());

            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows > 0) {
                return user;
            }
        } catch (SQLException ex) {
            logger.error("Unexpected error occurred while creating role", ex);
        }
        return null;
    }

}
