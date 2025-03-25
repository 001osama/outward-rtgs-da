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


    public User get(String loginId) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM USER_TL WHERE login_id = ?")) {

            preparedStatement.setString(1, loginId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) 
                {
                    return new User(
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

    public User create(User user) {
        try (Connection connection = dataSource.getConnection();
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

}
