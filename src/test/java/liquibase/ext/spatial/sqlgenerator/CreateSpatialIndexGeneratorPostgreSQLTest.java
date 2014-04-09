package liquibase.ext.spatial.sqlgenerator;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;
import liquibase.database.Database;
import liquibase.database.core.H2Database;
import liquibase.database.core.PostgresDatabase;
import liquibase.ext.spatial.statement.CreateSpatialIndexStatement;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * <code>CreateSpatialIndexGeneratorPostgreSQLTest</code> tests
 * {@link CreateSpatialIndexGeneratorPostgreSQL}.
 */
public class CreateSpatialIndexGeneratorPostgreSQLTest {

   /**
    * Tests
    * {@link CreateSpatialIndexGeneratorPostgreSQL#supports(CreateSpatialIndexStatement, Database)}
    */
   @Test
   public void testSupports() {
      final CreateSpatialIndexGeneratorPostgreSQL generator = new CreateSpatialIndexGeneratorPostgreSQL();
      final CreateSpatialIndexStatement statement = mock(CreateSpatialIndexStatement.class);
      assertTrue(generator.supports(statement, new PostgresDatabase()));
      assertFalse(generator.supports(statement, new H2Database()));
   }

   /**
    * Tests
    * {@link CreateSpatialIndexGeneratorPostgreSQL#generateSql(CreateSpatialIndexStatement, Database, SqlGeneratorChain)}
    * with a variety of inputs.
    * 
    * @param statement
    */
   @Test(dataProvider = "generateSqlTestData")
   public void testGenerateSql(final CreateSpatialIndexStatement statement) {
      final CreateSpatialIndexGeneratorPostgreSQL generator = new CreateSpatialIndexGeneratorPostgreSQL();
      final Database database = new PostgresDatabase();
      final SqlGeneratorChain sqlGeneratorChain = mock(SqlGeneratorChain.class);
      final Sql[] result = generator.generateSql(statement, database, sqlGeneratorChain);
      assertNotNull(result);
      assertEquals(result.length, 1);
      final String sql = result[0].toSql();
      String pattern = "(?i)CREATE INDEX ";
      pattern += statement.getIndexName() + " ON ";
      if (statement.getTableSchemaName() != null) {
         pattern += statement.getTableSchemaName() + '.';
      }
      pattern += statement.getTableName() + " USING GIST \\(" + statement.getColumns()[0];
      if (statement.getColumns().length > 1) {
         pattern += ", " + statement.getColumns()[1];
      }
      pattern += "\\)";
      assertTrue(sql.matches(pattern), "'" + sql + "' does not match the pattern '" + pattern + "'");
      assertNotNull(result[0].getAffectedDatabaseObjects());
      assertTrue(result[0].getAffectedDatabaseObjects().size() > 1, result[0]
            .getAffectedDatabaseObjects().toString());
   }

   /**
    * Generates test data for {@link #testGenerateSql(Integer, String)}.
    * 
    * @return the test data.
    */
   @DataProvider
   public Object[][] generateSqlTestData() {
      return new Object[][] {
            new Object[] { new CreateSpatialIndexStatement("indexname", "catalogname",
                  "schemaname", "tablename", new String[] { "geom" }, "tablespace", "Geometry",
                  4326) },
            new Object[] { new CreateSpatialIndexStatement("indexname", null, "schemaname",
                  "tablename", new String[] { "geom" }, "tablespace", "Geometry", 4326) },
            new Object[] { new CreateSpatialIndexStatement("indexname", null, null, "tablename",
                  new String[] { "geom" }, "tablespace", "Geometry", 4326) },
            new Object[] { new CreateSpatialIndexStatement("indexname", null, null, "tablename",
                  new String[] { "geom" }, null, "Geometry", 4326) },
            new Object[] { new CreateSpatialIndexStatement("indexname", null, null, "tablename",
                  new String[] { "geom" }, null, null, 4326) },
            new Object[] { new CreateSpatialIndexStatement("indexname", null, null, "tablename",
                  new String[] { "geom" }, null, null, null) },
            new Object[] { new CreateSpatialIndexStatement("indexname", null, null, "tablename",
                  new String[] { "geom", "another_olumn" }, null, null, null) } };
   }
}
