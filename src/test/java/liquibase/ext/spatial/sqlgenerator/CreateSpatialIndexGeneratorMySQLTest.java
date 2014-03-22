package liquibase.ext.spatial.sqlgenerator;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;
import liquibase.database.Database;
import liquibase.database.core.MySQLDatabase;
import liquibase.ext.spatial.statement.CreateSpatialIndexStatement;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * <code>CreateSpatialIndexGeneratorMySQLTest</code> tests {@link CreateSpatialIndexGeneratorMySQL}.
 */
public class CreateSpatialIndexGeneratorMySQLTest {
   @Test(dataProvider = "generateSqlTestData")
   public void testGenerateSql(final CreateSpatialIndexStatement statement) {
      final CreateSpatialIndexGeneratorMySQL generator = new CreateSpatialIndexGeneratorMySQL();
      final Database database = new MySQLDatabase();
      final SqlGeneratorChain sqlGeneratorChain = mock(SqlGeneratorChain.class);
      final Sql[] result = generator.generateSql(statement, database, sqlGeneratorChain);
      assertNotNull(result);
      assertEquals(result.length, 1);
      final String sql = result[0].toSql();
      final String pattern = "(?i)CREATE SPATIAL INDEX " + statement.getIndexName()
            + " ON ([a-zA-Z0-9]+[.])?" + statement.getTableName() + "\\("
            + statement.getColumns()[0] + "\\)";
      assertTrue(sql.matches(pattern), "'" + sql + "' does not match the pattern '" + pattern + "'");
      assertNotNull(result[0].getAffectedDatabaseObjects());
      assertTrue(result[0].getAffectedDatabaseObjects().size() > 1, result[0]
            .getAffectedDatabaseObjects().toString());
   }

   /**
    * Generates test data for {@link #testGenerateSql(Integer, String)}.
    * 
    * @return
    */
   @DataProvider
   public Object[][] generateSqlTestData() {
      return new Object[][] {
            new Object[] { new CreateSpatialIndexStatement("indexName", "catalogName",
                  "schemaName", "tableName", new String[] { "geom" }, "tablespace", "Geometry",
                  4326) },
            new Object[] { new CreateSpatialIndexStatement("indexName", null, "schemaName",
                  "tableName", new String[] { "geom" }, "tablespace", "Geometry", 4326) },
            new Object[] { new CreateSpatialIndexStatement("indexName", null, null, "tableName",
                  new String[] { "geom" }, "tablespace", "Geometry", 4326) },
            new Object[] { new CreateSpatialIndexStatement("indexName", null, null, "tableName",
                  new String[] { "geom" }, null, "Geometry", 4326) },
            new Object[] { new CreateSpatialIndexStatement("indexName", null, null, "tableName",
                  new String[] { "geom" }, null, null, 4326) },
            new Object[] { new CreateSpatialIndexStatement("indexName", null, null, "tableName",
                  new String[] { "geom" }, null, null, null) },
            new Object[] { new CreateSpatialIndexStatement("indexName", null, null, "tableName",
                  new String[] { "geom", "ignoredColumn" }, null, null, null) } };
   }
}
