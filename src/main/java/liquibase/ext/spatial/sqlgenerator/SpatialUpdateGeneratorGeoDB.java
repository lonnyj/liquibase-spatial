package liquibase.ext.spatial.sqlgenerator;

import liquibase.database.Database;
import liquibase.database.core.DerbyDatabase;
import liquibase.database.core.H2Database;
import liquibase.statement.core.UpdateStatement;

/**
 * <code>SpatialUpdateGeneratorGeoDB</code> generates the SQL for <code>UPDATING</code>ing
 * geometries in Apache Derby and H2.
 */
public class SpatialUpdateGeneratorGeoDB extends AbstractSpatialUpdateGenerator {
   @Override
   public boolean supports(final UpdateStatement statement, final Database database) {
      return database instanceof DerbyDatabase || database instanceof H2Database;
   }

   /**
    * Returns the name of the function that converts Well-Known Text to a database-specific
    * geometry.
    * 
    * @return the name of the function that converts WKT to a geometry.
    */
   @Override
   protected String getGeomFromWktFunction() {
      return "ST_GeomFromText";
   }
}
