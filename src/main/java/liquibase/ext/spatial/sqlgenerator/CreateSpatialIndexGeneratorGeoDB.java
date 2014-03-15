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
   /**
    * @see liquibase.sqlgenerator.core.AbstractSqlGenerator#supports(liquibase.statement.SqlStatement,
    *      liquibase.database.Database)
    */
   @Override
   public boolean supports(final CreateSpatialIndexStatement statement, final Database database) {
      return database instanceof DerbyDatabase || database instanceof H2Database;
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
         schemaName = database.getDefaultSchemaName();
      }
      final StringBuilder sql = new StringBuilder("CALL ");
      sql.append(schemaName).append(".CreateSpatialIndex(");

      // Add the schema name parameter.
      sql.append("'").append(database.escapeStringForDatabase(schemaName)).append("'");
      sql.append(", ");

      // Add the table name parameter.
      final String tableName = statement.getTableName();
      sql.append("'").append(database.escapeStringForDatabase(tableName)).append("'");
      sql.append(", ");

      // Add the column name parameter.
      final String columnName = database.escapeColumnName(catalogName, schemaName, tableName,
            statement.getColumns()[0]);
      sql.append("'").append(columnName).append("'");
      // spatializeSql.append(", ");
      // final String geometryType = statement.getGeometryType();
      // if (geometryType == null || "NULL".equalsIgnoreCase(geometryType)) {
      // spatializeSql.append("NULL");
      // } else {
      // spatializeSql.append("'").append(database.escapeStringForDatabase(geometryType))
      // .append("'");
      // }
      sql.append(", ");

      // Add the SRID parameter.
      final Integer srid = statement.getSrid();
      if (srid == null) {
         sql.append("NULL");
      } else {
         sql.append("'").append(srid).append("'");
      }
      // spatializeSql.append(", ").append("NULL"); // Use the default PK expose value.
      // spatializeSql.append(", ").append("NULL"); // Use the default (49) number of entries per
      // node.
      sql.append(')');
      final Table hatboxTable = new Table().setName(tableName + "_HATBOX");
      hatboxTable.setSchema(catalogName, schemaName);
      final UnparsedSql spatialize = new UnparsedSql(sql.toString(), hatboxTable);

      return new Sql[] { spatialize };
   }
}
