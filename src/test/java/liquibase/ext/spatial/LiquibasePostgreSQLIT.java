package liquibase.ext.spatial;

import liquibase.Liquibase;
import liquibase.database.DatabaseConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ResourceAccessor;

/**
 * <code>LiquibasePostgreSQLIT</code> is an integration test of Liquibase with PostgreSQL.
 */
public class LiquibasePostgreSQLIT extends LiquibaseIT {
   @Override
   protected String getUrl() {
      final String url = System.getProperty("postgresql.url");
      if (url == null) {
         throw new IllegalStateException("The property 'postgresql.url' must be defined");
      }
      return url;
   }

   @Override
   protected String getUserName() {
      final String username = System.getProperty("postgresql.username");
      if (username == null) {
         throw new IllegalStateException("The property 'postgresql.username' must be defined");
      }
      return username;
   }

   @Override
   protected String getPassword() {
      final String password = System.getProperty("postgresql.password");
      if (password == null) {
         throw new IllegalStateException("The property 'postgresql.password' must be defined");
      }
      return password;
   }

   /**
    * @see liquibase.ext.spatial.LiquibaseIT#createLiquibase(java.lang.String,
    *      liquibase.resource.ResourceAccessor, liquibase.database.DatabaseConnection)
    */
   @Override
   protected Liquibase createLiquibase(final String changeLogFile,
         final ResourceAccessor resourceAccessor, final DatabaseConnection databaseConnection)
         throws LiquibaseException {
      final Liquibase liquibase = super.createLiquibase(changeLogFile, resourceAccessor,
            databaseConnection);
      liquibase.getDatabase().setDefaultSchemaName(getUserName());
      return liquibase;
   }
}
