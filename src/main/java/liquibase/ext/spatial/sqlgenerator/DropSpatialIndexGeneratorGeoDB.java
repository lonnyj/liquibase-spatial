package liquibase.ext.spatial.sqlgenerator;

import liquibase.database.Database;
import liquibase.database.core.DerbyDatabase;
import liquibase.database.core.H2Database;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.ext.spatial.preconditions.SpatialIndexExistsPrecondition;
import liquibase.ext.spatial.statement.DropSpatialIndexStatement;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.AbstractSqlGenerator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Table;

/**
 * <code>DropSpatialIndexGeneratorGeoDB</code> ...
 */
public class DropSpatialIndexGeneratorGeoDB extends AbstractSqlGenerator<DropSpatialIndexStatement> {

   /**
    * @see liquibase.sqlgenerator.core.AbstractSqlGenerator#supports(liquibase.statement.SqlStatement,
    *      liquibase.database.Database)
    */
   @Override
   public boolean supports(final DropSpatialIndexStatement statement, final Database database) {
      return database instanceof DerbyDatabase || database instanceof H2Database;
   }

   @Override
   public ValidationErrors validate(final DropSpatialIndexStatement statement,
         final Database database, final SqlGeneratorChain sqlGeneratorChain) {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public Sql[] generateSql(final DropSpatialIndexStatement statement, final Database database,
         final SqlGeneratorChain sqlGeneratorChain) {
      final String catalogName = statement.getTableCatalogName();
      String schemaName = statement.getTableSchemaName();
      if (schemaName == null) {
         schemaName = database.getDefaultSchemaName();
      }

      final StringBuilder sql = new StringBuilder("CALL ");
      sql.append(schemaName).append(".DropSpatialIndex(");

      // Add the schema name parameter.
      sql.append("'").append(database.escapeStringForDatabase(schemaName)).append("'");
      sql.append(", ");

      // Add the table name parameter.
      final String tableName = statement.getTableName();
      sql.append("'").append(database.escapeStringForDatabase(tableName)).append("'");
      sql.append(')');

      final Table hatboxTable = new Table().setName(tableName + "_HATBOX");
      hatboxTable.setSchema(catalogName, schemaName);
      final UnparsedSql spatialize = new UnparsedSql(sql.toString(), hatboxTable);
      return new Sql[] { spatialize };
   }

   /**
    * Generates the SQL statement to drop the spatial index if it exists.
    * 
    * @param statement
    *           the drop spatial index statement.
    * @param database
    *           the database.
    * @param list
    *           the list of SQL statements to execute.
    */
   public Sql[] generateSqlIfExists(final DropSpatialIndexStatement statement,
         final Database database) {
      final String catalogName = statement.getTableCatalogName();
      final String schemaName = statement.getTableSchemaName();
      final String tableName = statement.getTableName();
      final SpatialIndexExistsPrecondition precondition = new SpatialIndexExistsPrecondition();
      precondition.setCatalogName(catalogName);
      precondition.setSchemaName(schemaName);
      precondition.setTableName(tableName);
      final DatabaseObject example = precondition.getExample(database, tableName);
      try {
         // If a spatial index exists on the table, drop it.
         if (SnapshotGeneratorFactory.getInstance().has(example, database)) {
            return generateSql(statement, database, null);
         }
      } catch (final Exception e) {
         throw new UnexpectedLiquibaseException(e);
      }
      return new Sql[0];
   }
}
