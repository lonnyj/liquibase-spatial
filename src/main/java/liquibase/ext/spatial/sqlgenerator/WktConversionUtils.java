package liquibase.ext.spatial.sqlgenerator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import liquibase.database.Database;

import com.vividsolutions.jts.geom.Geometry;

public class WktConversionUtils {
   /** The SRID regular expression. */
   public static final String SRID_REGEX = "SRID[\\s]*=[\\s]*([0-9]+)[\\s]*;";

   /** The WKT regular expression. */
   public static final String WKT_REGEX = "((((MULTI)?(POINT|LINESTRING|POLYGON)|GEOMETRYCOLLECTION)Z?M?)[(].*[)])";

   /** The PostGIS EWKT regular expression. */
   public static final String EWKT_REGEX = "(" + SRID_REGEX + "[\\s]*)?" + WKT_REGEX;

   /** The EWKT <code>Pattern</code> instance. */
   public static final Pattern EWKT_PATTERN = Pattern.compile(EWKT_REGEX, Pattern.CASE_INSENSITIVE);

   /** Hide the default constructor. */
   private WktConversionUtils() {
   }

   /**
    * If the old value is a geometry or a Well-Known Text, convert it to the appropriate new value.
    * Otherwise, this method returns the old value.
    * 
    * @param oldValue
    *           the old value.
    * @param database
    *           the database instance.
    * 
    * @return the new value.
    */
   public static Object handleColumnValue(final Object oldValue, final Database database,
         final WktInsertOrUpdateGenerator generator) {
      Object newValue = oldValue;
      if (oldValue instanceof Geometry) {
         final Geometry geometry = (Geometry) oldValue;
         final String wkt = geometry.toText();
         String sridString = null;
         if (geometry.getSRID() > 0) {
            sridString = String.valueOf(geometry.getSRID());
         }
         newValue = generator.convertToFunction(wkt, sridString, database);
      } else if (oldValue instanceof String) {
         final String value = oldValue.toString().trim();
         final Matcher matcher = EWKT_PATTERN.matcher(value);
         if (matcher.matches()) {
            final String sridString = matcher.group(2);
            final String wkt = matcher.group(3);
            final String function = generator.convertToFunction(wkt, sridString, database);
            newValue = function;
         }
      }
      return newValue;
   }

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
   public static String convertToFunction(final String wkt, final String srid,
         final Database database, final WktInsertOrUpdateGenerator generator) {
      if (wkt == null || wkt.equals("")) {
         throw new IllegalArgumentException("The Well-Known Text cannot be null or empty");
      }
      if (generator == null) {
         throw new IllegalArgumentException("The generator cannot be null or empty");
      }
      final String geomFromTextFunction = generator.getGeomFromWktFunction();
      String function = geomFromTextFunction + "('" + wkt + "'";
      if (srid != null && !srid.equals("")) {
         function += ", " + srid;
      } else if (generator.isSridRequiredInFunction(database)) {
         throw new IllegalArgumentException("An SRID was not provided with '" + wkt
               + "' but is required in call to '" + geomFromTextFunction + "'");
      }
      function += ")";
      return function;
   }
}
