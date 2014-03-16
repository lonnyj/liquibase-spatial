package liquibase.ext.spatial.sqlgenerator;

import liquibase.database.Database;
import liquibase.database.core.OracleDatabase;
import liquibase.statement.core.UpdateStatement;

/**
 * The <code>SpatialUpdateGeneratorH2</code> generates the SQL for <code>UPDATE</code>ing geometries
 * in Oracle.
 */
public class SpatialUpdateGeneratorOracle extends AbstractSpatialUpdateGenerator {
   /**
    * Verifies that the <code>UpdateStatement</code> has WKT or EWKT.
    */
   @Override
   public boolean supports(final UpdateStatement statement, final Database database) {
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
   protected String convertToFunction(final String wkt, final String srid) {
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
      final String oracleSrid = "SDO_CS.MAP_EPSG_TO_ORACLE(" + srid + ")";
      return super.convertToFunction(oracleWkt, oracleSrid);
   }
}
