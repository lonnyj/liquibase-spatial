package liquibase.ext.spatial.sqlgenerator;

import liquibase.database.Database;
import liquibase.database.core.MySQLDatabase;
import liquibase.statement.core.InsertStatement;

/**
 * The <code>SpatialInsertGeneratorH2</code> generates the SQL for <code>INSERT</code>ing geometries
 * into MySQL.
 */
public class SpatialInsertGeneratorMySQL extends AbstractSpatialInsertGenerator {
   @Override
   public boolean supports(final InsertStatement statement, final Database database) {
      return database instanceof MySQLDatabase;
   }

   /**
    * Returns the name of the function that converts Well-Known Text to a database-specific
    * geometry.
    * 
    * @return the name of the function that converts WKT to a geometry.
    */
   @Override
   public String getGeomFromWktFunction() {
      return "ST_GeomFromText";
   }
}
