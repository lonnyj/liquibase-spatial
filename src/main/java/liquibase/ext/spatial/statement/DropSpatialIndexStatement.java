package liquibase.ext.spatial.statement;

import liquibase.statement.AbstractSqlStatement;

/**
 * <code>DropSpatialIndexStatement</code> represents a <code>DROP SPATIAL INDEX</code> statement.
 */
public class DropSpatialIndexStatement extends AbstractSqlStatement {
   /** The index name. */
   private final String indexName;

   /** The table catalog name. */
   private final String tableCatalogName;

   /** The table schema name. */
   private final String tableSchemaName;

   /** The table name. */
   private final String tableName;

   /**
    * @param indexName
    * @param tableCatalogName
    * @param tableSchemaName
    * @param tableName
    */
   public DropSpatialIndexStatement(final String indexName, final String tableCatalogName,
         final String tableSchemaName, final String tableName) {
      this.indexName = indexName;
      this.tableCatalogName = tableCatalogName;
      this.tableSchemaName = tableSchemaName;
      this.tableName = tableName;
   }

   /**
    * Returns the index name.
    * 
    * @return the index name.
    */
   public String getIndexName() {
      return this.indexName;
   }

   /**
    * Returns the table catalog name.
    * 
    * @return the table catalog name.
    */
   public String getTableCatalogName() {
      return this.tableCatalogName;
   }

   /**
    * Returns the table schema name.
    * 
    * @return the table schema name.
    */
   public String getTableSchemaName() {
      return this.tableSchemaName;
   }

   /**
    * Returns the table name.
    * 
    * @return the table name.
    */
   public String getTableName() {
      return this.tableName;
   }
}
