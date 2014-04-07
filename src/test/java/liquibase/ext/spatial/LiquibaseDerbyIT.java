package liquibase.ext.spatial;

import java.sql.DriverManager;
import java.sql.SQLException;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

/**
 * <code>LiquibaseH2IT</code> is an integration test of Liquibase with Derby.
 */
public class LiquibaseDerbyIT extends LiquibaseIT {
   /**
    * The database sub-subprotocol (e.g. <code>memory:</code>) immediately after which the database
    * name may appear.
    * 
    * @return the database sub-subprotocol.
    */
   protected String getDatabaseSubSubProtocol() {
      return "memory:";
   }

   @Override
   protected String getUrl() {
      return "jdbc:derby:" + getDatabaseSubSubProtocol() + getDatabaseName();
   }

   @BeforeMethod
   public void createDatabase() throws SQLException {
      DriverManager.getConnection(getUrl() + ";create=true").close();
   }

   @AfterMethod
   public void dropDatabase() throws SQLException {
      try {
         DriverManager.getConnection(getUrl() + ";drop=true").close();
      } catch (final Exception ignore) {
      }
   }
}
