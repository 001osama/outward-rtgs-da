package service;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import model.Role;
import org.jboss.logging.Logger;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.sql.ResultSet;
import database.Datasources;

@RequestScoped
public class RoleService {

    @Inject
    Logger logger;

    @Inject
    Datasources datasources;


    List<Role> response = new ArrayList<>();
    public List<Role> getDetail() 
    {
        try
        {
            logger.info("message : =====> in ITO Batch Service getDetail()");
            
            Connection connection = datasources.getConnection();
            Statement statement = connection.createStatement();
            ResultSet resultset = statement.executeQuery("SELECT * FROM ROLE_TL");
            
            while (resultset.next()) {
                int roleId = resultset.getInt("role_id");
                String roleType = resultset.getString("role_type");

                Role role = new Role(roleId, roleType); 
                response.add(role);
            }

            logger.info(response);
        }
        catch(Exception e)
        {
            logger.info(e);
        }

        return response;
    }
}
