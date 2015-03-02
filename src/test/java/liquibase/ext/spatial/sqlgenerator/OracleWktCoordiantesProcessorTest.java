package liquibase.ext.spatial.sqlgenerator;

import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import liquibase.util.StringUtils;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * <code>OracleWktCoordiantesProcessorTest</code> tests {@link OracleWktCoordiantesProcessor}.
 */
public class OracleWktCoordiantesProcessorTest {
   
   /**
    * Tests {@link OracleWktCoordiantesProcessor#process(String, List, List)}
    * 
    * @param wkt
    * @param elementInfo
    * @param ordinates
    * @throws Exception
    */
   @Test(dataProvider = "processData")
   public void testProcessor(String wkt,String elementInfo, String ordinates) throws Exception{
      OracleWktCoordiantesProcessor processor = new OracleWktCoordiantesProcessor();

      List<String> elementInfoArray = new ArrayList<String>();
      List<String> ordinateArray = new ArrayList<String>();
      processor.process(wkt, elementInfoArray, ordinateArray);
      assertEquals(StringUtils.join(elementInfoArray, ","), elementInfo);
      assertEquals(StringUtils.join(ordinateArray, ","), ordinates);
   }
   
   
   @DataProvider
   public Object[][] processData(){
      return new Object[][] {
            // Point
            new Object[]{"Point(10 10)", "1,1,1", "10.0,10.0"},
            new Object[]{"Pointz(10 10 5)", "1,1,1", "10.0,10.0,5.0"},
            new Object[]{"Point m(10 10 5)", "1,1,1", "10.0,10.0,5.0"},
            new Object[]{"Pointzm(10 10 5 6)", "1,1,1", "10.0,10.0,5.0,6.0"},
            new Object[]{"MultiPOINT(10 10, 20 20, 30 30)", "1,1,1,3,1,1,5,1,1", "10.0,10.0,20.0,20.0,30.0,30.0"},
            new Object[]{"MultiPOINTz(10 10 5, 20 20 5.5, 30 30 5.1)", "1,1,1,4,1,1,7,1,1", "10.0,10.0,5.0,20.0,20.0,5.5,30.0,30.0,5.1"},
            
            // LineString
            new Object[]{"LineSTring(10 10,20 20,30 30)", "1,2,1", "10.0,10.0,20.0,20.0,30.0,30.0"},
            new Object[]{"LineSTringz(10 10 1, 20 20 2, 30 30 3)", "1,2,1", "10.0,10.0,1.0,20.0,20.0,2.0,30.0,30.0,3.0"},
            new Object[]{"LineSTring m(10 10 1, 20 20 2, 30 30 3)", "1,2,1", "10.0,10.0,1.0,20.0,20.0,2.0,30.0,30.0,3.0"},
            new Object[]{"LineSTringzm(10 10 1 1, 20 20 2 2, 30 30 3 3)", "1,2,1", "10.0,10.0,1.0,1.0,20.0,20.0,2.0,2.0,30.0,30.0,3.0,3.0"},
            new Object[]{" multiLineSTring ((10  10, 20 20, 30 30),(40 40, 50 50))", "1,2,1,7,2,1", "10.0,10.0,20.0,20.0,30.0,30.0,40.0,40.0,50.0,50.0"},
            new Object[]{"    multiLineSTring z ((10     10 1, 20 20 2, 30 30 3) ,  (40 40 4, 50 50 5)   )  ", "1,2,1,10,2,1", "10.0,10.0,1.0,20.0,20.0,2.0,30.0,30.0,3.0,40.0,40.0,4.0,50.0,50.0,5.0"},
            
            // Polygon
            new Object[]{"POLYGON ((10 10, 20 20, 30 30, 10 10))", "1,1003,1", "10.0,10.0,20.0,20.0,30.0,30.0,10.0,10.0"},
            new Object[]{"POLYGON Z((10 10 1, 20 20 2, 30 30 3, 10 10 1))", "1,1003,1", "10.0,10.0,1.0,20.0,20.0,2.0,30.0,30.0,3.0,10.0,10.0,1.0"},
            new Object[]{" polygon ((10 10, 20 20, 30 30, 10 10),(5.5 5.5, 6.6 6.6, 5.5 5.5),(8.8 8.8, 9.9 9.9, 8.8 8.8))", "1,1003,1,9,2003,1,15,2003,1", "10.0,10.0,20.0,20.0,30.0,30.0,10.0,10.0,5.5,5.5,6.6,6.6,5.5,5.5,8.8,8.8,9.9,9.9,8.8,8.8"},
            new Object[]{" polygonm ((10 10 1, 20 20 2, 30 30 3, 10 10 1),(5.5 5.5 5, 6.6 6.6 6, 7.7 7.7 7),(8.8 8.8 8, 9.9 9.9 9, 9.5 9.5 9))", "1,1003,1,13,2003,1,22,2003,1", "10.0,10.0,1.0,20.0,20.0,2.0,30.0,30.0,3.0,10.0,10.0,1.0,5.5,5.5,5.0,6.6,6.6,6.0,7.7,7.7,7.0,8.8,8.8,8.0,9.9,9.9,9.0,9.5,9.5,9.0"},
            new Object[]{" polygon zm   ((10 10 1 1, 20 20 2 2, 30 30 3 3, 10 10 1 1),(5.5 5.5 5 5, 6.6 6.6 6 6, 7.7 7.7 7 7),(8.8 8.8 8 8, 9.9 9.9 9 9, 9.5 9.5 9 9))", "1,1003,1,17,2003,1,29,2003,1", "10.0,10.0,1.0,1.0,20.0,20.0,2.0,2.0,30.0,30.0,3.0,3.0,10.0,10.0,1.0,1.0,5.5,5.5,5.0,5.0,6.6,6.6,6.0,6.0,7.7,7.7,7.0,7.0,8.8,8.8,8.0,8.0,9.9,9.9,9.0,9.0,9.5,9.5,9.0,9.0"},
            new Object[]{" multipolygon (((10 10, 20 20, 30 30, 10 10),(5.5 5.5, 6.6 6.6, 7.7 7.7),(8.8 8.8, 9.9 9.9, 9.5 9.5)),((1 1, 2 2, 3 3, 1 1)))", "1,1003,1,9,2003,1,15,2003,1,21,1003,1", "10.0,10.0,20.0,20.0,30.0,30.0,10.0,10.0,5.5,5.5,6.6,6.6,7.7,7.7,8.8,8.8,9.9,9.9,9.5,9.5,1.0,1.0,2.0,2.0,3.0,3.0,1.0,1.0"},
            new Object[]{" multipolygonm (((10 10 1, 20 20 2, 30 30 3, 10 10 1),(5.5 5.5 5, 6.6 6.6 6, 7.7 7.7 7),(8.8 8.8 8, 9.9 9.9 9, 9.5 9.5 9)),((1 1 1, 2 2 2, 3 3 3, 1 1 1)))", "1,1003,1,13,2003,1,22,2003,1,31,1003,1", "10.0,10.0,1.0,20.0,20.0,2.0,30.0,30.0,3.0,10.0,10.0,1.0,5.5,5.5,5.0,6.6,6.6,6.0,7.7,7.7,7.0,8.8,8.8,8.0,9.9,9.9,9.0,9.5,9.5,9.0,1.0,1.0,1.0,2.0,2.0,2.0,3.0,3.0,3.0,1.0,1.0,1.0"},
            
            // Geometry collection
            new Object[]{" GeometryCollection(Point(10 10))", "1,1,1", "10.0,10.0"},
            new Object[]{" GeometryCollection(Point(10 10),Point(20 20),Point(30 30))", "1,1,1,3,1,1,5,1,1", "10.0,10.0,20.0,20.0,30.0,30.0"},
            new Object[]{" geometrycollection(Point(10 10),lineSTRING(20 20,30 30))", "1,1,1,3,2,1", "10.0,10.0,20.0,20.0,30.0,30.0"},
            new Object[]{" GeometryCOllection(Point(10 10),lineSTRING(20 20,30 30), Point(10 10))", "1,1,1,3,2,1,7,1,1", "10.0,10.0,20.0,20.0,30.0,30.0,10.0,10.0"},
            new Object[]{"  GEOMETRYCOLLECTIONM( Point m(10 10 5))", "1,1,1", "10.0,10.0,5.0"},
            new Object[]{"  GEOMETRYCOLLECTION zM( Point zm(10 10 5 5.1), Point zm(10 10 5 5.1))", "1,1,1,5,1,1", "10.0,10.0,5.0,5.1,10.0,10.0,5.0,5.1"},
            new Object[]{"  GEOMETRYCOLLECTIONzM( LInestRINGzm(10 10 5 5.1,10 10 5 5.1))", "1,2,1", "10.0,10.0,5.0,5.1,10.0,10.0,5.0,5.1"},
            new Object[]{"gEOMETRYCOLLECTIONZm( LInestRINGzm(10 10 5 5.1,10 10 5 5.1), Point zm(10 10 5 5.1))", "1,2,1,9,1,1", "10.0,10.0,5.0,5.1,10.0,10.0,5.0,5.1,10.0,10.0,5.0,5.1"},
            new Object[]{"gEOMETRYCOLLECTIONZm( polygonzm((10 10 5 5.1,10 10 5 5.1, 10 10 5 5.1)))", "1,1003,1", "10.0,10.0,5.0,5.1,10.0,10.0,5.0,5.1,10.0,10.0,5.0,5.1"},
            new Object[]{"geometrycollectionzm( polygonzm((10 10 5 5.1,10 10 5 5.1, 10 10 5 5.1)), Point zm(10 10 5 5.1), linestringzm(10 10 5 5.1, 10 10 5 5.1))", "1,1003,1,13,1,1,17,2,1", "10.0,10.0,5.0,5.1,10.0,10.0,5.0,5.1,10.0,10.0,5.0,5.1,10.0,10.0,5.0,5.1,10.0,10.0,5.0,5.1,10.0,10.0,5.0,5.1"},
            new Object[]{"geometrycollection zm( polygonzm((10 10 5 5.1,10 10 5 5.1, 10 10 5 5.1)), Point zm(10 10 5 5.1), linestringzm(10 10 5 5.1, 10 10 5 5.1), polygonzm((10 10 5 5.1,10 10 5 5.1, 10 10 5 5.1), (10 10 5 5.1,10 10 5 5.1, 10 10 5 5.1)))", "1,1003,1,13,1,1,17,2,1,25,1003,1,37,2003,1", "10.0,10.0,5.0,5.1,10.0,10.0,5.0,5.1,10.0,10.0,5.0,5.1,10.0,10.0,5.0,5.1,10.0,10.0,5.0,5.1,10.0,10.0,5.0,5.1,10.0,10.0,5.0,5.1,10.0,10.0,5.0,5.1,10.0,10.0,5.0,5.1,10.0,10.0,5.0,5.1,10.0,10.0,5.0,5.1,10.0,10.0,5.0,5.1"}, 
            new Object[]{"geometrycollectionzm( polygonzm((10 10 5 5.1,10 10 5 5.1, 10 10 5 5.1)), Point zm(10 10 5 5.1), linestringzm(10 10 5 5.1, 10 10 5 5.1), polygonzm((10 10 5 5.1,10 10 5 5.1, 10 10 5 5.1), (10 10 5 5.1,10 10 5 5.1, 10 10 5 5.1)),multipointzm(10 10 5 5.1, 10 10 5 5.1))", "1,1003,1,13,1,1,17,2,1,25,1003,1,37,2003,1,49,1,1,53,1,1", "10.0,10.0,5.0,5.1,10.0,10.0,5.0,5.1,10.0,10.0,5.0,5.1,10.0,10.0,5.0,5.1,10.0,10.0,5.0,5.1,10.0,10.0,5.0,5.1,10.0,10.0,5.0,5.1,10.0,10.0,5.0,5.1,10.0,10.0,5.0,5.1,10.0,10.0,5.0,5.1,10.0,10.0,5.0,5.1,10.0,10.0,5.0,5.1,10.0,10.0,5.0,5.1,10.0,10.0,5.0,5.1"} 
      };
      
   }
   /**
    * Tests exception on invalids wkt
    * {@link OracleWktCoordiantesProcessor#process(String, List, List)}
    * 
    * @param wkt
    *           invalid Well-Known Text
    */
   @Test(dataProvider = "getInvalidWktData", expectedExceptions = IllegalArgumentException.class)
   public void testConvertToFunctionException(final String wkt) throws IOException{
      OracleWktCoordiantesProcessor processor = new OracleWktCoordiantesProcessor();
      List<String> elementInfoArray = new ArrayList<String>();
      List<String> ordinateArray = new ArrayList<String>();
      processor.process(wkt, elementInfoArray, ordinateArray);

   } 
   
   @DataProvider
   public Object[][] getInvalidWktData() {
      return new Object[][] { 
            new Object[] { "PONIT(1 1)" },
            new Object[] { "POINT(1)" },
            new Object[] { "POINT()" },
            new Object[] { "Z POINT(1 1)" },
            new Object[] { "POINTZ(1 1)" },
            new Object[] { "POINTMZ(1 1 1 1)" },
            new Object[] { "POINTZM(1 1)" },
            new Object[] { "POINTZM(1 1 1)" },
            new Object[] { "LINESTRINGs (1 1, 2 2)" },
            new Object[] { "LINESTRING((1 1, 2 2))" },
            new Object[] { "POLYgonm((1 1, 2 2))" },
            new Object[] { "POLYgon((1 1, 2 2),())" },
            new Object[] { "POLYgon((1 1, 2 2),)" },
            new Object[] { "poligon(1 1, 2 2, 1 1)" },
            new Object[] { "multypoligon((1 1, 2 2, 1 1),(1 1, 2 2, 1 1))" },
            new Object[] { "GeometryCollection ( LINESTRINGm (1 1 1, 2 2 2 )" },
            new Object[] { "GeometryCollection ( )" },
            new Object[] { "GeometryCollectionz ( LINESTRING (1 1, 2 2 )" },
      };

   }

}
