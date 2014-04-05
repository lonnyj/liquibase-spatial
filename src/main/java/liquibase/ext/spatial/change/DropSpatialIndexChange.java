package liquibase.ext.spatial.change;

import java.util.ArrayList;
import java.util.Collection;

import liquibase.change.AbstractChange;
import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.database.Database;
import liquibase.database.core.DerbyDatabase;
import liquibase.database.core.H2Database;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.ext.spatial.statement.DropSpatialIndexStatement;
import liquibase.ext.spatial.xml.XmlConstants;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.DropIndexStatement;
import liquibase.util.StringUtils;

/**
 * @author Lonny Jacobson
 */
@DatabaseChange(name = "dropSpatialIndex", description = "Drops the spatial index on an existing column or set of columns.", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "index")
public class DropSpatialIndexChange extends AbstractChange {
   /** The name of the catalog. */
   private String catalogName;

   /** The name of the schema. */
   private String schemaName;

   /** The name of the indexed table. */
   private String tableName;

   /** The name of the index to drop. */
   private String indexName;

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

   @Override
   public String getConfirmationMessage() {
      final StringBuilder message = new StringBuilder("Spatial index");
      if (StringUtils.trimToNull(getIndexName()) != null) {
         message.append(' ').append(getIndexName().trim());
      }
      message.append(" dropped");
      if (StringUtils.trimToNull(getTableName()) != null) {
         message.append(" from ").append(getTableName().trim());
      }
      return message.toString();
   }

   /**
    * Generates a {@link DropSpatialIndexStatement} followed by a {@link DropIndexStatement}, if
    * applicable. The first statement allows extra clean-up when dropping an index. The second
    * statement leverages the normal <code>DROP INDEX</code> logic.
    */
   @Override
   public SqlStatement[] generateStatements(final Database database) {
      final Collection<SqlStatement> statements = new ArrayList<SqlStatement>();
      // MySQL and PostgreSQL only need the normal DROP INDEX statement.
      if (!(database instanceof MySQLDatabase) && !(database instanceof PostgresDatabase)) {
         final DropSpatialIndexStatement dropSpatialIndex = new DropSpatialIndexStatement(
               this.indexName, this.catalogName, this.schemaName, this.tableName);
         statements.add(dropSpatialIndex);
      }

      // GeoDB doesn't use a tradition index structure so don't issue the normal DROP INDEX
      // statement.
      if (!(database instanceof DerbyDatabase) && !(database instanceof H2Database)) {
         final DropIndexStatement dropIndex = new DropIndexStatement(this.indexName,
               this.catalogName, this.schemaName, this.tableName, null);
         statements.add(dropIndex);
      }
      return statements.toArray(new SqlStatement[statements.size()]);
   }
}
