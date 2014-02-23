package liquibase.ext.spatial.change;

import java.util.ArrayList;
import java.util.List;

import liquibase.change.AbstractChange;
import liquibase.change.ChangeMetaData;
import liquibase.change.ChangeWithColumns;
import liquibase.change.ColumnConfig;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.database.Database;
import liquibase.ext.spatial.statement.CreateSpatialIndexStatement;
import liquibase.ext.spatial.xml.XmlConstants;
import liquibase.statement.SqlStatement;

/**
 * The <code>CreateSpatialIndexChange</code>....
 */
@DatabaseChange(name = "createSpatialIndex", description = "Creates a spatial index on an existing column or set of columns.", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "index")
public class CreateSpatialIndexChange extends AbstractChange implements
      ChangeWithColumns<ColumnConfig> {
   private String catalogName;
   private String schemaName;
   private String tableName;
   private String indexName;
   private String tablespace;
   private List<ColumnConfig> columns = new ArrayList<ColumnConfig>();

   /**
    * Sets the database catalog name.
    * 
    * @param catalogName
    */
   public void setCatalogName(String catalogName) {
      this.catalogName = catalogName;
   }

   @DatabaseChangeProperty(description = "Name of the catalog")
   public String getCatalogName() {
      return catalogName;
   }

   @DatabaseChangeProperty(mustEqualExisting = "index", description = "Name of the index to create")
   public String getIndexName() {
      return indexName;
   }

   public void setIndexName(String indexName) {
      this.indexName = indexName;
   }

   @DatabaseChangeProperty(mustEqualExisting = "index.schema")
   public String getSchemaName() {
      return schemaName;
   }

   public void setSchemaName(String schemaName) {
      this.schemaName = schemaName;
   }

   @DatabaseChangeProperty(mustEqualExisting = "index.table", description = "Name of the table to add the index to", exampleValue = "person")
   public String getTableName() {
      return tableName;
   }

   public void setTableName(String tableName) {
      this.tableName = tableName;
   }

   @Override
   @DatabaseChangeProperty(mustEqualExisting = "index.column", description = "Column(s) to add to the index", requiredForDatabase = "all")
   public List<ColumnConfig> getColumns() {
      if (columns == null) {
         return new ArrayList<ColumnConfig>();
      }
      return columns;
   }

   @Override
   public void setColumns(List<ColumnConfig> columns) {
      this.columns = columns;
   }

   @Override
   public void addColumn(ColumnConfig column) {
      columns.add(column);
   }

   @DatabaseChangeProperty(description = "Tablepace to create the index in.")
   public String getTablespace() {
      return tablespace;
   }

   public void setTablespace(String tablespace) {
      this.tablespace = tablespace;
   }

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
      String[] columns = new String[this.columns.size()];
      int ii = 0;
      for (ColumnConfig columnConfig : this.columns) {
         columns[ii++] = columnConfig.getName();
      }
      CreateSpatialIndexStatement statement = new CreateSpatialIndexStatement(
            getIndexName(), getCatalogName(), getSchemaName(), getTableName(),
            columns, getTablespace());
      return new SqlStatement[] { statement };
   }

   @Override
   public String getSerializedObjectNamespace() {
      return XmlConstants.SPATIAL_CHANGELOG_NAMESPACE;
   }
}
