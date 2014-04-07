package liquibase.ext.spatial;

/**
 * <code>LiquibaseOracleIT</code> is an integration test of Liquibase with Oracle.
 */
public class LiquibaseOracleIT extends LiquibaseIT {
   @Override
   protected String getUrl() {
      final String url = System.getProperty("oracle.url");
      if (url == null) {
         throw new IllegalStateException("The property 'oracle.url' must be defined");
      }
      return url;
   }

   @Override
   protected String getUserName() {
      final String username = System.getProperty("oracle.username");
      if (username == null) {
         throw new IllegalStateException("The property 'oracle.username' must be defined");
      }
      return username;
   }

   @Override
   protected String getPassword() {
      final String password = System.getProperty("oracle.password");
      if (password == null) {
         throw new IllegalStateException("The property 'oracle.password' must be defined");
      }
      return password;
   }
}
