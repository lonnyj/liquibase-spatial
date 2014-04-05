package liquibase.ext.spatial;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.h2.tools.DeleteDbFiles;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

/**
 * <code>LiquibaseH2IT</code> is an integration test of Liquibase with H2.
 */
public class LiquibaseH2IT extends LiquibaseIT {
   @BeforeMethod
   public void beforeClass() throws SQLException {
      DeleteDbFiles.execute("target", getDatabaseName(), true);
      DriverManager.getConnection("jdbc:h2:target/" + getDatabaseName()).close();
   }

   @AfterMethod
   public void afterClass() throws SQLException {
      DeleteDbFiles.execute("target", getDatabaseName(), true);
   }

   /**
    * @see liquibase.ext.spatial.LiquibaseIT#getConnection()
    */
   @Override
   protected Connection getConnection() throws SQLException {
      return DriverManager.getConnection("jdbc:h2:target/" + getDatabaseName());
   }
}
