package repository;

import model.Role;
import jakarta.enterprise.context.ApplicationScoped;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class RoleRepository extends BaseRepository<Role> {

    @Override
    protected Role mapResultSet(ResultSet resultSet) throws SQLException {
        return new Role(resultSet.getInt("role_id"), resultSet.getString("role_type"));
    }

    public Optional<Role> findById(int roleId) {
        List<Role> roles = executeQuery("SELECT * FROM ROLE_TL WHERE role_id = ?",
            paramSetter -> { 
                try { 
                    paramSetter.setInt(1, roleId); 
                } 
                catch (SQLException e) {} 
                return null; 
            });

        return roles.isEmpty() ? Optional.empty() : Optional.of(roles.get(0));
    }

    public List<Role> findAll() {
        return executeQuery("SELECT * FROM ROLE_TL", null);
    }

    public boolean create(Role role) {
        return executeUpdate("INSERT INTO ROLE_TL (role_id, role_type) VALUES (?, ?)",
            paramSetter -> { 
                try { 
                    paramSetter.setInt(1, role.getRoleId()); 
                    paramSetter.setString(2, role.getRoleType()); 
                } 
                catch (SQLException e) {} 
                return null; 
            });
    }

    public boolean update(Role role) {
        return executeUpdate("UPDATE ROLE_TL SET role_type = ? WHERE role_id = ?",
            paramSetter -> { 
                try { 
                    paramSetter.setString(1, role.getRoleType()); 
                    paramSetter.setInt(2, role.getRoleId()); 
                } 
                catch (SQLException e) {} 
                return null; 
            });
    }

    public boolean delete(int roleId) {
        return executeUpdate("DELETE FROM ROLE_TL WHERE role_id = ?",
            paramSetter -> { 
                try { 
                    paramSetter.setInt(1, roleId); 
                } 
                catch (SQLException e) {} 
                return null; 
            });
    }
}
