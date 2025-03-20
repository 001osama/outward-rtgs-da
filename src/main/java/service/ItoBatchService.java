package service;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.*;
import database.Datasources;

@RequestScoped
public class ItoBatchService {

    @Inject
    Logger logger;

    @Inject
    Datasources datasources;


    String response;

    public String getDetail() 
    {
        try
        {
        logger.info("message : =====> in ITO Batch Service getDetail()");
        
        String branch_code= "";
        Connection connection = datasources.getConnection();
        Statement stmt = connection.createStatement();
        ResultSet result = stmt.executeQuery("select * from date_tl");
        if(result.next())
        {
            branch_code = result.getString("brn_cd");
        }

        response = "Hello RESTEasy-getDetail ===>" + branch_code;
        }
        catch(SQLException e){
            logger.info(e);
        }

        return response;
    }

    public String insertDetail()
    {
        logger.info("message : =====> in ITO Batch Service insertDetail()");
        response = "Hello RESTEasy-insertDetail";
        return "Hello RESTEasy-insertDetail";
    }

}
