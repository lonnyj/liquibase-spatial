package liquibase.ext.spatial.statement;

import liquibase.statement.core.DropIndexStatement;

/**
 * The <code>DropSpatialIndexStatement</code>....
 */
public class DropSpatialIndexStatement extends DropIndexStatement {

   /**
    * @param indexName
    * @param tableCatalogName
    * @param tableSchemaName
    * @param tableName
    * @param associatedWith
    */
   public DropSpatialIndexStatement(String indexName, String tableCatalogName,
         String tableSchemaName, String tableName, String associatedWith) {
      super(indexName, tableCatalogName, tableSchemaName, tableName,
            associatedWith);
   }
}
