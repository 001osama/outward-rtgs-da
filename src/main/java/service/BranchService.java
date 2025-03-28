package service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.jboss.logging.Logger;
import database.Datasources;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import model.Branch;

@ApplicationScoped

public class BranchService {
    @Inject
    Logger logger;

    @Inject
    Datasources dataSource;

    public Branch get(int branchCd) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM BRANCH_TL WHERE branch_cd = ?")) {

            preparedStatement.setInt(1, branchCd);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return new Branch(resultSet.getInt("branch_cd"), resultSet.getString("branch_name"));
                }
            }
        }  
        catch (Exception e) {
            logger.error("Unexpected error occurred while retrieving branch with CD " + branchCd, e);
        }
        return null; 
    }

    public List<Branch> getAll() {
        List<Branch> branches = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM BRANCH_TL")) {

            while (resultSet.next()) {
                branches.add(new Branch(resultSet.getInt("branch_cd"), resultSet.getString("branch_name")));
            }
        } catch (SQLException e) {
            logger.error("Unexpected error occurred while retrieving branches", e);
        }
        return branches;
    }

    public Branch create(Branch branch) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO BRANCH_TL (branch_cd, branch_name) VALUES (?, ?)")) {

            preparedStatement.setInt(1, branch.getBranchCd());
            preparedStatement.setString(2, branch.getBranchName());

            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows > 0) {
                return branch;
            }
        } catch (SQLException e) {
            logger.error("Unexpected error occurred while creating branch", e);
        }
        return null;
    }

    public Branch update(Branch branch) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("UPDATE BRANCH_TL SET branch_name = ? WHERE branch_cd = ?")) {

            preparedStatement.setString(1, branch.getBranchName());
            preparedStatement.setInt(2, branch.getBranchCd());

            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows > 0) {
                return branch;
            }
        } catch (SQLException e) {
            logger.error("Unexpected error occurred while updating branch", e);
        }
        return null;
    }

    public boolean delete(int branchCd) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM BRANCH_TL WHERE branch_cd = ?")) {

            preparedStatement.setInt(1, branchCd);
            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            logger.error("Unexpected error occurred while deleting branch", e);
        }
        return false;
    }
}
