package liquibase.ext.spatial;

import liquibase.Liquibase;
import liquibase.database.DatabaseConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ResourceAccessor;

/**
 * <code>LiquibaseH2AsSAIT</code> is an integration test of Liquibase with H2 as the SA user.
 */
public class LiquibaseH2AsSAIT extends LiquibaseH2IT {
   @Override
   protected String getUrl() {
      return "jdbc:h2:./target/" + getDatabaseName()
            + ";INIT=CREATE SCHEMA IF NOT EXISTS sa\\;SET SCHEMA sa;SCHEMA_SEARCH_PATH=sa,public";
   }

   /**
    * @see liquibase.ext.spatial.LiquibaseIT#getUserName()
    */
   @Override
   protected String getUserName() {
      return "sa";
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
