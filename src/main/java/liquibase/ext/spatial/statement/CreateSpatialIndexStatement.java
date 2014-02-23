package liquibase.ext.spatial.statement;

import liquibase.statement.core.CreateIndexStatement;

/**
 * <code>CreateSpatialIndexStatement</code> represents a
 * <code>CREATE SPATIAL INDEX</code> statement.
 */
public class CreateSpatialIndexStatement extends CreateIndexStatement {

   /**
    * @param indexName
    * @param tableCatalogName
    * @param tableSchemaName
    * @param tableName
    * @param isUnique
    * @param associatedWith
    * @param columns
    */
   public CreateSpatialIndexStatement(String indexName,
         String tableCatalogName, String tableSchemaName, String tableName,
         Boolean isUnique, String associatedWith, String[] columns) {
      super(indexName, tableCatalogName, tableSchemaName, tableName, isUnique,
            associatedWith, columns);
   }
}
