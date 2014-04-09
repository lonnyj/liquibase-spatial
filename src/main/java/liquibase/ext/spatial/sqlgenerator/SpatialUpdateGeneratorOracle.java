package liquibase.ext.spatial.sqlgenerator;

import liquibase.database.Database;
import liquibase.database.core.OracleDatabase;
import liquibase.statement.core.UpdateStatement;

/**
 * The <code>SpatialUpdateGeneratorOracle</code> generates the SQL for <code>UPDATE</code>ing
 * geometries in Oracle.
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
   public String convertToFunction(final String wkt, final String srid, final Database database) {
      final String oracleWkt = OracleSpatialUtils.getOracleWkt(wkt);
      final String oracleSrid = OracleSpatialUtils.getOracleSrid(srid, database);
      return super.convertToFunction(oracleWkt, oracleSrid, database);
   }
}
