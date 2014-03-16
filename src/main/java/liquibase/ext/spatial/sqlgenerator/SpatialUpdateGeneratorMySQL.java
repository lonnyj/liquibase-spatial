package liquibase.ext.spatial.sqlgenerator;

import liquibase.database.Database;
import liquibase.database.core.MySQLDatabase;
import liquibase.statement.core.UpdateStatement;

/**
 * The <code>SpatialUpdateGeneratorMySQL</code> generates the SQL for <code>UPDATE</code>ing
 * geometries in MySQL.
 */
public class SpatialUpdateGeneratorMySQL extends AbstractSpatialUpdateGenerator {
   @Override
   public boolean supports(final UpdateStatement statement, final Database database) {
      return database instanceof MySQLDatabase;
   }

   /**
    * Returns the name of the function that converts Well-Known Text to a database-specific
    * geometry.
    * 
    * @return the name of the function that converts WKT to a geometry.
    */
   @Override
   protected String getGeomFromWktFunction() {
      return "GeomFromText";
   }
}
