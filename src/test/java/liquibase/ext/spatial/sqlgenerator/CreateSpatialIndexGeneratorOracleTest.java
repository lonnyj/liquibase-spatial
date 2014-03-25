package liquibase.ext.spatial.sqlgenerator;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;
import liquibase.database.Database;
import liquibase.database.core.H2Database;
import liquibase.database.core.OracleDatabase;
import liquibase.ext.spatial.statement.CreateSpatialIndexStatement;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * <code>CreateSpatialIndexGeneratorOracleTest</code> tests
 * {@link CreateSpatialIndexGeneratorOracle}.
 */
public class CreateSpatialIndexGeneratorOracleTest {
   /**
    * Tests
    * {@link CreateSpatialIndexGeneratorOracle#supports(CreateSpatialIndexStatement, Database)}
    */
   @Test
   public void testSupports() {
      final CreateSpatialIndexGeneratorOracle generator = new CreateSpatialIndexGeneratorOracle();
      final CreateSpatialIndexStatement statement = mock(CreateSpatialIndexStatement.class);
      assertTrue(generator.supports(statement, new OracleDatabase()));
      assertFalse(generator.supports(statement, new H2Database()));
   }

   /**
    * Tests {@link CreateSpatialIndexGeneratorOracle#getGtype(String)}.
    */
   @Test(dataProvider = "getGtypeTestData")
   public void testGetGtype(final String geometryType, final String expected) {
      final CreateSpatialIndexGeneratorOracle generator = new CreateSpatialIndexGeneratorOracle();
      final String result = generator.getGtype(geometryType);
      assertEquals(result, expected);
   }

   /**
    * Generates test data for {@link #testGetGtype(String, String)}.
    * 
    * @return the test data.
    */
   @DataProvider
   public Object[][] getGtypeTestData() {
      return new Object[][] { new Object[] { "Geometry", "COLLECTION" },
            new Object[] { "Point", "POINT" }, new Object[] { "MultiPoint", "MULTIPOINT" },
            new Object[] { "LineString", "LINE" }, new Object[] { "MultiLineString", "MULTILINE" },
            new Object[] { "Polygon", "POLYGON" }, new Object[] { "MultiPolygon", "MULTIPOLYGON" },
            new Object[] { "Triangle", "POLYGON" },
            new Object[] { "CircularString", "COLLECTION" }, new Object[] { "Curve", "CURVE" },
            new Object[] { "MultiCurve", "MULTICURVE" },
            new Object[] { "CompoundCurve", "COLLECTION" },
            new Object[] { "CurvePolygon", "COLLECTION" },
            new Object[] { "Surface", "COLLECTION" },
            new Object[] { "MultiSurface", "COLLECTION" },
            new Object[] { "PolyhedralSurface", "COLLECTION" },
            new Object[] { "TIN", "COLLECTION" },
            new Object[] { "GeometryCollection", "COLLECTION" }, new Object[] { null, null },
            new Object[] { "UNKNOWN", "COLLECTION" } };
   }

   /**
    * Tests
    * {@link CreateSpatialIndexGeneratorOracle#generateSql(CreateSpatialIndexStatement, Database, SqlGeneratorChain)}
    * with a variety of inputs.
    * 
    * @param statement
    */
   @Test(dataProvider = "generateSqlTestData")
   public void testGenerateSql(final CreateSpatialIndexStatement statement) {
      final CreateSpatialIndexGeneratorOracle generator = new CreateSpatialIndexGeneratorOracle();
      final Database database = new OracleDatabase();
      final SqlGeneratorChain sqlGeneratorChain = mock(SqlGeneratorChain.class);
      final Sql[] result = generator.generateSql(statement, database, sqlGeneratorChain);
      assertNotNull(result);
      assertEquals(result.length, 3);

      // Verify the DELETE statement.
      final String deleteSql = result[0].toSql();
      String deletePattern = "(?i)DELETE FROM user_sdo_geom_metadata ";
      deletePattern += "WHERE table_name = '" + statement.getTableName().toUpperCase();
      deletePattern += "' AND column_name = '" + statement.getColumns()[0].toUpperCase();
      deletePattern += "'";
      assertTrue(deleteSql.matches(deletePattern), "'" + deleteSql
            + "' does not match the pattern '" + deletePattern + "'");
      assertNotNull(result[2].getAffectedDatabaseObjects());
      assertTrue(result[2].getAffectedDatabaseObjects().size() > 1, result[0]
            .getAffectedDatabaseObjects().toString());

      // Verify the INSERT statement.
      final String insertSql = result[1].toSql();
      final String insertPattern = "(?i)INSERT INTO user_sdo_geom_metadata \\(.*\\) VALUES \\(.*\\)";
      assertTrue(insertSql.matches(insertPattern), "'" + insertSql
            + "' does not match the pattern '" + insertPattern + "'");
      assertNotNull(result[2].getAffectedDatabaseObjects());
      assertTrue(result[2].getAffectedDatabaseObjects().size() > 1, result[0]
            .getAffectedDatabaseObjects().toString());

      // Verify the CREATE INDEX statement.
      final String createSql = result[2].toSql();
      String createPattern = "(?i)CREATE INDEX ";
      if (statement.getTableCatalogName() != null) {
         createPattern += statement.getTableCatalogName() + '.';
      } else if (statement.getTableSchemaName() != null) {
         createPattern += statement.getTableSchemaName() + '.';
      }
      createPattern += statement.getIndexName() + " ON ";
      if (statement.getTableCatalogName() != null) {
         createPattern += statement.getTableCatalogName() + '.';
      } else if (statement.getTableSchemaName() != null) {
         createPattern += statement.getTableSchemaName() + '.';
      }
      createPattern += statement.getTableName() + " \\(" + statement.getColumns()[0] + "\\)";
      createPattern += " INDEXTYPE IS mdsys.spatial_index";
      if (statement.getGeometryType() != null || statement.getTablespace() != null) {
         createPattern += " PARAMETERS \\('";
         if (statement.getGeometryType() != null) {
            createPattern += " ?layer_gtype=[a-zA-Z]+";
         }
         if (statement.getTablespace() != null) {
            createPattern += " ?tablespace=" + statement.getTablespace();
         }
         createPattern += "'\\)";
      }
      assertTrue(createSql.matches(createPattern), "'" + createSql
            + "' does not match the pattern '" + createPattern + "'");
      assertNotNull(result[2].getAffectedDatabaseObjects());
      assertTrue(result[2].getAffectedDatabaseObjects().size() > 1, result[0]
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
            new Object[] { new CreateSpatialIndexStatement("index_name", "catalog_name",
                  "schema_name", "table_name", new String[] { "geom" }, "tablespace", "Geometry",
                  4326) },
            new Object[] { new CreateSpatialIndexStatement("index_name", null, "schema_name",
                  "table_name", new String[] { "geom" }, "tablespace", "Geometry", 4326) },
            new Object[] { new CreateSpatialIndexStatement("index_name", null, null, "table_name",
                  new String[] { "geom" }, "tablespace", "Geometry", 4326) },
            new Object[] { new CreateSpatialIndexStatement("index_name", null, null, "table_name",
                  new String[] { "geom" }, null, "Geometry", 4326) },
            new Object[] { new CreateSpatialIndexStatement("index_name", null, null, "table_name",
                  new String[] { "geom" }, null, null, 4326) },
            new Object[] { new CreateSpatialIndexStatement("index_name", null, null, "table_name",
                  new String[] { "geom" }, null, null, null) },
            new Object[] { new CreateSpatialIndexStatement("index_name", null, null, "table_name",
                  new String[] { "geom", "ignoredColumn" }, null, null, null) } };
   }
}
