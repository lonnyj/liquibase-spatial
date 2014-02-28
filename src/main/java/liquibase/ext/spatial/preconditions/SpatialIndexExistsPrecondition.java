package liquibase.ext.spatial.preconditions;

import java.util.ArrayList;
import java.util.List;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.database.core.DerbyDatabase;
import liquibase.database.core.H2Database;
import liquibase.exception.PreconditionErrorException;
import liquibase.exception.PreconditionFailedException;
import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
import liquibase.logging.LogFactory;
import liquibase.logging.Logger;
import liquibase.precondition.Precondition;
import liquibase.precondition.core.ErrorPrecondition;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.Index;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;
import liquibase.util.StringUtils;

/**
 * The <code>SpatialIndexExistsPrecondition</code>....
 */
public class SpatialIndexExistsPrecondition implements Precondition {
   /** The Logger for this class. */
   private static final Logger LOGGER = LogFactory.getInstance().getLog(
         SpatialIndexExistsPrecondition.class.getSimpleName());
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
      final ValidationErrors validationErrors = new ValidationErrors();

      if ((database instanceof DerbyDatabase || database instanceof H2Database)
            && getTableName() == null) {
         validationErrors
               .addError("tableName is required for " + database.getDatabaseProductName());
      }

      // TODO: This needs to take the database type into account.
      if (getIndexName() == null && getTableName() == null && getColumnNames() == null) {
         validationErrors.addError("indexName OR tableName and columnNames is required");
      }
      return validationErrors;
   }

   @Override
   public void check(final Database database, final DatabaseChangeLog changeLog,
         final ChangeSet changeSet) throws PreconditionFailedException, PreconditionErrorException {
      try {
         final Schema schema = new Schema(getCatalogName(), getSchemaName());
         final String tableName = StringUtils.trimToNull(getTableName());

         // For H2, the index is actually another table.
         final DatabaseObject example;
         if (database instanceof DerbyDatabase || database instanceof H2Database) {

            if (getTableName() == null) {
               final List<ErrorPrecondition> erroredPreconditions = new ArrayList<ErrorPrecondition>();
               erroredPreconditions.add(new ErrorPrecondition(new NullPointerException(
                     "The tableName is null"), changeLog, this));
               throw new PreconditionErrorException("tableName is required for "
                     + database.getDatabaseProductName(), erroredPreconditions);
            }
            final String correctedTableName = database.correctObjectName(tableName + "_HATBOX",
                  Table.class);
            example = new Table().setName(correctedTableName).setSchema(schema);
         } else {
            example = getIndexExample(database, schema, tableName);
         }
         LOGGER.debug("Checking for the example " + example + " in " + database);
         if (!SnapshotGeneratorFactory.getInstance().has(example, database)) {
            LOGGER.debug(example + " was not found");
            String name = "";

            if (getIndexName() != null) {
               name += database.escapeObjectName(getIndexName(), Index.class);
            }

            if (tableName != null) {
               name += " on " + database.escapeObjectName(getTableName(), Table.class);

               if (StringUtils.trimToNull(getColumnNames()) != null) {
                  name += " columns " + getColumnNames();
               }
            }
            throw new PreconditionFailedException("Index " + name + " does not exist", changeLog,
                  this);
         }
         LOGGER.debug(example + " was found");
      } catch (final Exception e) {
         if (e instanceof PreconditionFailedException) {
            throw (((PreconditionFailedException) e));
         }
         throw new PreconditionErrorException(e, changeLog, this);
      }
   }

   /**
    * @param database
    * @param schema
    * @param tableName
    * @return
    */
   protected DatabaseObject getHatboxTableExample(final Database database, final Schema schema,
         final String tableName) {
      final DatabaseObject example;
      final Table table = new Table();
      table.setName(database.correctObjectName(tableName + "_HATBOX", Table.class)).setSchema(null,
            "PUBLIC");
      // TODO: schema);
      example = table;
      return example;
   }

   /**
    * @param database
    * @param schema
    * @param tableName
    * @return
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
