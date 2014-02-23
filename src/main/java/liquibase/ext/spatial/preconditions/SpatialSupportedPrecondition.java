package liquibase.ext.spatial.preconditions;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.exception.PreconditionErrorException;
import liquibase.exception.PreconditionFailedException;
import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
import liquibase.precondition.Precondition;

/**
 * @author Lonny Jacobson
 */
public class SpatialSupportedPrecondition implements Precondition {

   /**
    * @see liquibase.precondition.Precondition#getName()
    */
   public String getName() {
      // TODO Auto-generated method stub
      return null;
   }

   /**
    * @see liquibase.precondition.Precondition#warn(liquibase.database.Database)
    */
   public Warnings warn(Database database) {
      // TODO Auto-generated method stub
      return null;
   }

   /**
    * @see liquibase.precondition.Precondition#validate(liquibase.database.Database)
    */
   public ValidationErrors validate(Database database) {
      // TODO Auto-generated method stub
      return null;
   }

   /**
    * @see liquibase.precondition.Precondition#check(liquibase.database.Database, liquibase.changelog.DatabaseChangeLog, liquibase.changelog.ChangeSet)
    */
   public void check(Database database, DatabaseChangeLog changeLog,
         ChangeSet changeSet) throws PreconditionFailedException,
         PreconditionErrorException {
      // TODO Auto-generated method stub

   }

}
