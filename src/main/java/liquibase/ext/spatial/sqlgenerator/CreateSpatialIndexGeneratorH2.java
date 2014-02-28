package liquibase.ext.spatial.sqlgenerator;

import liquibase.database.Database;
import liquibase.database.core.H2Database;
import liquibase.exception.ValidationErrors;
import liquibase.ext.spatial.statement.CreateSpatialIndexStatement;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.structure.core.Table;

/**
 * The <code>CreateSpatialIndexGeneratorH2</code> generates the SQL for creating a spatial index in
 * H2.
 */
public class CreateSpatialIndexGeneratorH2 extends AbstractCreateSpatialIndexGenerator {
   /**
    * @see liquibase.sqlgenerator.core.AbstractSqlGenerator#supports(liquibase.statement.SqlStatement,
    *      liquibase.database.Database)
    */
   @Override
   public boolean supports(final CreateSpatialIndexStatement statement, final Database database) {
      return database instanceof H2Database;
   }

   /**
    * @see liquibase.sqlgenerator.SqlGenerator#validate(liquibase.statement.SqlStatement,
    *      liquibase.database.Database, liquibase.sqlgenerator.SqlGeneratorChain)
    */
   @Override
   public ValidationErrors validate(final CreateSpatialIndexStatement statement,
         final Database database, final SqlGeneratorChain sqlGeneratorChain) {
      // TODO Auto-generated method stub
      return null;
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
         schemaName = "PUBLIC";
      }
      final StringBuilder spatializeSql = new StringBuilder("CALL ");
      spatializeSql.append(schemaName).append(".HATBOX_SPATIALIZE(");
      if (schemaName == null || "NULL".equalsIgnoreCase(schemaName)) {
         spatializeSql.append("NULL");
      } else {
         spatializeSql.append("'").append(database.escapeStringForDatabase(schemaName)).append("'");
      }
      spatializeSql.append(", ");
      final String tableName = statement.getTableName();
      spatializeSql.append("'").append(database.escapeStringForDatabase(tableName)).append("'");
      spatializeSql.append(", ");
      final String columnName = database.escapeColumnName(catalogName, schemaName, tableName,
            statement.getColumns()[0]);
      spatializeSql.append("'").append(columnName).append("'");
      spatializeSql.append(", ");
      final String geometryType = statement.getGeometryType();
      if (geometryType == null || "NULL".equalsIgnoreCase(geometryType)) {
         spatializeSql.append("NULL");
      } else {
         spatializeSql.append("'").append(database.escapeStringForDatabase(geometryType))
               .append("'");
      }
      spatializeSql.append(", ");
      final Integer srid = statement.getSrid();
      if (srid == null) {
         spatializeSql.append("NULL");
      } else {
         spatializeSql.append("'").append(srid).append("'");
      }
      spatializeSql.append(", ").append("NULL"); // Use the default PK expose value.
      spatializeSql.append(", ").append("NULL"); // Use the default (49) number of entries per node.
      spatializeSql.append(')');
      final Table hatboxTable = new Table().setName(tableName + "_HATBOX");
      hatboxTable.setSchema(catalogName, schemaName);
      final UnparsedSql spatialize = new UnparsedSql(spatializeSql.toString(), hatboxTable);

      final StringBuilder buildIndexSql = new StringBuilder("CALL HATBOX_BUILD_INDEX(");
      if (schemaName == null || "NULL".equalsIgnoreCase(schemaName)) {
         buildIndexSql.append("NULL");
      } else {
         buildIndexSql.append("'").append(database.escapeStringForDatabase(schemaName)).append("'");
      }
      buildIndexSql.append(", ");
      buildIndexSql.append("'").append(database.escapeStringForDatabase(tableName)).append("'");
      buildIndexSql.append(", ").append(10000); // Commit every 10k records.
      buildIndexSql.append(", NULL"); // Do not use the progress monitor.
      buildIndexSql.append(')');
      final UnparsedSql buildIndex = new UnparsedSql(buildIndexSql.toString(), hatboxTable);
      return new Sql[] { spatialize, buildIndex };
   }
}
