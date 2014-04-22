package liquibase.ext.spatial;

import java.sql.SQLException;

import org.h2.tools.DeleteDbFiles;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

/**
 * <code>LiquibaseH2IT</code> is an integration test of Liquibase with H2.
 */
public class LiquibaseH2IT extends LiquibaseIT {
   @Override
   protected String getUrl() {
      return "jdbc:h2:./target/" + getDatabaseName();
   }

   @BeforeMethod
   public void beforeMethod() throws SQLException {
      DeleteDbFiles.execute("./target", getDatabaseName(), true);
      getConnection().close();
   }

   @AfterMethod
   public void afterMethod() throws SQLException {
      DeleteDbFiles.execute("./target", getDatabaseName(), true);
   }
}
