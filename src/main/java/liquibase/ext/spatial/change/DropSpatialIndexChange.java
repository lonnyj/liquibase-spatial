package liquibase.ext.spatial.change;

import liquibase.change.AbstractChange;
import liquibase.database.Database;
import liquibase.statement.SqlStatement;

/**
 * @author Lonny Jacobson
 */
public class DropSpatialIndexChange extends AbstractChange {

   /**
    * @see liquibase.change.Change#getConfirmationMessage()
    */
   public String getConfirmationMessage() {
      // TODO Auto-generated method stub
      return null;
   }

   /**
    * @see liquibase.change.Change#generateStatements(liquibase.database.Database)
    */
   public SqlStatement[] generateStatements(Database database) {
      // TODO Auto-generated method stub
      return null;
   }

}
