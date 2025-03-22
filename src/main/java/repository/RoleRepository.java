package repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.jboss.logging.Logger;

import database.Datasources;
import model.Role;

@ApplicationScoped
public class RoleRepository {

    @Inject
    Logger logger;

    @Inject
    Datasources dataSource;

    public Role get(int roleId) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM ROLE_TL WHERE role_id = ?")) {

            preparedStatement.setInt(1, roleId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return new Role(resultSet.getInt("role_id"), resultSet.getString("role_type"));
                }
            }
        }  
        catch (Exception e) {
            logger.error("Unexpected error occurred while retrieving role with ID " + roleId, e);
        }
        return null; 
    }

    public List<Role> getAll() {
        List<Role> roles = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM ROLE_TL")) {

            while (resultSet.next()) {
                roles.add(new Role(resultSet.getInt("role_id"), resultSet.getString("role_type")));
            }
        } catch (SQLException e) {
            logger.error("Unexpected error occurred while retrieving roles", e);
        }
        return roles;
    }

    public Role create(Role role) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO ROLE_TL (role_id, role_type) VALUES (?, ?)")) {

            preparedStatement.setInt(1, role.getRoleId());
            preparedStatement.setString(2, role.getRoleType());

            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows > 0) {
                return role;
            }
        } catch (SQLException e) {
            logger.error("Unexpected error occurred while creating role", e);
        }
        return null; // Or throw an exception
    }

    public Role update(Role role) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("UPDATE ROLE_TL SET role_type = ? WHERE role_id = ?")) {

            preparedStatement.setString(1, role.getRoleType());
            preparedStatement.setInt(2, role.getRoleId());

            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows > 0) {
                return role;
            }
        } catch (SQLException e) {
            logger.error("Unexpected error occurred while update role", e);
        }
        return null; // Or throw an exception
    }

    public boolean delete(int roleId) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM ROLE_TL WHERE role_id = ?")) {

            preparedStatement.setInt(1, roleId);
            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            logger.error("Unexpected error occurred while deleting role", e);
        }
        return false;
    }
}