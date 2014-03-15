package liquibase.ext.spatial;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

/**
 * <code>LiquibaseH2IT</code> is an integration test of Liquibase with Derby.
 */
public class LiquibaseDerbyIT extends LiquibaseIT {
   @BeforeMethod
   public void createDatabase() throws SQLException {
      DriverManager.getConnection(
            "jdbc:derby:" + getDatabaseSubSubProtocol() + getDatabaseName() + ";create=true")
            .close();
   }

   @AfterMethod
   public void dropDatabase() throws SQLException {
      try {
         DriverManager.getConnection(
               "jdbc:derby:" + getDatabaseSubSubProtocol() + getDatabaseName() + ";drop=true")
               .close();
      } catch (final Exception ignore) {
      }
   }

   /**
    * The database sub-subprotocol (e.g. <code>memory:</code>) immediately after which the database
    * name may appear.
    * 
    * @return the database sub-subprotocol.
    */
   public static String getDatabaseSubSubProtocol() {
      return "memory:";
   }

   /**
    * @see liquibase.ext.spatial.LiquibaseIT#getConnection()
    */
   @Override
   protected Connection getConnection() throws SQLException {
      return DriverManager.getConnection("jdbc:derby:" + getDatabaseSubSubProtocol()
            + getDatabaseName());
   }
}
