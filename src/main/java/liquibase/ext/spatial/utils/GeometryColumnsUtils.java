package liquibase.ext.spatial.utils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.core.DerbyDatabase;
import liquibase.database.core.H2Database;
import liquibase.database.core.PostgresDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Table;
import liquibase.structure.core.View;

/**
 * <code>GeometryColumnsUtils</code> provides useful methods for checking the
 * <code>GEOMETRY_COLUMNS</code> table.
 */
public class GeometryColumnsUtils {

   /**
    * Hide the default constructor.
    */
   private GeometryColumnsUtils() {
   }

   /**
    * Determines if the given table is in <code>GEOMETRY_COLUMNS</code> and,
    * therefore, has a geometry column.
    * 
    * @param database
    *           the database to query.
    * @param schemaName
    *           the schema name.
    * @param tableName
    *           the table name to check.
    * @return <code>true</code> if the table has a geometry column.
    */
   public static boolean hasGeometryColumn(final Database database,
         final String schemaName, final String tableName) {
      boolean isSpatialColumn = false;
      Statement jdbcStatement = null;
      try {
         if (geometryColumnsExists(database)) {
            final String query = "SELECT * FROM geometry_columns WHERE f_table_schema = '"
                  + (schemaName == null ? database.getDefaultSchemaName()
                        : schemaName)
                  + "' AND f_table_name = '"
                  + tableName
                  + "'";
            final DatabaseConnection databaseConnection = database
                  .getConnection();
            final JdbcConnection jdbcConnection = (JdbcConnection) databaseConnection;
            jdbcStatement = jdbcConnection.getUnderlyingConnection()
                  .createStatement();
            final ResultSet rs = jdbcStatement.executeQuery(query);
            isSpatialColumn = rs.next();
         }
      } catch (final SQLException e) {
         throw new UnexpectedLiquibaseException(
               "Failed to determine if the table has a geometry column", e);
      } finally {
         if (jdbcStatement != null) {
            try {
               jdbcStatement.close();
            } catch (final SQLException ignore) {
            }
         }
      }
      return isSpatialColumn;
   }

   /**
    * Determines if the given column is in <code>GEOMETRY_COLUMNS</code>.
    * 
    * @param database
    *           the database to query.
    * @param schemaName
    *           the schema name.
    * @param tableName
    *           the table name.
    * @param columnName
    *           the column name.
    * @return <code>true</code> if the column is a geometry column.
    */
   public static boolean isGeometryColumn(final Database database,
         final String schemaName, final String tableName,
         final String columnName) {
      boolean isSpatialColumn = false;
      Statement jdbcStatement = null;
      try {
         if (geometryColumnsExists(database)) {
            final String query = "SELECT * FROM geometry_columns WHERE f_table_schema = '"
                  + (schemaName == null ? database.getDefaultSchemaName()
                        : schemaName)
                  + "' AND f_table_name = '"
                  + tableName
                  + "' AND upper(f_geometry_column) = '"
                  + columnName.toUpperCase() + "'";
            final DatabaseConnection databaseConnection = database
                  .getConnection();
            final JdbcConnection jdbcConnection = (JdbcConnection) databaseConnection;
            jdbcStatement = jdbcConnection.getUnderlyingConnection()
                  .createStatement();
            final ResultSet rs = jdbcStatement.executeQuery(query);
            isSpatialColumn = rs.next();
         }
      } catch (final SQLException e) {
         throw new UnexpectedLiquibaseException(
               "Failed to determine if the column to be dropped is a geometry column",
               e);
      } finally {
         if (jdbcStatement != null) {
            try {
               jdbcStatement.close();
            } catch (final SQLException ignore) {
            }
         }
      }
      return isSpatialColumn;
   }

   /**
    * Indicates if the <code>GEOMETRY_COLUMNS</code> table or view exists.
    * 
    * @param database
    *           the database to check.
    * @return <code>true</code> if the table or view exists.
    */
   public static boolean geometryColumnsExists(final Database database) {
      String geometryColumnsName = database.correctObjectName(
            "geometry_columns", Table.class);
      DatabaseObject example = null;
      if (database instanceof DerbyDatabase || database instanceof H2Database) {
         final Table tableExample = new Table();
         tableExample.setName(geometryColumnsName);
         tableExample.setSchema(database.getDefaultCatalogName(),
               database.getDefaultSchemaName());
         example = tableExample;
      } else if (database instanceof PostgresDatabase) {
         final View viewExample = new View();
         viewExample.setName(geometryColumnsName);
         viewExample.setSchema(database.getDefaultCatalogName(), "public");
         example = viewExample;
      }
      try {
         return example != null
               && SnapshotGeneratorFactory.getInstance().has(example, database);
      } catch (final LiquibaseException e) {
         throw new UnexpectedLiquibaseException(
               "Failed to determine if the geometry_columns table or view exists",
               e);
      }
   }
}
