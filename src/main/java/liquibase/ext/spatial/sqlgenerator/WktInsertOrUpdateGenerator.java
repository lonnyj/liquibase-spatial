package liquibase.ext.spatial.sqlgenerator;

import liquibase.database.Database;

public interface WktInsertOrUpdateGenerator {

   /**
    * Converts the given Well-Known Text and SRID to the appropriate function call for the database.
    * 
    * @param wkt
    *           the Well-Known Text string.
    * @param srid
    *           the SRID string which may be an empty string.
    * @param database
    *           the database instance.
    * @return the string that converts the WKT to a database-specific geometry.
    */
   String convertToFunction(String wkt, String sridString, Database database);

   /**
    * Returns the name of the function that converts Well-Known Text to a database-specific
    * geometry.
    * 
    * @return the name of the function that converts WKT to a geometry.
    */
   String getGeomFromWktFunction();

   /**
    * Indicates if the SRID parameter is required in the function returned from
    * {@link #getGeomFromWktFunction()}.
    * 
    * @param database
    *           the database instance.
    * 
    * @return <code>true</code> if the SRID parameter is required in order to invoke the function.
    */
   boolean isSridRequiredInFunction(Database database);
}
