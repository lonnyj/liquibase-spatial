package liquibase.ext.spatial;

/**
 * <code>LiquibaseMySQLIT</code> is an integration test of Liquibase with MySQL.
 */
public class LiquibaseMySQLIT extends LiquibaseIT {
   @Override
   protected String getUrl() {
      final String url = System.getProperty("mysql.url");
      if (url == null) {
         throw new IllegalStateException("The property 'mysql.url' must be defined");
      }
      return url;
   }

   @Override
   protected String getUserName() {
      final String username = System.getProperty("mysql.username");
      if (username == null) {
         throw new IllegalStateException("The property 'mysql.username' must be defined");
      }
      return username;
   }

   @Override
   protected String getPassword() {
      final String password = System.getProperty("mysql.password");
      if (password == null) {
         throw new IllegalStateException("The property 'mysql.password' must be defined");
      }
      return password;
   }
}
