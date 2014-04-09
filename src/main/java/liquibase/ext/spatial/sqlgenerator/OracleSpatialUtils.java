package liquibase.ext.spatial.sqlgenerator;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.util.StringUtils;

/**
 * <code>OracleSpatialUtils</code> provides utility methods for Oracle Spatial.
 */
public class OracleSpatialUtils {
   /** The Oracle function that converts an EPSG SRID to the corresponding Oracle SRID. */
   public static final String EPSG_TO_ORACLE_FUNCTION = "SDO_CS.MAP_EPSG_SRID_TO_ORACLE";

   /** The mapping of EPSG SRID to Oracle SRID. */
   private final static Map<String, String> EPSG_TO_ORACLE_MAP = Collections
         .synchronizedMap(new HashMap<String, String>());

   /** Hide the default constructor. */
   private OracleSpatialUtils() {
   }

   /**
    * Converts the given Well-Known Text string to one that will work in Oracle. If the string is
    * greater than 4000 characters, the string is broken into pieces where each piece is converted
    * to a CLOB. The CLOB handling assumes that the result will be wrapped in single quotes so it
    * wraps the result in "<code>' || TO_CLOB(...) || '</code>".
    * 
    * @param wkt
    *           the Well-Known Text string to convert.
    * @return the original WKT or a <code>TO_CLOB</code> concatenation of the WKT.
    */
   public static String getOracleWkt(final String wkt) {
      final String oracleWkt;
      // Strings longer than 4000 characters need to be converted to CLOBs.
      if (wkt.length() > 4000) {
         int index = 4000;
         final StringBuilder clobs = new StringBuilder("' || TO_CLOB('").append(
               wkt.substring(0, index)).append("')");
         while (index < wkt.length()) {
            final int endIndex = Math.min(index + 4000, wkt.length());
            clobs.append(" || TO_CLOB('").append(wkt.substring(index, endIndex)).append("')");
            index = endIndex;
         }
         clobs.append(" || '");
         oracleWkt = clobs.toString();
      } else {
         oracleWkt = wkt;
      }
      return oracleWkt;
   }

   /**
    * Converts the given EPSG SRID to the corresponding Oracle SRID.
    * 
    * @param srid
    *           the EPSG SRID.
    * @param database
    *           the database instance.
    * @return the corresponding Oracle SRID.
    */
   public static String getOracleSrid(final String srid, final Database database) {
      final String oracleSrid;
      if (StringUtils.trimToNull(srid) == null) {
         oracleSrid = null;
      } else if (EPSG_TO_ORACLE_MAP.containsKey(srid)) {
         oracleSrid = EPSG_TO_ORACLE_MAP.get(srid);
      } else {
         oracleSrid = loadOracleSrid(srid, database);
         EPSG_TO_ORACLE_MAP.put(srid, oracleSrid);
      }
      return oracleSrid;
   }

   /**
    * Queries to the database to convert the given EPSG SRID to the corresponding Oracle SRID.
    * 
    * @param srid
    *           the EPSG SRID.
    * @param database
    *           the database instance.
    * @return the corresponding Oracle SRID.
    */
   public static String loadOracleSrid(final String srid, final Database database) {
      final String oracleSrid;
      final JdbcConnection jdbcConnection = (JdbcConnection) database.getConnection();
      final Connection connection = jdbcConnection.getUnderlyingConnection();
      Statement statement = null;
      try {
         statement = connection.createStatement();
         final ResultSet resultSet = statement.executeQuery("SELECT " + EPSG_TO_ORACLE_FUNCTION
               + "(" + srid + ") FROM dual");
         resultSet.next();
         oracleSrid = resultSet.getString(1);
      } catch (final SQLException e) {
         throw new UnexpectedLiquibaseException("Failed to find the Oracle SRID for EPSG:" + srid,
               e);
      } finally {
         try {
            statement.close();
         } catch (final SQLException ignore) {
         }
      }
      return oracleSrid;
   }
}
