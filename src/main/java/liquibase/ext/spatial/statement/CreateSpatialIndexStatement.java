package liquibase.ext.spatial.statement;

import liquibase.statement.core.CreateIndexStatement;

/**
 * <code>CreateSpatialIndexStatement</code> represents a
 * <code>CREATE SPATIAL INDEX</code> statement.
 */
public class CreateSpatialIndexStatement extends CreateIndexStatement {

   private String tableSpace;

   /**
    * @param indexName
    * @param tableCatalogName
    * @param tableSchemaName
    * @param tableName
    * @param columns
    * @param tableSpace
    */
   public CreateSpatialIndexStatement(String indexName,
         String tableCatalogName, String tableSchemaName, String tableName,
         String[] columns, String tableSpace) {
      super(indexName, tableCatalogName, tableSchemaName, tableName, false,
            null, columns);
      this.tableSpace = tableSpace;
   }
}
