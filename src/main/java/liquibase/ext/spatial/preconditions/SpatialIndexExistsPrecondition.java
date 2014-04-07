package liquibase.ext.spatial.preconditions;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.database.core.DerbyDatabase;
import liquibase.database.core.H2Database;
import liquibase.exception.PreconditionErrorException;
import liquibase.exception.PreconditionFailedException;
import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
import liquibase.precondition.Precondition;
import liquibase.precondition.core.IndexExistsPrecondition;
import liquibase.precondition.core.TableExistsPrecondition;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.Index;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;
import liquibase.util.StringUtils;

/**
 * <code>SpatialIndexExistsPrecondition</code> determines if a spatial index exists on a specified
 * table.
 */
public class SpatialIndexExistsPrecondition implements Precondition {
   private String catalogName;
   private String schemaName;
   private String tableName;
   private String columnNames;
   private String indexName;

   public String getCatalogName() {
      return this.catalogName;
   }

   public void setCatalogName(final String catalogName) {
      this.catalogName = catalogName;
   }

   public String getSchemaName() {
      return this.schemaName;
   }

   public void setSchemaName(final String schemaName) {
      this.schemaName = schemaName;
   }

   public String getTableName() {
      return this.tableName;
   }

   public void setTableName(final String tableName) {
      this.tableName = tableName;
   }

   public String getIndexName() {
      return this.indexName;
   }

   public void setIndexName(final String indexName) {
      this.indexName = indexName;
   }

   public String getColumnNames() {
      return this.columnNames;
   }

   public void setColumnNames(final String columnNames) {
      this.columnNames = columnNames;
   }

   @Override
   public String getName() {
      return "spatialIndexExists";
   }

   @Override
   public Warnings warn(final Database database) {
      return new Warnings();
   }

   @Override
   public ValidationErrors validate(final Database database) {
      final ValidationErrors validationErrors;

      if ((database instanceof DerbyDatabase || database instanceof H2Database)
            && getTableName() == null) {
         validationErrors = new ValidationErrors();
         validationErrors
               .addError("tableName is required for " + database.getDatabaseProductName());
      } else {
         final IndexExistsPrecondition precondition = new IndexExistsPrecondition();
         precondition.setCatalogName(getCatalogName());
         precondition.setSchemaName(getSchemaName());
         precondition.setTableName(getTableName());
         precondition.setIndexName(getIndexName());
         precondition.setColumnNames(getColumnNames());
         validationErrors = precondition.validate(database);
      }
      return validationErrors;
   }

   @Override
   public void check(final Database database, final DatabaseChangeLog changeLog,
         final ChangeSet changeSet) throws PreconditionFailedException, PreconditionErrorException {
      Precondition delegatedPrecondition;
      if (database instanceof DerbyDatabase || database instanceof H2Database) {
         final TableExistsPrecondition precondition = new TableExistsPrecondition();
         precondition.setCatalogName(getCatalogName());
         precondition.setSchemaName(getSchemaName());
         final String tableName = getHatboxTableName();
         precondition.setTableName(tableName);
         delegatedPrecondition = precondition;
      } else {
         final IndexExistsPrecondition precondition = new IndexExistsPrecondition();
         precondition.setCatalogName(getCatalogName());
         precondition.setSchemaName(getSchemaName());
         precondition.setTableName(getTableName());
         precondition.setIndexName(getIndexName());
         precondition.setColumnNames(getColumnNames());
         delegatedPrecondition = precondition;
      }
      delegatedPrecondition.check(database, changeLog, changeSet);
   }

   /**
    * Generates the table name containing the Hatbox index.
    * 
    * @return the Hatbox table name.
    */
   protected String getHatboxTableName() {
      final String tableName;
      if (!StringUtils.hasUpperCase(getTableName())) {
         tableName = getTableName() + "_hatbox";
      } else {
         tableName = getTableName() + "_HATBOX";
      }
      return tableName;
   }

   /**
    * Creates an example of the database object for which to check.
    * 
    * @param database
    *           the database instance.
    * @param tableName
    *           the table name of the index.
    * @return the database object example.
    */
   public DatabaseObject getExample(final Database database, final String tableName) {
      final Schema schema = new Schema(getCatalogName(), getSchemaName());
      final DatabaseObject example;

      // For GeoDB, the index is another table.
      if (database instanceof DerbyDatabase || database instanceof H2Database) {
         final String correctedTableName = database.correctObjectName(getHatboxTableName(),
               Table.class);
         example = new Table().setName(correctedTableName).setSchema(schema);
      } else {
         example = getIndexExample(database, schema, tableName);
      }
      return example;
   }

   /**
    * Generates the {@link Index} example (taken from {@link IndexExistsPrecondition}).
    * 
    * @param database
    *           the database instance.
    * @param schema
    *           the schema instance.
    * @param tableName
    *           the table name of the index.
    * @return the index example.
    */
   protected Index getIndexExample(final Database database, final Schema schema,
         final String tableName) {
      final Index example = new Index();
      if (tableName != null) {
         example.setTable((Table) new Table().setName(
               database.correctObjectName(getTableName(), Table.class)).setSchema(schema));
      }
      example.setName(database.correctObjectName(getIndexName(), Index.class));
      if (StringUtils.trimToNull(getColumnNames()) != null) {
         for (final String column : getColumnNames().split("\\s*,\\s*")) {
            example.getColumns().add(database.correctObjectName(column, Column.class));
         }
      }
      return example;
   }
}
