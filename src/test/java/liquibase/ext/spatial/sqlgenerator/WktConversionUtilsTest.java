package liquibase.ext.spatial.sqlgenerator;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;
import liquibase.database.Database;

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
      final String wkt = "POINT(0 0)";
      final String function = "ST_GeomFromText";
      return new Object[][] {
            new Object[] { wkt, null, function, false, function + "('" + wkt + "')" },
            new Object[] { wkt, "", function, false, function + "('" + wkt + "')" },
            new Object[] { wkt, "4326", function, false, function + "('" + wkt + "', 4326)" },
            new Object[] { wkt, "4326", function, true, function + "('" + wkt + "', 4326)" }, };
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
}
