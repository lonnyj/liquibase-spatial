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
import liquibase.sqlgenerator.core.DropColumnGenerator;
import liquibase.statement.core.DropColumnStatement;
import liquibase.structure.core.Column;

/**
 * <code>DropGeometryColumnGeneratorGeoDB</code> is a {@link DropColumnGenerator} that specializes
 * in GeoDB. If there exists an index on the column, <code>DropSpatialIndex</code> is invoked first.
 * If the column to be dropped is the geometry column, the <code>DropGeometryColumn</code> procedure
 * is invoked instead of performing the typical <code>ALTER TABLE</code> statement. Otherwise, the
 * next SQL generator in the chain is invoked to handle the request.
 */
public class DropGeometryColumnGeneratorGeoDB extends DropColumnGenerator {
   /**
    * @see liquibase.sqlgenerator.core.AbstractSqlGenerator#supports(liquibase.statement.SqlStatement,
    *      liquibase.database.Database)
    */
   @Override
   public boolean supports(final DropColumnStatement statement, final Database database) {
      return database instanceof DerbyDatabase || database instanceof H2Database;
   }

   /**
    * @see liquibase.sqlgenerator.core.AbstractSqlGenerator#getPriority()
    */
   @Override
   public int getPriority() {
      return super.getPriority() + 1;
   }

   @Override
   public ValidationErrors validate(final DropColumnStatement statement, final Database database,
         final SqlGeneratorChain sqlGeneratorChain) {
      return sqlGeneratorChain.validate(statement, database);
   }

   @Override
   public Sql[] generateSql(final DropColumnStatement statement, final Database database,
         final SqlGeneratorChain sqlGeneratorChain) {

      String schemaName = statement.getSchemaName();
      if (schemaName == null) {
         schemaName = database.getDefaultSchemaName();
      }
      final String tableName = statement.getTableName();
      final String columnName = statement.getColumnName();
      final boolean isGeometryColumn = GeometryColumnsUtils.isGeometryColumn(database, schemaName,
            tableName, columnName);
      final List<Sql> list = new ArrayList<Sql>();
      if (isGeometryColumn) {
         dropSpatialIndexIfExists(statement.getCatalogName(), schemaName, tableName, database, list);
         final String sql = "CALL DropGeometryColumn('" + schemaName + "', '" + tableName + "', '"
               + columnName + "')";
         final Column column = getAffectedColumn(statement);
         final Sql dropGeometryColumn = new UnparsedSql(sql, column);
         list.add(dropGeometryColumn);
      } else {
         list.addAll(Arrays.asList(sqlGeneratorChain.generateSql(statement, database)));
      }
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
