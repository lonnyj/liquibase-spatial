package liquibase.ext.spatial.sqlgenerator;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.ext.spatial.sqlgenerator.WktConversionUtils.EWKTInfo;
import liquibase.util.StringUtils;

/**
 * <code>OracleSpatialUtils</code> provides utility methods for Oracle Spatial.
 */
public class OracleSpatialUtils {

   /**
    * The Oracle function that converts an EPSG SRID to the corresponding Oracle
    * SRID.
    */
   public static final String EPSG_TO_ORACLE_FUNCTION = "SDO_CS.MAP_EPSG_SRID_TO_ORACLE";

   /** The mapping of EPSG SRID to Oracle SRID. */
   private final static Map<String, String> EPSG_TO_ORACLE_MAP = Collections
         .synchronizedMap(new HashMap<String, String>());

   /** Stores if Oracle SRID is projecter or not. */
   private final static Map<String, String> ORACLE_SRID_KIND = Collections
         .synchronizedMap(new HashMap<String, String>());

   public static final String ORACLE_SR_KIND_PROJECTED = "PROJECTED";

   public static final BigDecimal DECIMAL_MULTIPLIER = new BigDecimal(10000000);

   /** Hide the default constructor. */
   private OracleSpatialUtils() {
   }

   /**
    * Converts the given Well-Known Text string to one that will work in Oracle.
    * If the string is greater than 4000 characters, the string is broken into
    * pieces where each piece is converted to a CLOB. The CLOB handling assumes
    * that the result will be wrapped in single quotes so it wraps the result in
    * "<code>' || TO_CLOB(...) || '</code>".
    * 
    * @param wkt the Well-Known Text string to convert.
    * @return the original WKT or a <code>TO_CLOB</code> concatenation of the
    *         WKT.
    */
   public static String getOracleWkt(final String wkt) {
      final StringBuilder oracleWkt = new StringBuilder();
      // Strings longer than 4000 characters need to be converted to CLOBs.
      splitStringIntoClobsExpression(wkt, oracleWkt, true, null);
      return oracleWkt.toString();
   }

   /**
    * If the string is greater than 4000 characters, the string is broken into
    * pieces where each piece is converted to a CLOB. If
    * <code>insideString</code> The CLOB handling assumes that the result will
    * be wrapped in single quotes so it wraps the result in "
    * <code>' || TO_CLOB(...) || '</code>".
    * 
    * @param source
    * @param builder
    * @param insideString
    * @param limit (optional default 4000)
    * @return
    */
   public static void splitStringIntoClobsExpression(final String source,
         StringBuilder builder, boolean insideString, Integer limit) {
      final int curLimit = limit == null ? 4000 : limit;
      if (source.length() > curLimit) {
         int index = curLimit;
         if (insideString) {
            builder.append("' || ");
         }
         builder.append("TO_CLOB('").append(source.substring(0, index))
               .append("')");
         while (index < source.length()) {
            final int endIndex = Math.min(index + curLimit, source.length());
            builder.append(" || TO_CLOB('")
                  .append(source.substring(index, endIndex)).append("')");
            index = endIndex;
         }
         if (insideString) {
            builder.append(" || '");
         }
      }
      else {
         builder.append(source);
      }
   }

   /**
    * Converts the given EPSG SRID to the corresponding Oracle SRID.
    * 
    * @param srid the EPSG SRID.
    * @param database the database instance.
    * @return the corresponding Oracle SRID.
    */
   public static String getOracleSrid(final String srid, final Database database) {
      final String oracleSrid;
      if (StringUtils.trimToNull(srid) == null) {
         oracleSrid = null;
      }
      else if (EPSG_TO_ORACLE_MAP.containsKey(srid)) {
         oracleSrid = EPSG_TO_ORACLE_MAP.get(srid);
      }
      else {
         oracleSrid = loadOracleSrid(srid, database);
         EPSG_TO_ORACLE_MAP.put(srid, oracleSrid);
      }
      return oracleSrid;
   }

   /**
    * @see #getOracleSRIDExpression(String)
    */
   public static String getOracleSRIDExpression(Integer epsgSrid) {
      return getOracleSRIDExpression(String.valueOf(epsgSrid));
   }

   /**
    * Generate Oracle SQL Expression to get Oracle-SRID based on EPSG-SRID
    * 
    * @param epsgSrid
    * @return
    */
   public static String getOracleSRIDExpression(String epsgSrid) {
      // XXX For EPSG25830 SDO_CS.MAP_EPSG_SRID_TO_ORACLE return null
      // but on SDO_COORD_REF_SYSTEM.srid = 25830 matches correctly.
      // So, use COALESCE + Subquery expression to get it:

      // COALESCE( SDO_CS.MAP_EPSG_SRID_TO_ORACLE(25830),
      // (SELECT srid SDO_COORD_REF_SYSTEM where srid = 25830))

      return String
            .format(
                  "COALESCE( %s(%s), (SELECT srid from SDO_COORD_REF_SYSTEM where srid = %s))",
                  OracleSpatialUtils.EPSG_TO_ORACLE_FUNCTION, epsgSrid,
                  epsgSrid);
   }

   /**
    * Queries to the database to convert the given EPSG SRID to the
    * corresponding Oracle SRID.
    * 
    * @param srid the EPSG SRID.
    * @param database the database instance.
    * @return the corresponding Oracle SRID.
    */
   public static String loadOracleSrid(final String srid,
         final Database database) {
      String oracleSrid;
      try {
         oracleSrid = getStringFromQuery("SELECT "
               + getOracleSRIDExpression(srid) + " FROM dual", database);

         if (oracleSrid == null) {
            // for 25830 SDO_CS.MAP_EPSG_SRID_TO_ORACLE returns null when
            // 25830 is registered on SDO_COORD_REF_SYSTEM (???).
            // Try to find in using SDO_COORD_REF_SYSTEM.srid field
            oracleSrid = getStringFromQuery(
                  "SELECT srid from SDO_COORD_REF_SYSTEM FROM dual where srid ="
                        .concat(srid),
                  database);
         }
      }
      catch (final SQLException e) {
         throw new UnexpectedLiquibaseException(
               "Failed to find the Oracle SRID for EPSG:" + srid, e);
      }
      return oracleSrid;
   }

   /**
    * Execute <code>sql</code> query and gets the first result column (as
    * string)
    * 
    * @param sql of query
    * @param database
    * @return first query result column
    * @throws SQLException
    */
   private static String getStringFromQuery(final String sql,
         final Database database) throws SQLException {

      final String result;
      final JdbcConnection jdbcConnection = (JdbcConnection) database
            .getConnection();
      final Connection connection = jdbcConnection.getUnderlyingConnection();
      Statement statement = null;
      try {
         statement = connection.createStatement();
         final ResultSet resultSet = statement.executeQuery(sql);
         resultSet.next();
         result = resultSet.getString(1);
      }
      finally {
         try {
            statement.close();
         }
         catch (final SQLException ignore) {
         }
      }
      return result;
   }

   /**
    * Informs if a EPSG SRID is projected or not (based on Oracle SYS_REF table
    * info)
    * 
    * @param epsgSrid
    * @param database
    * @return true if SRID is projected
    */
   public static boolean isSRIDProjected(String epsgSrid, Database database) {
      final String oracleSrid = getOracleSrid(epsgSrid, database);
      return isOracleSRIDProjected(oracleSrid, database);
   }

   /**
    * Informs if a EPSG SRID is projected or not (based on Oracle SYS_REF table
    * info)
    * 
    * @param epsgSrid
    * @param database
    * @return true if SRID is projected
    */
   public static boolean isOracleSRIDProjected(String oraSrid, Database database) {
      String kind = getOracleSRIDKind(oraSrid, database);
      return ORACLE_SR_KIND_PROJECTED.equalsIgnoreCase(kind);

   }

   /**
    * Gets COORD_REF_SYS_KIND of a Oracle SRID
    * 
    * @param oraSrid
    * @param database
    * @return COORD_REF_SYS_KIND of the Oracle SRID
    */
   public static String getOracleSRIDKind(String oraSrid, Database database) {
      String kind = ORACLE_SRID_KIND.get(oraSrid);
      if (kind == null) {
         try {
            kind = getStringFromQuery(
                  "Select COORD_REF_SYS_KIND from SDO_COORD_REF_SYSTEM where srid = "
                        .concat(oraSrid),
                  database);
            ORACLE_SRID_KIND.put(oraSrid, kind);
         }
         catch (final SQLException e) {
            throw new UnexpectedLiquibaseException(
                  "Failed to indentify if the Oracle SRID is projected:"
                        + oraSrid, e);
         }
      }
      return kind;
   }

   /**
    * Generates SDO_GEOMETRY constructor (native geometry constructor)
    * expression for a WKT. <br/>
    * This generates a expression using constructor call:
    * <code>SDO_GEOMETRY(SDO_GTYPE, SDO_SRID , SDO_POINT, SDO_ELEM_INFO,
    * SDO_ORDINATES)</code> which defines a Oracle Geometry. <br/>
    * Example, for the WKT string: <br/>
    * <code>POLYGON(3 3, 6 3, 6 5, 4 5, 3 3)</code> <br/>
    * This method generates: <br/>
    * <code>SDO_GEOMETRY(2003,NULL,NULL,SDO_ELEM_INFO_ARRAY(1,1003,1),
    * SDO_ORDINATE_ARRAY(3,3,6,3,6,5,4,5,3,3))</code>
    * 
    * 
    * @param wktInfo
    * @param srid
    * @param database
    * @return SDO_GEOMETRY constructor-call string
    */
   public static String getNativeOracleConstructor(final EWKTInfo wktInfo,
         final String srid, final Database database) {
      return getNativeOracleConstructor(wktInfo, srid, null, database);
   }

   /**
    * Generates SDO_GEOMETRY constructor (native geometry constructor)
    * expression for a WKT string. <br/>
    * This method generates a expression using constructor call:
    * <code>SDO_GEOMETRY(SDO_GTYPE, SDO_SRID , SDO_POINT, SDO_ELEM_INFO,
    * SDO_ORDINATES)</code> which defines a Oracle Geometry. <br/>
    * Example, for the WKT string: <br/>
    * <code>POLYGON(3 3, 6 3, 6 5, 4 5, 3 3)</code> <br/>
    * This method generates: <br/>
    * <code>SDO_GEOMETRY(2003,NULL,NULL,SDO_ELEM_INFO_ARRAY(1,1003,1),
    * SDO_ORDINATE_ARRAY(3,3,6,3,6,5,4,5,3,3))</code> <br/>
    * <code>limit</code> defines the maximum number of parameters allowed on
    * array creation. If arrays are bigger, will be generate expression to avoid
    * it (this is related to <code>ORA-00939</code>. Uses expressions based
    * on xml-structure oracle support to load the array as a string.
    * 
    * 
    * @param wktInfo
    * @param srid
    * @param limit (optional default 900) max array limit
    * @param database
    * @return SDO_GEOMETRY constructor-call string
    */
   public static String getNativeOracleConstructor(final EWKTInfo wktInfo,
         final String srid, Integer arrayLimit, final Database database) {

      // Prepare common data
      String sdoGtype = getSDO_GTYPE(wktInfo);
      String oracleSRID = "NULL";
      if (srid != null) {
         String tmpOracleSRID = getOracleSrid(srid, database);
         if (tmpOracleSRID != null && !tmpOracleSRID.isEmpty()) {
            oracleSRID = tmpOracleSRID;
         }
      }
      else if (wktInfo.getSrid() != null) {
         String tmpOracleSRID = getOracleSrid(wktInfo.getSrid(), database);
         if (tmpOracleSRID != null && !tmpOracleSRID.isEmpty()) {
            oracleSRID = tmpOracleSRID;
         }
      }

      // Function call
      StringBuilder sbuilder = new StringBuilder("SDO_GEOMETRY(");

      // Append SDO_GTYPE
      sbuilder.append(sdoGtype).append(',');

      // Append SDO_SRID
      sbuilder.append(oracleSRID).append(',');

      // Append SDO_POINT (not used)
      sbuilder.append("NULL,");

      // Prepare SDO_ELEMENT_INFO_ARRAY
      List<String> elementInfo = new ArrayList<String>();

      // Prepare SDO_ORDINATES_ARRAY
      List<String> ordinates = new ArrayList<String>();

      // Process WKT to generate SDO_ELEM_INFO_ARRAY and SDO_ORDINATES_ARRAY
      OracleWktCoordiantesProcessor processor = new OracleWktCoordiantesProcessor();
      try {
         processor.process(wktInfo.getWktWithoutSRID(), elementInfo, ordinates);
      }
      catch (IOException e) {
         throw new IllegalArgumentException("Invalid WKT", e);
      }

      // Append SDO_ELEMENT_INFO and prepare coordinates
      writeSdoArray(elementInfo,"SDO_ELEM_INFO_ARRAY", arrayLimit, sbuilder);
      sbuilder.append(",");
      // sbuilder.append("SDO_ELEM_INFO_ARRAY(");
      // writeCommaSeparatedList(elementInfo, sbuilder);
      // sbuilder.append("),");

      // Append SDO_ORDINATES
      writeSdoArray(ordinates,"SDO_ORDINATE_ARRAY", arrayLimit, sbuilder);
      // sbuilder.append("SDO_ORDINATE_ARRAY(");
      // writeCommaSeparatedList(ordinates, sbuilder);
      // sbuilder.append(')');

      return sbuilder.append(')').toString();
   }

   /**
    * Write on a {@link StringBuilder} constructor expression to generate a
    * SDO_ELEM_INFO_ARRAY or SDO_ORDINATE_ARRAY
    * 
    * @param arrayItems
    * @param type
    * @param limit of array items to use XML-expression way
    * @param sbuilder
    */
   private static void writeSdoArray(List<String> arrayItems, String type, Integer limit,
         StringBuilder sbuilder) {

      int curLimit = limit == null ? 900 : limit;

      if (arrayItems.size() < curLimit) {
         writeConstructorWithNumericArray(type, arrayItems,
               sbuilder);
      }
      else {
         /**
          * (select cast(multiset(select to_number(x.column_value.extract('v/text()'))c from table(xmlsequence(xmltype('<r><v>1.2</v><v>2332322.3333</v></r>').extract('r/v')))x)as SDO_ELEM_INFO_ARRAY)from dual)
          */
         sbuilder
               .append("(select cast(multiset(select to_number(x.column_value.extract('v/text()'))c from table(xmlsequence(xmltype('<r>");

         // Avoid long string problem
         StringBuilder tmpBuilder = new StringBuilder();
         writeXmlList(arrayItems, "v", tmpBuilder);
         splitStringIntoClobsExpression(tmpBuilder.toString(), sbuilder, true,
               null);

         sbuilder.append("</r>').extract('r/v')))x)as ").append(type).append(")from dual)");
      }
   }

   /**
    * Write on a {@link StringBuilder} constructor expression which receives
    * an array of numeric elements.
    * 
    * @param contructorFunction
    * @param arrayItems
    * @param sbuilder
    */
   private static <T> void writeConstructorWithNumericArray(
         String contructorFunction, List<T> arrayItems, StringBuilder sbuilder) {
      sbuilder.append(contructorFunction).append("(");
      writeCommaSeparatedList(arrayItems, sbuilder);
      sbuilder.append(')');
   }


   /**
    * Write on a {@link StringBuilder} a list of elements as a XML
    * tag sequence
    * 
    * @param list
    * @param tagName
    * @param sbuilder
    */
   private static void writeXmlList(List<String> list,
         String tagName,StringBuilder sbuilder) {
      final String start = "<".concat(tagName).concat(">");
      final String end = "</".concat(tagName).concat(">");
      for (String value : list) {
         sbuilder.append(start).append(value).append(end);
      }
   }

   /**
    * Write on a {@link StringBuilder} a list of elements as a comma 
    * separated list
    * 
    * @param list
    * @param sbuilder
    */
   private static <T> void writeCommaSeparatedList(List<T> list,
         StringBuilder sbuilder) {
      final int last = list.size() - 1;
      for (int i = 0; i < last; i++) {
         sbuilder.append(list.get(i));
         sbuilder.append(',');
      }
      sbuilder.append(list.get(last));
   }

   /**
    * Return the <code>SDO_GTYPE</code> of a geometry based on wktInfo
    * 
    * @param wktInfo
    * @return
    */
   public static String getSDO_GTYPE(final EWKTInfo info) {
      // Prepares number of dimensions and M coordinate position
      int d = getDimensions(info);
      int l = 0;

      // if has M this will be the last coordinate
      if (info.isMCoordinate()) {
         l = d;
      }

      // Identifies the type
      int t = 0;
      if (info.isCollection()) {
         t = 4;
      }
      else {
         final String baseType = info.getGeometryBaseType();
         if ("POINT".equals(baseType)) {
            t = 1;
         }
         else if ("LINESTRING".equals(baseType)) {
            t = 2;
         }
         else if ("POLYGON".equals(baseType)) {
            t = 3;
         }
         else {
            throw new IllegalArgumentException(
                  "Unknow base geometry type: ".concat(baseType));
         }
         if (info.isMulti()) {
            t = t + 4;
         }
      }

      return String.format("%s%s0%s", d, l, t);
   }

   /**
    * Get number of dimensions of a WKT definition
    * 
    * @param info
    * @return
    */
   private static int getDimensions(EWKTInfo info) {
      int n = 2;
      if (info.isZCoordinate()) {
         n++;
      }
      if (info.isMCoordinate()) {
         n++;
      }
      return n;
   }
}
