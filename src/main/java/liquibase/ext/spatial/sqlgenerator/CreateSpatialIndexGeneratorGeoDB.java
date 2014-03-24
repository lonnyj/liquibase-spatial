package liquibase.ext.spatial.sqlgenerator;

import liquibase.database.Database;
import liquibase.database.core.DerbyDatabase;
import liquibase.database.core.H2Database;
import liquibase.exception.ValidationErrors;
import liquibase.ext.spatial.statement.CreateSpatialIndexStatement;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.structure.core.Table;

/**
 * <code>CreateSpatialIndexGeneratorGeoDB</code> generates the SQL for creating a spatial index in
 * Apache Derby and H2.
 */
public class CreateSpatialIndexGeneratorGeoDB extends AbstractCreateSpatialIndexGenerator {
   @Override
   public boolean supports(final CreateSpatialIndexStatement statement, final Database database) {
      return database instanceof DerbyDatabase || database instanceof H2Database;
   }

   /**
    * {@inheritDoc} Also ensures that the SRID is populated.
    */
   @Override
   public ValidationErrors validate(final CreateSpatialIndexStatement statement,
         final Database database, final SqlGeneratorChain sqlGeneratorChain) {
      final ValidationErrors validationErrors = super.validate(statement, database,
            sqlGeneratorChain);
      validationErrors.checkRequiredField("srid", statement.getSrid());
      return validationErrors;
   }

   /**
    * @see liquibase.sqlgenerator.SqlGenerator#generateSql(liquibase.statement.SqlStatement,
    *      liquibase.database.Database, liquibase.sqlgenerator.SqlGeneratorChain)
    */
   @Override
   public Sql[] generateSql(final CreateSpatialIndexStatement statement, final Database database,
         final SqlGeneratorChain sqlGeneratorChain) {
      final String catalogName = statement.getTableCatalogName();
      String schemaName = statement.getTableSchemaName();
      if (schemaName == null) {
         schemaName = database.getDefaultSchemaName();
      }
      final StringBuilder sql = new StringBuilder("CALL ");
      sql.append(schemaName).append(".CreateSpatialIndex(");

      // Add the schema name parameter.
      sql.append("'").append(schemaName).append("'");
      sql.append(", ");

      // Add the table name parameter.
      final String tableName = statement.getTableName();
      sql.append("'").append(tableName).append("'");
      sql.append(", ");

      // Add the column name parameter.
      final String columnName = statement.getColumns()[0];
      sql.append("'").append(columnName).append("'");
      sql.append(", ");

      // Add the SRID parameter.
      final int srid = statement.getSrid();
      sql.append("'").append(srid).append("'");
      sql.append(')');
      final Table hatboxTable = new Table().setName(database.correctObjectName(tableName
            + "_HATBOX", Table.class));
      hatboxTable.setSchema(catalogName, schemaName);
      final UnparsedSql spatialize = new UnparsedSql(sql.toString(), hatboxTable);

      return new Sql[] { spatialize };
   }
}
