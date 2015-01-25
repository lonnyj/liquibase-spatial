package liquibase.ext.spatial.change;

import static org.testng.Assert.*;
import liquibase.change.ColumnConfig;
import liquibase.database.Database;
import liquibase.database.core.H2Database;
import liquibase.exception.ValidationErrors;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * <code>CreateSpatialIndexChangeTest</code> tests {@link CreateSpatialIndexChange}.
 */
public class CreateSpatialIndexChangeTest {
   /**
    * Tests {@link CreateSpatialIndexChange#validate(liquibase.database.Database)}.
    *
    * @param catalogName
    *           the name of the catalog.
    * @param schemaName
    *           the name of the schema.
    * @param tablespace
    *           the name of the tablespace.
    * @param tableName
    *           the name of the table.
    * @param columnName
    *           the geometry column name.
    * @param indexName
    *           the name of the spatial index.
    * @param geometryType
    *           the geometry type.
    * @param srid
    *           the Spatial Reference System ID.
    * @param database
    *           the database instance.
    * @param passes
    *           indicates if the test is expected to pass.
    */
   @Test(dataProvider = "validateTestData")
   public void testValidate(final String catalogName, final String schemaName,
         final String tablespace, final String tableName, final String columnName,
         final String indexName, final String geometryType, final String srid,
         final Database database, final boolean passes) {
      final CreateSpatialIndexChange change = new CreateSpatialIndexChange();
      change.setCatalogName(catalogName);
      change.setSchemaName(schemaName);
      change.setTablespace(tablespace);
      change.setTableName(tableName);
      final ColumnConfig column = new ColumnConfig();
      column.setName(columnName);
      change.addColumn(column);
      change.setIndexName(indexName);
      change.setGeometryType(geometryType);
      change.setSrid(srid);
      final ValidationErrors errors = change.validate(database);
      assertEquals(errors.hasErrors(), !passes, "Errors were " + (passes ? " not" : "")
            + "expected");
   }

   /**
    * Generates the test data for
    * {@link #testValidate(String, String, String, String, String, String, String, String, Database, boolean)}
    * .
    *
    * @return the test data.
    */
   @DataProvider
   public Object[][] validateTestData() {
      final String cat = "mycatalog";
      final String sch = "myschema";
      final String ts = "mytablespace";
      final String tab = "test_table";
      final String col = "GEOM";
      final String ind = "SPATIAL_INDEX";
      final String geom = "POINT";
      final String srid = "4326";
      final H2Database h2 = new H2Database();
      return new Object[][] { new Object[] { cat, sch, ts, tab, col, ind, geom, srid, h2, true },
            new Object[] { cat, sch, ts, tab, col, ind, geom, null, h2, false },
            new Object[] { cat, sch, ts, tab, col, ind, geom, "bad", h2, false },
            new Object[] { cat, sch, ts, tab, col, ind, geom, "4326bad", h2, false },
            new Object[] { cat, sch, ts, tab, col, ind, geom, "bad4326", h2, false },
            new Object[] { cat, sch, ts, tab, col, ind, geom, "b4326ad", h2, false } };
   }
}
