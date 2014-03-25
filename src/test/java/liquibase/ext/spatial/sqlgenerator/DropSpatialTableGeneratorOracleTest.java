package liquibase.ext.spatial.sqlgenerator;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;
import liquibase.database.Database;
import liquibase.database.core.H2Database;
import liquibase.database.core.OracleDatabase;
import liquibase.ext.spatial.statement.CreateSpatialIndexStatement;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.DropTableStatement;

import org.testng.annotations.Test;

/**
 * <code>DropSpatialTableGeneratorOracleTest</code> tests {@link DropSpatialTableGeneratorOracle}.
 */
public class DropSpatialTableGeneratorOracleTest {
   /**
    * Tests {@link DropSpatialTableGeneratorOracle#supports(CreateSpatialIndexStatement, Database)}
    */
   @Test
   public void testSupports() {
      final DropSpatialTableGeneratorOracle generator = new DropSpatialTableGeneratorOracle();
      final DropTableStatement statement = mock(DropTableStatement.class);
      assertTrue(generator.supports(statement, new OracleDatabase()));
      assertFalse(generator.supports(statement, new H2Database()));
   }

   /**
    * Tests
    * {@link DropSpatialTableGeneratorOracle#generateSql(CreateSpatialIndexStatement, Database, SqlGeneratorChain)}
    * with a variety of inputs.
    * 
    * @param statement
    */
   @Test(dataProvider = "generateSqlTestData")
   public void testGenerateSql() {
      final DropSpatialTableGeneratorOracle generator = new DropSpatialTableGeneratorOracle();
      final Database database = new OracleDatabase();
      final SqlGeneratorChain sqlGeneratorChain = mock(SqlGeneratorChain.class);
      final DropTableStatement statement = new DropTableStatement("catalog_name", "schema_name",
            "table_name", true);
      final Sql[] result = generator.generateSql(statement, database, sqlGeneratorChain);
      assertNotNull(result);
      assertEquals(result.length, 1);

      // Verify the DELETE statement.
      final String deleteSql = result[0].toSql();
      String deletePattern = "(?i)DELETE FROM user_sdo_geom_metadata ";
      deletePattern += "WHERE table_name = '" + statement.getTableName().toUpperCase();
      deletePattern += "'";
      assertTrue(deleteSql.matches(deletePattern), "'" + deleteSql
            + "' does not match the pattern '" + deletePattern + "'");
      assertNotNull(result[0].getAffectedDatabaseObjects());
      assertTrue(result[0].getAffectedDatabaseObjects().size() > 1, result[0]
            .getAffectedDatabaseObjects().toString());
   }
}
