package liquibase.ext.spatial.preconditions;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.database.core.DerbyDatabase;
import liquibase.database.core.H2Database;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.exception.LiquibaseException;
import liquibase.exception.PreconditionErrorException;
import liquibase.exception.PreconditionFailedException;
import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
import liquibase.precondition.Precondition;
import liquibase.precondition.core.ErrorPrecondition;
import liquibase.precondition.core.TableExistsPrecondition;
import liquibase.precondition.core.ViewExistsPrecondition;

/**
 * <code>SpatialSupportedPrecondition</code> checks the state of the database and determines if it
 * has spatial support.
 * 
 * @author Lonny Jacobson
 */
public class SpatialSupportedPrecondition implements Precondition {
   @Override
   public String getName() {
      return "spatialSupported";
   }

   @Override
   public Warnings warn(final Database database) {
      final Warnings warnings = new Warnings();
      if (!(database instanceof DerbyDatabase || database instanceof H2Database
            || database instanceof MySQLDatabase || database instanceof OracleDatabase || database instanceof PostgresDatabase)) {
         warnings.addWarning(database.getDatabaseProductName()
               + " is not supported by this extension");
      }
      return warnings;
   }

   @Override
   public ValidationErrors validate(final Database database) {
      final ValidationErrors errors = new ValidationErrors();
      if (!(database instanceof DerbyDatabase || database instanceof H2Database
            || database instanceof MySQLDatabase || database instanceof OracleDatabase || database instanceof PostgresDatabase)) {
         errors.addError(database.getDatabaseProductName() + " is not supported by this extension");
      }
      return errors;
   }

   @Override
   public void check(final Database database, final DatabaseChangeLog changeLog,
         final ChangeSet changeSet) throws PreconditionFailedException, PreconditionErrorException {
      if (database instanceof DerbyDatabase || database instanceof H2Database) {
         final TableExistsPrecondition precondition = new TableExistsPrecondition();
         precondition.setTableName("geometry_columns");
         precondition.check(database, changeLog, changeSet);
      } else if (database instanceof PostgresDatabase) {
         final ViewExistsPrecondition precondition = new ViewExistsPrecondition();
         precondition.setViewName("geometry_columns");
         precondition.check(database, changeLog, changeSet);
      } else if (database instanceof OracleDatabase) {
         final ViewExistsPrecondition precondition = new ViewExistsPrecondition();
         precondition.setViewName("user_sdo_geom_metadata");
         precondition.check(database, changeLog, changeSet);
      } else if (!(database instanceof MySQLDatabase)) {
         final Throwable exception = new LiquibaseException(database.getDatabaseProductName()
               + " is not supported by this extension");
         final ErrorPrecondition errorPrecondition = new ErrorPrecondition(exception, changeLog,
               this);
         throw new PreconditionErrorException(errorPrecondition);
      }
   }
}
