package liquibase.ext.spatial.change;

import liquibase.change.AbstractChange;
import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.database.Database;
import liquibase.ext.spatial.statement.DropSpatialIndexStatement;
import liquibase.ext.spatial.xml.XmlConstants;
import liquibase.statement.SqlStatement;

/**
 * @author Lonny Jacobson
 */
@DatabaseChange(name = "dropSpatialIndex", description = "Drops the spatial index on an existing column or set of columns.", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "index")
public class DropSpatialIndexChange extends AbstractChange {

   private String schemaName;
   private String indexName;
   private String tableName;

   private String catalogName;

   @DatabaseChangeProperty(mustEqualExisting = "index.schema")
   public String getSchemaName() {
      return this.schemaName;
   }

   public void setSchemaName(final String schemaName) {
      this.schemaName = schemaName;
   }

   @DatabaseChangeProperty(mustEqualExisting = "index", description = "Name of the index to drop", requiredForDatabase = "mysql, oracle, postgresql")
   public String getIndexName() {
      return this.indexName;
   }

   public void setIndexName(final String indexName) {
      this.indexName = indexName;
   }

   @DatabaseChangeProperty(mustEqualExisting = "index.table", description = "Name fo the indexed table.", requiredForDatabase = "h2, derby")
   public String getTableName() {
      return this.tableName;
   }

   public void setTableName(final String tableName) {
      this.tableName = tableName;
   }

   @DatabaseChangeProperty(mustEqualExisting = "index.catalog")
   public String getCatalogName() {
      return this.catalogName;
   }

   public void setCatalogName(final String catalogName) {
      this.catalogName = catalogName;
   }

   @Override
   public String getSerializedObjectNamespace() {
      return XmlConstants.SPATIAL_CHANGELOG_NAMESPACE;
   }

   /**
    * @see liquibase.change.Change#getConfirmationMessage()
    */
   @Override
   public String getConfirmationMessage() {
      // TODO Auto-generated method stub
      return null;
   }

   /**
    * @see liquibase.change.Change#generateStatements(liquibase.database.Database)
    */
   @Override
   public SqlStatement[] generateStatements(final Database database) {
      final DropSpatialIndexStatement drop = new DropSpatialIndexStatement(this.indexName,
            this.catalogName, this.schemaName, this.tableName);
      return new SqlStatement[] { drop };
   }
}
