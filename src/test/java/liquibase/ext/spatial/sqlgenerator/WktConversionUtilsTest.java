package liquibase.ext.spatial.sqlgenerator;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;
import liquibase.database.Database;
import liquibase.ext.spatial.sqlgenerator.WktConversionUtils.EWKTInfo;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * <code>WktConversionUtilsTest</code> tests {@link WktConversionUtils}.
 */
public class WktConversionUtilsTest {
   /**
    * Tests successful invocataions of
    * {@link WktConversionUtils#convertToFunction(String, String, Database, WktInsertOrUpdateGenerator)}
    * .
    * 
    * @param wkt
    *           the Well-Known Text
    * @param srid
    *           the SRID
    * @param geomFromTextFunction
    *           the geometry function name.
    * @param isSridRequired
    *           <code>true</code> if the SRID is required.
    * @param expected
    *           the expected result.
    */
   @Test(dataProvider = "convertToFunctionTestData")
   public void testConvertToFunction(final String wkt, final String srid,
         final String geomFromTextFunction, final boolean isSridRequired, final String expected) {
      final Database database = mock(Database.class);
      final WktInsertOrUpdateGenerator generator = mock(WktInsertOrUpdateGenerator.class);
      when(generator.getGeomFromWktFunction()).thenReturn(geomFromTextFunction);
      when(generator.isSridRequiredInFunction(database)).thenReturn(isSridRequired);
      final String actual = WktConversionUtils.convertToFunction(wkt, srid, database, generator);
      assertEquals(actual, expected);
   }

   /**
    * Generates the test data for
    * {@link #testConvertToFunction(String, String, String, boolean, String)}.
    * 
    * @return the test data.
    */
   @DataProvider
   public Object[][] convertToFunctionTestData() {
      final String wkt1 = "POINT(0 0)";
      final String wkt2 = "POINT (0 0)";
      final String wkt3 = " POINT (0 0)";
      final String wkt4 = " POINT ( 0 0 ) ";
      final String function = "ST_GeomFromText";
      return new Object[][] {
            new Object[] { wkt1, null, function, false, function + "('" + wkt1 + "')" },
            new Object[] { wkt2, null, function, false, function + "('" + wkt2 + "')" },
            new Object[] { wkt3, null, function, false, function + "('" + wkt3 + "')" },
            new Object[] { wkt4, null, function, false, function + "('" + wkt4 + "')" },
            new Object[] { wkt1, "", function, false, function + "('" + wkt1 + "')" },
            new Object[] { wkt1, "4326", function, false, function + "('" + wkt1 + "', 4326)" },
            new Object[] { wkt1, "4326", function, true, function + "('" + wkt1 + "', 4326)" },
            new Object[] { wkt2, "4326", function, true, function + "('" + wkt2 + "', 4326)" },
            new Object[] { wkt3, "4326", function, true, function + "('" + wkt3 + "', 4326)" },
            new Object[] { wkt4, "4326", function, true, function + "('" + wkt4 + "', 4326)" }, };
   }

   /**
    * Tests exception paths through
    * {@link WktConversionUtils#convertToFunction(String, String, Database, WktInsertOrUpdateGenerator)}
    * .
    * 
    * @param wkt
    *           the Well-Known Text
    * @param srid
    *           the SRID
    * @param database
    *           the database instance.
    * @param generator
    *           the generator instance.
    */
   @Test(dataProvider = "convertToFunctionExceptionTestData", expectedExceptions = IllegalArgumentException.class)
   public void testConvertToFunctionException(final String wkt, final String srid,
         final Database database, final WktInsertOrUpdateGenerator generator) {
      final String actual = WktConversionUtils.convertToFunction(wkt, srid, database, generator);
      fail("convertToFunction returned " + actual);
   }

   /**
    * Generates the test data for
    * {@link #testConvertToFunctionException(String, String, Database, WktInsertOrUpdateGenerator)}.
    * 
    * @return the test data.
    */
   @DataProvider
   public Object[][] convertToFunctionExceptionTestData() {
      final Database database = mock(Database.class);
      final WktInsertOrUpdateGenerator generator = mock(WktInsertOrUpdateGenerator.class);
      when(generator.getGeomFromWktFunction()).thenReturn("ST_GeomFromText");
      when(generator.isSridRequiredInFunction(database)).thenReturn(true);
      final String wkt = "POINT(0 0)";
      final String srid = "4326";
      return new Object[][] { new Object[] { null, srid, database, generator },
            new Object[] { "", srid, database, generator },
            new Object[] { wkt, null, database, generator },
            new Object[] { wkt, "", database, generator },
            new Object[] { wkt, srid, database, null }, };
   }

   @Test(dataProvider = "handleColumnValueTestData")
   public void testHandleColumnValue(final Object oldValue, final Database database,
         final WktInsertOrUpdateGenerator generator, final Object expected) {
      final Object actual = WktConversionUtils.handleColumnValue(oldValue, database, generator);
      assertEquals(actual, expected);
   }

   @DataProvider
   public Object[][] handleColumnValueTestData() {
      final Database database = mock(Database.class);
      final WktInsertOrUpdateGenerator generator = mock(WktInsertOrUpdateGenerator.class);
      when(generator.convertToFunction(anyString(), anyString(), eq(database))).thenAnswer(
            new Answer<String>() {
               @Override
               public String answer(final InvocationOnMock invocation) throws Throwable {
                  return invocation.getArguments()[0] + "," + invocation.getArguments()[1];
               }
            });
      final String wkt = "POINT(0 0)";
      final String srid = "SRID=4326";
      return new Object[][] { new Object[] { null, database, generator, null },
            new Object[] { 12345, database, generator, 12345 },
            new Object[] { "test", database, generator, "test" },
            new Object[] { wkt, database, generator, wkt + ",null" },
            new Object[] { srid + ";" + wkt, database, generator, wkt + ",4326" }, };
   }
   
   /**
    * Test {@link WktConversionUtils#getWktInfo(String)}
    * 
    * @param wkt
    * @param wktWithoutSRID
    * @param isV1_1
    * @param srid
    * @param geometryType
    * @param geometryBaseType
    * @param mCoordinate
    * @param zCoordinate
    * @param isMulti
    * @param isColleciton
    * @param data
    */
   @Test(dataProvider = "getWktInfoData")
   public void testGetWktInfo(final String wkt,final String wktWithoutSRID, final boolean isV1_1, final String srid, final String geometryType, final String geometryBaseType, final boolean mCoordinate, final boolean zCoordinate, final boolean isMulti, final boolean isColleciton, final String data) {
      final EWKTInfo info = WktConversionUtils.getWktInfo(wkt);
      assertEquals(info.getOriginalWkt(), wkt.trim());
      assertEquals(info.getWktWithoutSRID(), wktWithoutSRID);
      assertEquals(info.isV1_1(),isV1_1);
      assertEquals(info.getSrid(), srid);
      assertEquals(info.getGeometryType(), geometryType);
      assertEquals(info.getGeometryBaseType(), geometryBaseType);
      assertEquals(info.isMCoordinate(), mCoordinate);
      assertEquals(info.isZCoordinate(), zCoordinate);
      assertEquals(info.isMulti(), isMulti);
      assertEquals(info.isCollection(), isColleciton);
      assertEquals(info.getData(), data);
   }
   
   @DataProvider
   public Object[][] getWktInfoData() {
      return new Object[][] { 
            // Test Point
            new Object[] {" Point(1 1)", "Point(1 1)", true, null, "POINT","POINT", false, false, false, false, "1 1" },
            new Object[] {" POIntz(1 1 1)", "POIntz(1 1 1)", false, null, "POINTZ","POINT", false, true, false,false, "1 1 1" },
            new Object[] {"  point zm(1 1 1 1)  ", "point zm(1 1 1 1)", false, null, "POINTZM","POINT", true, true,false,false, "1 1 1 1" },
            new Object[] {"point m(1 1 1)", "point m(1 1 1)", false, null, "POINTM","POINT", true, false, false, false, "1 1 1" },
            new Object[] {"Srid=4326;point(1 1)", "point(1 1)", false, "4326", "POINT","POINT", false, false, false, false, "1 1" },
            new Object[] {" SRID=4326;POINTZ(1 1 1)", "POINTZ(1 1 1)", false, "4326", "POINTZ","POINT", false, true, false, false, "1 1 1" },
            new Object[] {"MULTIPOINTM(1 1 1, 2 2 2)", "MULTIPOINTM(1 1 1, 2 2 2)", false, null, "MULTIPOINTM","POINT", true, false, true, false, "1 1 1, 2 2 2" },
            
            // Test LineString
            new Object[] {" Linestring(1 1, 2 2)", "Linestring(1 1, 2 2)", true, null, "LINESTRING","LINESTRING", false, false, false, false, "1 1, 2 2" },
            new Object[] {" LINEStringz(1 1 1, 2 2 2)", "LINEStringz(1 1 1, 2 2 2)", false, null, "LINESTRINGZ","LINESTRING", false, true, false,false, "1 1 1, 2 2 2" },
            new Object[] {"  linestring zm(1 1 1 1, 2 2 2 2)  ", "linestring zm(1 1 1 1, 2 2 2 2)", false, null, "LINESTRINGZM","LINESTRING", true, true,false,false, "1 1 1 1, 2 2 2 2" },
            new Object[] {"linestring m(1 1 1, 2 2 2)", "linestring m(1 1 1, 2 2 2)", false, null, "LINESTRINGM","LINESTRING", true, false, false, false, "1 1 1, 2 2 2" },
            new Object[] {"Srid=4326;linestring(1 1, 2 2)", "linestring(1 1, 2 2)", false, "4326", "LINESTRING","LINESTRING", false, false, false, false, "1 1, 2 2" },
            new Object[] {" SRID=4326;LINESTRINGZ(1 1 1, 2 2 2)", "LINESTRINGZ(1 1 1, 2 2 2)", false, "4326", "LINESTRINGZ","LINESTRING", false, true, false, false, "1 1 1, 2 2 2" },
            new Object[] {"MULTILINESTRINGM((1 1 1, 2 2 2),(3 3 3, 4 4 4))", "MULTILINESTRINGM((1 1 1, 2 2 2),(3 3 3, 4 4 4))", false, null, "MULTILINESTRINGM","LINESTRING", true, false, true, false, "(1 1 1, 2 2 2),(3 3 3, 4 4 4)" }, 
            
            // Test Polygon
            new Object[] {" Polygon((1 1, 2 2, 3 3, 4 4, 1 1),(1.5 1.5, 1.75 1.75, 1.5 1.5))", "Polygon((1 1, 2 2, 3 3, 4 4, 1 1),(1.5 1.5, 1.75 1.75, 1.5 1.5))", true, null, "POLYGON","POLYGON", false, false, false, false, "(1 1, 2 2, 3 3, 4 4, 1 1),(1.5 1.5, 1.75 1.75, 1.5 1.5)" },
            new Object[] {" POLygonz((1 1 1, 2 2 2, 3 3 3, 4 4 4, 1 1 1))", "POLygonz((1 1 1, 2 2 2, 3 3 3, 4 4 4, 1 1 1))", false, null, "POLYGONZ","POLYGON", false, true, false,false, "(1 1 1, 2 2 2, 3 3 3, 4 4 4, 1 1 1)" },
            new Object[] {"  polygon zm((1 1 1 1, 2 2 2 2, 3 3 3 3, 4 4 4 4))  ", "polygon zm((1 1 1 1, 2 2 2 2, 3 3 3 3, 4 4 4 4))", false, null, "POLYGONZM","POLYGON", true, true,false,false, "(1 1 1 1, 2 2 2 2, 3 3 3 3, 4 4 4 4)" },
            new Object[] {"polygon m((1 1 1, 2 2 2, 3 3 3, 4 4 4, 1 1 1))", "polygon m((1 1 1, 2 2 2, 3 3 3, 4 4 4, 1 1 1))", false, null, "POLYGONM","POLYGON", true, false, false, false, "(1 1 1, 2 2 2, 3 3 3, 4 4 4, 1 1 1)" },
            new Object[] {"Srid=4326;polygon((1 1, 2 2, 3 3, 4 4, 1 1))", "polygon((1 1, 2 2, 3 3, 4 4, 1 1))", false, "4326", "POLYGON","POLYGON", false, false, false, false, "(1 1, 2 2, 3 3, 4 4, 1 1)" },
            new Object[] {" SRID=4326;POLYGONZ((1 1 1, 2 2 2, 3 3 3, 4 4 4, 1 1 1))", "POLYGONZ((1 1 1, 2 2 2, 3 3 3, 4 4 4, 1 1 1))", false, "4326", "POLYGONZ","POLYGON", false, true, false, false, "(1 1 1, 2 2 2, 3 3 3, 4 4 4, 1 1 1)" },
            new Object[] {"MULTIPOLYGONM(((1 1 1, 2 2 2, 3 3 3, 4 4 4, 1 1 1)),((1 1 1, 2 2 2, 3 3 3, 4 4 4, 1 1 1), (1.5 1.5 1.5, 1.75 1.75 1.75, 1.5 1.5 1.5)))", "MULTIPOLYGONM(((1 1 1, 2 2 2, 3 3 3, 4 4 4, 1 1 1)),((1 1 1, 2 2 2, 3 3 3, 4 4 4, 1 1 1), (1.5 1.5 1.5, 1.75 1.75 1.75, 1.5 1.5 1.5)))", false, null, "MULTIPOLYGONM","POLYGON", true, false, true, false, "((1 1 1, 2 2 2, 3 3 3, 4 4 4, 1 1 1)),((1 1 1, 2 2 2, 3 3 3, 4 4 4, 1 1 1), (1.5 1.5 1.5, 1.75 1.75 1.75, 1.5 1.5 1.5))" }, 
            
            // Test Collection
            new Object[] {"GeometryCollection (Point(1 1),POINT(2 2))", "GeometryCollection (Point(1 1),POINT(2 2))", true, null, "GEOMETRYCOLLECTION","GEOMETRYCOLLECTION", false, false, false, true, "Point(1 1),POINT(2 2)" },
            new Object[] {"  GeometryCollectionZ (POIntz(1 1 1), POIntz(1 1 1)) ", "GeometryCollectionZ (POIntz(1 1 1), POIntz(1 1 1))", false, null, "GEOMETRYCOLLECTIONZ","GEOMETRYCOLLECTION", false, true, false,true, "POIntz(1 1 1), POIntz(1 1 1)" },
            new Object[] {"  geometrycollection zm(pointzm(1 1 1 1),pointzm(1 1 1 1))  ", "geometrycollection zm(pointzm(1 1 1 1),pointzm(1 1 1 1))", false, null, "GEOMETRYCOLLECTIONZM","GEOMETRYCOLLECTION", true, true,false,true, "pointzm(1 1 1 1),pointzm(1 1 1 1)" },
            new Object[] {"geometrycollection m (point m(1 1 1))", "geometrycollection m (point m(1 1 1))", false, null, "GEOMETRYCOLLECTIONM","GEOMETRYCOLLECTION", true, false, false, true, "point m(1 1 1)" },
            new Object[] {"Srid=4326;geometrycollection(point(1 1))", "geometrycollection(point(1 1))", false, "4326", "GEOMETRYCOLLECTION","GEOMETRYCOLLECTION", false, false, false, true, "point(1 1)" },
            new Object[] {" SRID=4326;POINTZ(1 1 1)", "POINTZ(1 1 1)", false, "4326", "POINTZ","POINT", false, true, false, false, "1 1 1" },
       };
      
   }
   
   /**
    * Tests exception on invalids wkt
    * {@link WktConversionUtils#getWktInfo(String)}
    * 
    * @param wkt
    *           invalid Well-Known Text
    */
   @Test(dataProvider = "getInvalidWktData", expectedExceptions = IllegalArgumentException.class)
   public void testConvertToFunctionException(final String wkt) {
      final EWKTInfo info = WktConversionUtils.getWktInfo(wkt);
      fail("Info returned " + info);
   }
   
   @DataProvider
   public Object[][] getInvalidWktData() {
      return new Object[][] { 
            new Object[] { "PONIT(1 1)" },
            new Object[] { "Z POINT(1 1)" },
            new Object[] { "POINTMZ(1 1)" },
            new Object[] { "POINTMZ 1 1)" },
            new Object[] { "LINESTRINGs (1 1, 2 2)" },
      };

   }
}
