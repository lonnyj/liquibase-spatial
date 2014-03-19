package liquibase.ext.spatial.sqlgenerator;

import liquibase.database.Database;
import liquibase.database.core.DerbyDatabase;
import liquibase.database.core.H2Database;
import liquibase.statement.core.InsertStatement;

/**
 * The <code>SpatialInsertGeneratorH2</code> generates the SQL for <code>INSERT</code>ing geometries
 * into Apache Derby and H2.
 */
public class SpatialInsertGeneratorGeoDB extends AbstractSpatialInsertGenerator {
   @Override
   public boolean supports(final InsertStatement statement, final Database database) {
      return database instanceof DerbyDatabase || database instanceof H2Database;
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

   /**
    * Always returns <code>true</code> for GeoDB.
    * 
    * @see AbstractSpatialInsertGenerator#isSridRequiredInFunction(Database)
    */
   @Override
   public boolean isSridRequiredInFunction(final Database database) {
      return true;
   }
}
