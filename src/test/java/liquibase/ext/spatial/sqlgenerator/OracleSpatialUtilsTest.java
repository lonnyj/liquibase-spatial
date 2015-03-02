package liquibase.ext.spatial.sqlgenerator;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

import liquibase.database.Database;
import liquibase.ext.spatial.sqlgenerator.WktConversionUtils.EWKTInfo;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * <code>OracleSpatialUtilsTest</code> tests {@link OracleSpatialUtils}.
 */
public class OracleSpatialUtilsTest {
   
   
   /**
    * Test 
    * {@link OracleSpatialUtils#getNativeOracleConstructor(liquibase.ext.spatial.sqlgenerator.WktConversionUtils.EWKTInfo, liquibase.database.Database)} 
    */
   @Test(dataProvider="getNativeOracleConstructorTestData")
   public void testGetNativeOracleConstructor(String wkt, String expression) {
      EWKTInfo info = WktConversionUtils.getWktInfo(wkt);
      final Database database = mock(Database.class);
      String result = OracleSpatialUtils.getNativeOracleConstructor(info,null, database);
      assertEquals(result, expression);
   }
   
   @DataProvider
   public Object[][] getNativeOracleConstructorTestData(){
      return new Object[][]{
            // POINT
            new Object[]{"Point(10 10)", "SDO_GEOMETRY(2001,NULL,NULL,SDO_ELEM_INFO_ARRAY(1,1,1),SDO_ORDINATE_ARRAY(10.0,10.0))"},
            new Object[]{"PointM(10 10 3)", "SDO_GEOMETRY(3301,NULL,NULL,SDO_ELEM_INFO_ARRAY(1,1,1),SDO_ORDINATE_ARRAY(10.0,10.0,3.0))"},
            new Object[]{"PointZ(10 10 3)", "SDO_GEOMETRY(3001,NULL,NULL,SDO_ELEM_INFO_ARRAY(1,1,1),SDO_ORDINATE_ARRAY(10.0,10.0,3.0))"},
            new Object[]{"Point ZM(10 10 3 1)", "SDO_GEOMETRY(4401,NULL,NULL,SDO_ELEM_INFO_ARRAY(1,1,1),SDO_ORDINATE_ARRAY(10.0,10.0,3.0,1.0))"},
            new Object[]{"MultiPoint(10.0 10, 20 20, 30 30)", "SDO_GEOMETRY(2005,NULL,NULL,SDO_ELEM_INFO_ARRAY(1,1,1,3,1,1,5,1,1),SDO_ORDINATE_ARRAY(10.0,10.0,20.0,20.0,30.0,30.0))"},
            new Object[]{"MultiPointM(10.0 10 1, 20 20 2, 30 30 1)", "SDO_GEOMETRY(3305,NULL,NULL,SDO_ELEM_INFO_ARRAY(1,1,1,4,1,1,7,1,1),SDO_ORDINATE_ARRAY(10.0,10.0,1.0,20.0,20.0,2.0,30.0,30.0,1.0))"},
            new Object[]{"MultiPointzM(10.0 10 1 1, 20 20 2 2, 30 30 1 1)", "SDO_GEOMETRY(4405,NULL,NULL,SDO_ELEM_INFO_ARRAY(1,1,1,5,1,1,9,1,1),SDO_ORDINATE_ARRAY(10.0,10.0,1.0,1.0,20.0,20.0,2.0,2.0,30.0,30.0,1.0,1.0))"},
            
            // LINESTRING
            new Object[]{"LineSTring(10 10, 20 20)", "SDO_GEOMETRY(2002,NULL,NULL,SDO_ELEM_INFO_ARRAY(1,2,1),SDO_ORDINATE_ARRAY(10.0,10.0,20.0,20.0))"},
            new Object[]{"linestring M(10 10 3, 20 20 1)", "SDO_GEOMETRY(3302,NULL,NULL,SDO_ELEM_INFO_ARRAY(1,2,1),SDO_ORDINATE_ARRAY(10.0,10.0,3.0,20.0,20.0,1.0))"},
            new Object[]{"LINESTRINGZM(10 10 3 1, 20 20 1 1, 30 30 2 2)", "SDO_GEOMETRY(4402,NULL,NULL,SDO_ELEM_INFO_ARRAY(1,2,1),SDO_ORDINATE_ARRAY(10.0,10.0,3.0,1.0,20.0,20.0,1.0,1.0,30.0,30.0,2.0,2.0))"},
            new Object[]{"MultiLINESTRING((10 10, 20 20),(30 30, 2 2))", "SDO_GEOMETRY(2006,NULL,NULL,SDO_ELEM_INFO_ARRAY(1,2,1,5,2,1),SDO_ORDINATE_ARRAY(10.0,10.0,20.0,20.0,30.0,30.0,2.0,2.0))"},
            new Object[]{"MultiLINESTRING z((10 10 1, 20 20 1),(30 30 1, 2 2 1))", "SDO_GEOMETRY(3006,NULL,NULL,SDO_ELEM_INFO_ARRAY(1,2,1,7,2,1),SDO_ORDINATE_ARRAY(10.0,10.0,1.0,20.0,20.0,1.0,30.0,30.0,1.0,2.0,2.0,1.0))"},
            
            
            // Polygon
            new Object[]{"Polygon((10 10, 20 20, 10 10))", "SDO_GEOMETRY(2003,NULL,NULL,SDO_ELEM_INFO_ARRAY(1,1003,1),SDO_ORDINATE_ARRAY(10.0,10.0,20.0,20.0,10.0,10.0))"},
            new Object[]{"    Polygon Z(  (10 10 1   , 20 20 1, 10 10 1))", "SDO_GEOMETRY(3003,NULL,NULL,SDO_ELEM_INFO_ARRAY(1,1003,1),SDO_ORDINATE_ARRAY(10.0,10.0,1.0,20.0,20.0,1.0,10.0,10.0,1.0))"},
            new Object[]{"Polygon m((10 10 1, 20 20 1, 10 10 1))", "SDO_GEOMETRY(3303,NULL,NULL,SDO_ELEM_INFO_ARRAY(1,1003,1),SDO_ORDINATE_ARRAY(10.0,10.0,1.0,20.0,20.0,1.0,10.0,10.0,1.0))"},
            new Object[]{"PolygonZm((10 10 1 1, 20 20      1 1, 10 10 1 1))", "SDO_GEOMETRY(4403,NULL,NULL,SDO_ELEM_INFO_ARRAY(1,1003,1),SDO_ORDINATE_ARRAY(10.0,10.0,1.0,1.0,20.0,20.0,1.0,1.0,10.0,10.0,1.0,1.0))"},
            new Object[]{"  Polygon((10 10, 20 20, 10 10),(11.0 11.0, 19.0 19.0, 11.0 11))", "SDO_GEOMETRY(2003,NULL,NULL,SDO_ELEM_INFO_ARRAY(1,1003,1,7,2003,1),SDO_ORDINATE_ARRAY(10.0,10.0,20.0,20.0,10.0,10.0,11.0,11.0,19.0,19.0,11.0,11.0))"},
            new Object[]{"Polygonz((10 10 1, 20 20 1, 10 10 1),(11.0 11.0 1, 19.0 19.0 1, 11.0 11 1))", "SDO_GEOMETRY(3003,NULL,NULL,SDO_ELEM_INFO_ARRAY(1,1003,1,10,2003,1),SDO_ORDINATE_ARRAY(10.0,10.0,1.0,20.0,20.0,1.0,10.0,10.0,1.0,11.0,11.0,1.0,19.0,19.0,1.0,11.0,11.0,1.0))"},
            new Object[]{" Polygonm((10 10 1, 20 20 1, 10 10 1),(11.0 11.0 1, 19.0 19.0 1, 11.0 11 1))", "SDO_GEOMETRY(3303,NULL,NULL,SDO_ELEM_INFO_ARRAY(1,1003,1,10,2003,1),SDO_ORDINATE_ARRAY(10.0,10.0,1.0,20.0,20.0,1.0,10.0,10.0,1.0,11.0,11.0,1.0,19.0,19.0,1.0,11.0,11.0,1.0))"},
            new Object[]{"MULTIPolygon(((10 10, 20 20, 10 10))      ,((10 10, 20 20, 10 10)))", "SDO_GEOMETRY(2007,NULL,NULL,SDO_ELEM_INFO_ARRAY(1,1003,1,7,1003,1),SDO_ORDINATE_ARRAY(10.0,10.0,20.0,20.0,10.0,10.0,10.0,10.0,20.0,20.0,10.0,10.0))"},
            
            
            // Geometry collections
            new Object[]{"  Geometrycollection  (Point(10    10), Linestring(11 11, 15 15))", "SDO_GEOMETRY(2004,NULL,NULL,SDO_ELEM_INFO_ARRAY(1,1,1,3,2,1),SDO_ORDINATE_ARRAY(10.0,10.0,11.0,11.0,15.0,15.0))"},
            new Object[]{" Geometrycollection z (         Pointz(10 10 1), Linestring Z(11 11 1, 15 15 1))", "SDO_GEOMETRY(3004,NULL,NULL,SDO_ELEM_INFO_ARRAY(1,1,1,4,2,1),SDO_ORDINATE_ARRAY(10.0,10.0,1.0,11.0,11.0,1.0,15.0,15.0,1.0))"},
            new Object[]{"  Geometrycollection m (Pointm(10 10 1), Linestring M(11 11 1, 15 15 1))", "SDO_GEOMETRY(3304,NULL,NULL,SDO_ELEM_INFO_ARRAY(1,1,1,4,2,1),SDO_ORDINATE_ARRAY(10.0,10.0,1.0,11.0,11.0,1.0,15.0,15.0,1.0))"},
            new Object[]{"GeometryCOLLECTION(MULTIPolygon(((10 10, 20 20, 10 10)),((10 10, 20 20, 10 10))), POINT(1 1))", "SDO_GEOMETRY(2004,NULL,NULL,SDO_ELEM_INFO_ARRAY(1,1003,1,7,1003,1,13,1,1),SDO_ORDINATE_ARRAY(10.0,10.0,20.0,20.0,10.0,10.0,10.0,10.0,20.0,20.0,10.0,10.0,1.0,1.0))"},
             
      };
      
   }
   
   /**
    * Test 
    * {@link OracleSpatialUtils#getNativeOracleConstructor(EWKTInfo, String, Integer, Database)}
    * 
    */
   @Test(dataProvider="getNativeOracleConstructorLongTestData")
   public void testGetNativeOracleConstructorLong(String wkt,Integer limit, String expression) {
      EWKTInfo info = WktConversionUtils.getWktInfo(wkt);
      final Database database = mock(Database.class);
      String result = OracleSpatialUtils.getNativeOracleConstructor(info,null, limit, database);
      assertEquals(result, expression);
   }
   @DataProvider
   public Object[][] getNativeOracleConstructorLongTestData(){
      return new Object[][]{
            new Object[]{"GeometryCOLLECTION(MULTIPolygon(((10 10, 20 20, 10 10)),((10 10, 20 20, 10 10))), POINT(1 1))", 3, "SDO_GEOMETRY(2004,NULL,NULL,(select cast(multiset(select to_number(x.column_value.extract('v/text()'))c from table(xmlsequence(xmltype('<r><v>1</v><v>1003</v><v>1</v><v>7</v><v>1003</v><v>1</v><v>13</v><v>1</v><v>1</v></r>').extract('r/v')))x)as SDO_ELEM_INFO_ARRAY)from dual),(select cast(multiset(select to_number(x.column_value.extract('v/text()'))c from table(xmlsequence(xmltype('<r><v>10</v><v>10</v><v>20</v><v>20</v><v>10</v><v>10</v><v>10</v><v>10</v><v>20</v><v>20</v><v>10</v><v>10</v><v>1</v><v>1</v></r>').extract('r/v')))x)as SDO_ORDINATE_ARRAY)from dual))"},
            new Object[]{"GeometryCOLLECTION(MULTIPolygon(((10.1 10.1, 20.1 20.1, 10.1 10.1)),((10.1 10.1, 20.1 20.1, 10.1 10.1))), POINT(1 1))", 3, "SDO_GEOMETRY(2004,NULL,NULL,(select cast(multiset(select to_number(x.column_value.extract('v/text()'))c from table(xmlsequence(xmltype('<r><v>1</v><v>1003</v><v>1</v><v>7</v><v>1003</v><v>1</v><v>13</v><v>1</v><v>1</v></r>').extract('r/v')))x)as SDO_ELEM_INFO_ARRAY)from dual),(select cast(multiset(select to_number(x.column_value.extract('v/text()'))c from table(xmlsequence(xmltype('<r><v>10.1</v><v>10.1</v><v>20.1</v><v>20.1</v><v>10.1</v><v>10.1</v><v>10.1</v><v>10.1</v><v>20.1</v><v>20.1</v><v>10.1</v><v>10.1</v><v>1</v><v>1</v></r>').extract('r/v')))x)as SDO_ORDINATE_ARRAY)from dual))"},
      };
   }
}
