package liquibase.ext.spatial.sqlgenerator;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import liquibase.database.Database;
import liquibase.database.core.OracleDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.statement.core.InsertStatement;

/**
 * The <code>SpatialInsertGeneratorOracle</code> generates the SQL for <code>INSERT</code>ing
 * geometries into Oracle.
 */
public class SpatialInsertGeneratorOracle extends AbstractSpatialInsertGenerator {
   /** The mapping of EPSG SRID to Oracle SRID. */
   private final Map<String, String> epsgToOracleMap = Collections
         .synchronizedMap(new HashMap<String, String>());

   /**
    * Verifies that the <code>InsertStatement</code> has WKT or EWKT.
    */
   @Override
   public boolean supports(final InsertStatement statement, final Database database) {
      return database instanceof OracleDatabase;
   }

   /**
    * Returns the name of the function that converts Well-Known Text to a database-specific
    * geometry.
    * 
    * @return the name of the function that converts WKT to a geometry.
    */
   @Override
   public String getGeomFromWktFunction() {
      return "SDO_GEOMETRY";
   }

   /**
    * Handles the Well-Known Text and SRID for Oracle.
    */
   @Override
   public String convertToFunction(final String wkt, final String srid, final Database database) {
      final String oracleWkt;

      // Strings longer than 4000 characters need to be converted to CLOBs.
      if (wkt.length() > 4000) {
         int index = 4000;
         final StringBuilder clobs = new StringBuilder("TO_CLOB('").append(wkt.substring(0, index))
               .append("')");
         while (index < wkt.length()) {
            final int endIndex = Math.min(index + 4000, wkt.length());
            clobs.append(" || TO_CLOB('").append(wkt.substring(index, endIndex)).append("')");
            index = endIndex;
         }
         oracleWkt = clobs.toString();
      } else {
         oracleWkt = wkt;
      }

      final String oracleSrid;
      if (srid != null && !srid.equals("")) {
         if (this.epsgToOracleMap.containsKey(srid)) {
            oracleSrid = this.epsgToOracleMap.get(srid);
         } else {
            oracleSrid = getOracleSrid(srid, database);
            this.epsgToOracleMap.put(srid, oracleSrid);
         }
      } else {
         oracleSrid = null;
      }
      return super.convertToFunction(oracleWkt, oracleSrid, database);
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
   protected String getOracleSrid(final String srid, final Database database) {
      final String oracleSrid;
      final JdbcConnection jdbcConnection = (JdbcConnection) database.getConnection();
      final Connection connection = jdbcConnection.getUnderlyingConnection();
      Statement statement;
      ResultSet resultSet;
      try {
         statement = connection.createStatement();
         resultSet = statement.executeQuery("SELECT SDO_CS.MAP_EPSG_TO_ORACLE(" + srid
               + ") FROM dual");
         resultSet.next();
         oracleSrid = resultSet.getString(1);
         statement.close();
      } catch (final SQLException e) {
         throw new UnexpectedLiquibaseException("Failed to find the Oracle SRID for EPSG:" + srid,
               e);
      } finally {
         try {
            connection.close();
         } catch (final SQLException ignore) {
         }
      }
      return oracleSrid;
   }
}
