package liquibase.ext.spatial.change;

import liquibase.change.core.CreateIndexChange;
import liquibase.database.Database;
import liquibase.ext.spatial.xml.XmlConstants;
import liquibase.statement.SqlStatement;

/**
 * The <code>CreateSpatialIndexChange</code>....
 */
public class CreateSpatialIndexChange extends CreateIndexChange {
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
      return null;
   }

   @Override
   public String getSerializedObjectNamespace() {
      return XmlConstants.SPATIAL_CHANGELOG_NAMESPACE;
   }
}
