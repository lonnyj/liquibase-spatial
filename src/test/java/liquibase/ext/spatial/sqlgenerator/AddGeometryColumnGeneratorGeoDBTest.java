package liquibase.ext.spatial.sqlgenerator;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;
import liquibase.database.Database;
import liquibase.database.core.DerbyDatabase;
import liquibase.database.core.H2Database;
import liquibase.database.core.OracleDatabase;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.AddColumnStatement;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * <code>AddGeometryColumnGeneratorGeoDBTest</code> tests {@link AddGeometryColumnGeneratorGeoDB}.
 */
public class AddGeometryColumnGeneratorGeoDBTest {
   /**
    * Tests {@link AddGeometryColumnGeneratorGeoDB#supports(AddColumnStatement, Database)}.
    *
    * @param statement
    *           the add column statement.
    * @param database
    *           the database instance.
    * @param expected
    *           the expected result from <code>supports</code>.
    */
   @Test(dataProvider = "supportsTestData")
   public void testSupports(final AddColumnStatement statement, final Database database,
         final boolean expected) {
      final AddGeometryColumnGeneratorGeoDB generator = new AddGeometryColumnGeneratorGeoDB();
      final boolean result = generator.supports(statement, database);
      assertEquals(result, expected);
   }

   /**
    * Provides test data to {@link #testSupports(AddColumnStatement, Database, boolean)}.
    *
    * @return the test data.
    */
   @DataProvider
   public Object[][] supportsTestData() {
      final AddColumnStatement statement = new AddColumnStatement((String) null, null, null, null,
            null, null);
      return new Object[][] { new Object[] { statement, new DerbyDatabase(), true },
            new Object[] { statement, new H2Database(), true },
            new Object[] { statement, new OracleDatabase(), false }, };
   }

   @Test(dataProvider = "generateSqlTestData")
   public void testGenerateSql(final AddColumnStatement statement, final Database database,
         final Sql[] expected) {
      final AddGeometryColumnGeneratorGeoDB generator = new AddGeometryColumnGeneratorGeoDB();
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
   public Object[][] generateSqlTestData() {
      final Database database = new H2Database();

      final AddColumnStatement notGeometry = new AddColumnStatement(null, null, null, null,
            "BOOLEAN", null);

      final AddColumnStatement nullSchema = new AddColumnStatement(null, null, "TEST", "COLUMN",
            "Geometry(Point, 4327)", null);
      final Sql nullSchemaExpected = new UnparsedSql("CALL AddGeometryColumn('"
            + database.getDefaultSchemaName() + "', 'TEST', 'COLUMN', 4327, 'Point', 2)");

      final AddColumnStatement complete = new AddColumnStatement(null,
            database.getDefaultSchemaName(), "TEST", "COLUMN", "Geometry(Geometry,4326)", null);
      final Sql completeExpected = new UnparsedSql("CALL AddGeometryColumn('"
            + database.getDefaultSchemaName() + "', 'TEST', 'COLUMN', 4326, 'Geometry', 2)");

      return new Object[][] { new Object[] { notGeometry, database, new Sql[0] },
            new Object[] { nullSchema, database, new Sql[] { nullSchemaExpected } },
            new Object[] { complete, database, new Sql[] { completeExpected } }, };
   }
}
