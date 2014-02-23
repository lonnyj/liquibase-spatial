package liquibase.ext.spatial.datatype;

import liquibase.database.Database;
import liquibase.database.core.H2Database;
import liquibase.database.core.OracleDatabase;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.statement.DatabaseFunction;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

/**
 * The <code>GeometryType</code> assists in defining database-specific geometry types and converting
 * SQL representations of geometries.
 */
@DataTypeInfo(name = "geometry", aliases = { "com.vividsolutions.jts.geom.Geometry" }, minParameters = 0, maxParameters = 2, priority = LiquibaseDataType.PRIORITY_DEFAULT)
public class GeometryType extends LiquibaseDataType {
   /**
    * Creates the appropriate Geometry <code>DatabaseDataType</code>.
    */
   @Override
   public DatabaseDataType toDatabaseDataType(final Database database) {
      final DatabaseDataType databaseDataType;
      if (database instanceof H2Database) {
         // TODO: Make this configurable as BLOB may also be desired for very large geometries.
         databaseDataType = new DatabaseDataType("BINARY");
      } else if (database instanceof OracleDatabase) {
         databaseDataType = new DatabaseDataType("SDO_GEOMETRY");
      } else {
         databaseDataType = new DatabaseDataType("GEOMETRY");
      }
      return databaseDataType;
   }

   /**
    * @see liquibase.datatype.LiquibaseDataType#objectToSql(java.lang.Object,
    *      liquibase.database.Database)
    */
   @Override
   public String objectToSql(final Object value, final Database database) {
      final String returnValue;
      if (value instanceof Geometry) {
         // TODO: Tailor the output for the database.
         returnValue = ((Geometry) value).toText();
      } else if (value instanceof String) {
         returnValue = value.toString();
      } else if (value instanceof DatabaseFunction) {
         returnValue = value.toString();
      } else if (value == null || value.toString().equalsIgnoreCase("null")) {
         returnValue = null;
      } else {
         throw new UnexpectedLiquibaseException("Cannot convert type " + value.getClass()
               + " to a Geometry value");
      }
      return returnValue;
   }

   /**
    * @see liquibase.datatype.LiquibaseDataType#sqlToObject(java.lang.String,
    *      liquibase.database.Database)
    */
   @Override
   public Object sqlToObject(final String value, final Database database) {
      final Geometry returnValue;
      if (value == null || value.equalsIgnoreCase("null")) {
         returnValue = null;
      } else {
         final WKTReader reader = new WKTReader();
         try {
            // TODO: Check for SRID.
            returnValue = reader.read(value);
         } catch (final ParseException e) {
            throw new UnexpectedLiquibaseException("Cannot parse " + value + " to a Geometry", e);
         }
      }
      return returnValue;
   }
}
