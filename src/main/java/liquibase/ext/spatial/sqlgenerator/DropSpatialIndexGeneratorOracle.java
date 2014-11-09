package liquibase.ext.spatial.sqlgenerator;

import liquibase.database.Database;
import liquibase.database.core.OracleDatabase;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.ext.spatial.statement.DropSpatialIndexStatement;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.AbstractSqlGenerator;
import liquibase.structure.core.Column;
import liquibase.structure.core.Index;
import liquibase.structure.core.Table;
import liquibase.structure.core.View;

/**
 * <code>DropSpatialIndexGeneratorOracle</code> generates the SQL for cleaning up a spatial index in
 * Oracle.
 */
public class DropSpatialIndexGeneratorOracle extends
AbstractSqlGenerator<DropSpatialIndexStatement> {
   @Override
   public boolean supports(final DropSpatialIndexStatement statement, final Database database) {
      return database instanceof OracleDatabase;
   }

   /**
    * Ensures that the index name is populated.
    */
   @Override
   public ValidationErrors validate(final DropSpatialIndexStatement statement,
         final Database database, final SqlGeneratorChain sqlGeneratorChain) {
      final ValidationErrors validationErrors = new ValidationErrors();
      validationErrors.checkRequiredField("indexName", statement.getIndexName());
      return validationErrors;
   }

   @Override
   public Sql[] generateSql(final DropSpatialIndexStatement statement, final Database database,
         final SqlGeneratorChain sqlGeneratorChain) {
      final String indexName = statement.getIndexName();
      final Index example = new Index().setName(indexName);
      if (statement.getTableName() != null) {
         example.setTable((Table) new Table().setName(statement.getTableName()).setSchema(
               statement.getTableCatalogName(), statement.getTableSchemaName()));
      }
      Index index;
      try {
         index = SnapshotGeneratorFactory.getInstance().createSnapshot(example, database);
      } catch (final Exception e) {
         throw new UnexpectedLiquibaseException("Failed to create a snapshot of '" + indexName
               + "'", e);
      }

      final String tableName = index.getTable().getName();
      final Column column = index.getColumns().get(0);

      final StringBuilder sql = new StringBuilder();
      sql.append("DELETE FROM user_sdo_geom_metadata ");
      sql.append("WHERE table_name = '").append(database.correctObjectName(tableName, Table.class));
      sql.append("' AND column_name = '").append(
            database.correctObjectName(column.getName(), Column.class));
      sql.append("'");
      final UnparsedSql deleteMetadata = new UnparsedSql(sql.toString(),
            new View().setName("user_sdo_geom_metadata"));
      return new Sql[] { deleteMetadata };
   }
}
