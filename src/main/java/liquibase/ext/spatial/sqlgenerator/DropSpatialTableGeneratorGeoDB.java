package liquibase.ext.spatial.sqlgenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import liquibase.database.Database;
import liquibase.database.core.DerbyDatabase;
import liquibase.database.core.H2Database;
import liquibase.exception.ValidationErrors;
import liquibase.ext.spatial.statement.DropSpatialIndexStatement;
import liquibase.ext.spatial.utils.GeometryColumnsUtils;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.AbstractSqlGenerator;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.DropTableStatement;

/**
 * <code>DropSpatialTableGeneratorGeoDB</code> generates the SQL statements for cleaning up any
 * indexes and geometry columns prior to dropping the table.
 */
public class DropSpatialTableGeneratorGeoDB extends AbstractSqlGenerator<DropTableStatement> {
   /**
    * @see AbstractSqlGenerator#supports(SqlStatement, Database)
    */
   @Override
   public boolean supports(final DropTableStatement statement, final Database database) {
      return database instanceof DerbyDatabase || database instanceof H2Database;
   }

   /**
    * @see AbstractSqlGenerator#getPriority()
    */
   @Override
   public int getPriority() {
      return super.getPriority() + 1;
   }

   @Override
   public ValidationErrors validate(final DropTableStatement statement, final Database database,
         final SqlGeneratorChain sqlGeneratorChain) {
      return sqlGeneratorChain.validate(statement, database);
   }

   @Override
   public Sql[] generateSql(final DropTableStatement statement, final Database database,
         final SqlGeneratorChain sqlGeneratorChain) {
      final List<Sql> list = new ArrayList<Sql>();
      String schemaName = statement.getSchemaName();
      if (schemaName == null) {
         schemaName = database.getDefaultSchemaName();
      }
      final String tableName = statement.getTableName();

      // If the table has a geometry column, drop it first.
      if (GeometryColumnsUtils.hasGeometryColumn(database, schemaName, tableName)) {
         dropSpatialIndexIfExists(statement.getCatalogName(), schemaName, tableName, database, list);
         final String sql = "CALL DropGeometryColumns('" + schemaName + "', '" + tableName + "')";
         final Sql addGeometryColumn = new UnparsedSql(sql);
         list.add(addGeometryColumn);
      }
      list.addAll(Arrays.asList(sqlGeneratorChain.generateSql(statement, database)));
      return list.toArray(new Sql[list.size()]);
   }

   /**
    * Adds the SQL statement to drop the spatial index if it is present.
    * 
    * @param catalogName
    *           the catalog name.
    * @param schemaName
    *           the schema name.
    * @param tableName
    *           the table name.
    * @param database
    *           the database.
    * @param list
    *           the list of SQL statements to execute.
    */
   protected void dropSpatialIndexIfExists(final String catalogName, final String schemaName,
         final String tableName, final Database database, final List<Sql> list) {
      final DropSpatialIndexGeneratorGeoDB generator = new DropSpatialIndexGeneratorGeoDB();
      final DropSpatialIndexStatement statement = new DropSpatialIndexStatement(null, catalogName,
            schemaName, tableName);
      list.addAll(Arrays.asList(generator.generateSqlIfExists(statement, database)));
   }
}
