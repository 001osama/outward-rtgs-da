package database;

import java.sql.SQLException;

import org.jboss.logging.Logger;

import com.ibm.db2.jcc.DB2Diagnosable;
import com.ibm.db2.jcc.DB2Sqlca;

public class DatabaseErrorHandler {
    private static final Logger logger = Logger.getLogger(DatabaseErrorHandler.class);

    public static void log(SQLException ex) throws Exception {
        try {
            String errorMessage = "Database error occurred: ";
            if (ex instanceof DB2Diagnosable) {
                DB2Sqlca sqlca = ((DB2Diagnosable) ex).getSqlca();
                if (sqlca != null) {
                    errorMessage += "SQLCODE = " + sqlca.getSqlCode() +
                                    ", SQLSTATE = " + sqlca.getSqlState() +
                                    ", Message = " + sqlca.getMessage();
                }
            }
            errorMessage += " " + ex.getMessage();
            logger.error(errorMessage, ex);
        } catch (Exception e) {
            logger.error("Failed to log database error", e);
            throw e;
        }
    }
}
