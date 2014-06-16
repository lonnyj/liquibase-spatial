package liquibase.ext.spatial.utils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.structure.core.Table;

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
      boolean isSpatialColumn;
      Statement jdbcStatement = null;
      try {
         Table geometryColumns = new Table();
         geometryColumns.setSchema(database.getDefaultCatalogName(),
               database.getDefaultSchemaName());
         geometryColumns.setName("geometry_columns");
         if (!SnapshotGeneratorFactory.getInstance().has(geometryColumns,
               database)) {
            System.out.println("geometry_columns is not found");
            isSpatialColumn = false;
         } else {
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
      } catch (final LiquibaseException e) {
         throw new UnexpectedLiquibaseException(
               "Failed to determine if the geometry_columns table exists", e);
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
      boolean isSpatialColumn;
      Statement jdbcStatement = null;
      try {
         final String query = "SELECT * FROM geometry_columns WHERE f_table_schema = '"
               + (schemaName == null ? database.getDefaultSchemaName()
                     : schemaName)
               + "' AND f_table_name = '"
               + tableName
               + "' AND upper(f_geometry_column) = '"
               + columnName.toUpperCase() + "'";
         final DatabaseConnection databaseConnection = database.getConnection();
         final JdbcConnection jdbcConnection = (JdbcConnection) databaseConnection;
         jdbcStatement = jdbcConnection.getUnderlyingConnection()
               .createStatement();
         final ResultSet rs = jdbcStatement.executeQuery(query);
         isSpatialColumn = rs.next();
      } catch (final SQLException e) {
         throw new UnexpectedLiquibaseException(
               "Failed to determine if the column to be dropped is a geometry column",
               e);
      } finally {
         try {
            jdbcStatement.close();
         } catch (final SQLException ignore) {
         }
      }
      return isSpatialColumn;
   }
}
