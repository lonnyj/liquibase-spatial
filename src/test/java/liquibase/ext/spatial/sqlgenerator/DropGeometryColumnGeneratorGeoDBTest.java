package liquibase.ext.spatial.sqlgenerator;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.core.DerbyDatabase;
import liquibase.database.core.H2Database;
import liquibase.database.core.OracleDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.DropColumnStatement;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * <code>DropGeometryColumnGeneratorGeoDBTest</code> tests {@link DropGeometryColumnGeneratorGeoDB}.
 */
public class DropGeometryColumnGeneratorGeoDBTest {
   /**
    * Tests {@link DropGeometryColumnGeneratorGeoDB#supports(DropColumnStatement, Database)}.
    * 
    * @param statement
    *           the add column statement.
    * @param database
    *           the database instance.
    * @param expected
    *           the expected result from <code>supports</code>.
    */
   @Test(dataProvider = "supportsTestData")
   public void testSupports(final DropColumnStatement statement, final Database database,
         final boolean expected) {
      final DropGeometryColumnGeneratorGeoDB generator = new DropGeometryColumnGeneratorGeoDB();
      final boolean result = generator.supports(statement, database);
      assertEquals(result, expected);
   }

   /**
    * Provides test data to {@link #testSupports(DropColumnStatement, Database, boolean)}.
    * 
    * @return the test data.
    */
   @DataProvider
   public Object[][] supportsTestData() {
      final DropColumnStatement statement = new DropColumnStatement(null, null, null, null);
      return new Object[][] { new Object[] { statement, new DerbyDatabase(), true },
            new Object[] { statement, new H2Database(), true },
            new Object[] { statement, new OracleDatabase(), false }, };
   }

   @Test(dataProvider = "generateSqlTestData")
   public void testGenerateSql(final DropColumnStatement statement, final Database database,
         final Sql[] expected) throws DatabaseException {
      final DropGeometryColumnGeneratorGeoDB generator = new DropGeometryColumnGeneratorGeoDB();
      final SqlGeneratorChain sqlGeneratorChain = mock(SqlGeneratorChain.class);
      when(sqlGeneratorChain.generateSql(statement, database)).thenReturn(new Sql[0]);
      final Sql[] result = generator.generateSql(statement, database, sqlGeneratorChain);
      assertEquals(result.length, expected.length);
      if (result.length > 0) {
         for (int ii = 0; ii < result.length; ii++) {
            final Sql resultSql = result[ii];
            final Sql expectedSql = expected[ii];
            assertEquals(resultSql.toSql(), expectedSql.toSql());
         }
      }
   }

   @DataProvider
   public Object[][] generateSqlTestData() throws SQLException {
      // Create an H2 database instance with an empty geometry_columns table.
      final Database notGeometryDatabase = new H2Database();
      Connection connection = DriverManager.getConnection("jdbc:h2:mem:target/noGeometry");
      DatabaseConnection conn = new JdbcConnection(connection);
      notGeometryDatabase.setConnection(conn);
      Statement statement = connection.createStatement();
      statement.execute("CREATE TABLE geometry_columns (f_table_schema VARCHAR(128), "
            + "f_table_name VARCHAR(128), f_geometry_column VARCHAR(128), coord_dimension INT, "
            + "srid INT, type VARCHAR(30))");
      statement.close();

      // Create an H2 database instance with a populated geometry_columns table.
      final Database geometryDatabase = new H2Database();
      connection = DriverManager.getConnection("jdbc:h2:mem:target/geometry");
      conn = new JdbcConnection(connection);
      geometryDatabase.setConnection(conn);
      statement = connection.createStatement();
      statement.execute("CREATE TABLE geometry_columns (f_table_schema VARCHAR(128), "
            + "f_table_name VARCHAR(128), f_geometry_column VARCHAR(128), coord_dimension INT, "
            + "srid INT, type VARCHAR(30))");
      statement
            .execute("INSERT INTO geometry_columns VALUES('"
                  + geometryDatabase.getDefaultSchemaName()
                  + "', 'TEST', 'COLUMN', 2, 4326, 'GEOMETRY')");
      statement.close();

      final DropColumnStatement nullSchema = new DropColumnStatement(null, null, "TEST", "COLUMN");

      final DropColumnStatement complete = new DropColumnStatement(null,
            geometryDatabase.getDefaultSchemaName(), "TEST", "COLUMN");
      final Sql completeExpected = new UnparsedSql("CALL DropGeometryColumn('"
            + geometryDatabase.getDefaultSchemaName() + "', 'TEST', 'COLUMN')");

      return new Object[][] { new Object[] { nullSchema, notGeometryDatabase, new Sql[0] },
            new Object[] { nullSchema, geometryDatabase, new Sql[] { completeExpected } },
            new Object[] { complete, geometryDatabase, new Sql[] { completeExpected } }, };
   }
}
