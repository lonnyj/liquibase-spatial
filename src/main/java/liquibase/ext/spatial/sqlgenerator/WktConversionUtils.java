package liquibase.ext.spatial.sqlgenerator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import liquibase.database.Database;

import com.vividsolutions.jts.geom.Geometry;

public class WktConversionUtils {
   
   /**
    * WKT Point geometry type
    */
   public static final String T_POINT = "POINT";

   /**
    * WKT Line string geometry type
    */
   public static final String T_LINESTRING = "LINESTRING";

   /**
    * WKT Polygon geometry type
    */
   public static final String T_POLYGON = "POLYGON";

   /**
    * WKT Geometry collection type
    */
   public static final String T_GEOMETRY_COLLECTION = "GEOMETRYCOLLECTION";
   
   /** The SRID regular expression. */
   public static final String SRID_REGEX = "SRID[\\s]*=[\\s]*([0-9]+)[\\s]*;";
   

   /** The WKT regular expression. */
   public static final String WKT_REGEX = "((((MULTI)?("+T_POINT+"|"+T_LINESTRING+"|"+T_POLYGON+")|"+ T_GEOMETRY_COLLECTION+ ")[\\s]?(Z)?(M)?)[\\s]*[(](.*)[)])";

   /** The PostGIS EWKT regular expression. */
   public static final String EWKT_REGEX = "(" + SRID_REGEX + "([\\s]*))?" + WKT_REGEX;

   /** The EWKT <code>Pattern</code> instance. */
   public static final Pattern EWKT_PATTERN = Pattern.compile(EWKT_REGEX, Pattern.CASE_INSENSITIVE);
   
   /** EWKT <code>Pattern</code> groups constants **/
   public static final int G_SRID_DEFINITION = 1;
   public static final int G_SRID = 2;
   public static final int G_WKT_WITHOUT_SRID = 4;
   public static final int G_GEOMETRY_TYPE = 5;
   public static final int G_GEOMETRY_OR_COLLECTION = 6;
   public static final int G_MULTI = 7;
   public static final int G_BASE_GEOMETRY_TYPE = 8;
   public static final int G_HAS_Z = 9;
   public static final int G_HAS_M = 10;
   public static final int G_DATA = 11;

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
            final String sridString = matcher.group(G_SRID);
            final String wkt = matcher.group(G_WKT_WITHOUT_SRID);
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
   
    /**
     * Parse information about a WKT 
     * 
     * @param wkt
     * @return
     */
    public static EWKTInfo getWktInfo(String wkt) {
        if (wkt == null || wkt.equals("")) {
            throw new IllegalArgumentException("The Well-Known Text cannot be null or empty");
         }
        final String value = wkt.trim();
        final Matcher matcher = EWKT_PATTERN.matcher(value);
        if (matcher.matches()) {
            return new EWKTInfo(value, matcher);
        } else {
            throw new IllegalArgumentException("The Well-Known Text invalid");
        }
    }
   
    /**
     * Object which contains useful information about a WKT string 
     */
    public static class EWKTInfo {

        private final String originalWkt;

        private final String wktWithoutSRID;

        private final boolean isV1_1;

        private final String srid;
        
        private final String geometryType;

        private final String geometryBaseType;

        private final boolean mCoordinate;

        private final boolean zCoordinate;
        
        private final boolean isMulti;

        private final boolean isCollection;

        private final String data;
        
        private EWKTInfo(String originalWkt, Matcher ewktMatcher) {
            this.originalWkt = originalWkt;
            final String sridString = ewktMatcher.group(G_SRID);
            if (sridString == null || sridString.isEmpty()){
                srid = null;
            } else {
                srid = sridString;
            }
            wktWithoutSRID = ewktMatcher.group(G_WKT_WITHOUT_SRID);
            zCoordinate = "Z".equalsIgnoreCase(ewktMatcher.group(G_HAS_Z));
            mCoordinate = "M".equalsIgnoreCase(ewktMatcher.group(G_HAS_M));
            isCollection = T_GEOMETRY_COLLECTION.equalsIgnoreCase(ewktMatcher.group(G_GEOMETRY_OR_COLLECTION));
            if (isCollection) {
                geometryBaseType = T_GEOMETRY_COLLECTION;
                String tmpGeometryType= T_GEOMETRY_COLLECTION;
                if (this.zCoordinate) {
                   tmpGeometryType = tmpGeometryType.concat("Z");
                }
                if (this.mCoordinate) {
                   tmpGeometryType = tmpGeometryType.concat("M");
                }
                geometryType = tmpGeometryType;
            } else {
               geometryType = ewktMatcher.group(G_GEOMETRY_TYPE).toUpperCase().replaceAll("[\\s]", "");
               if (geometryType == null) {
                  throw new IllegalArgumentException("Invalid WKT: Missing geomtry type");
               }
               geometryBaseType = ewktMatcher.group(G_BASE_GEOMETRY_TYPE).toUpperCase();
            }
            isMulti = "MULTI".equalsIgnoreCase(ewktMatcher.group(G_MULTI));
            data = ewktMatcher.group(G_DATA);
            isV1_1 = srid == null && !mCoordinate && !zCoordinate;
        }

        /**
         * @return the original wkt string
         */
        public String getOriginalWkt() {
            return originalWkt;
        }

        /**
         * @return the SRID (if any)
         */
        public String getSrid() {
            return srid;
        }

        /**
         * @return geometry type (POINT, MULTIPOINT, POINTZM,....)
         */
        public String getGeometryType() {
            return geometryType;
        }

        /**
         * @return geometry type has M coordinate
         */
        public boolean isMCoordinate() {
            return mCoordinate;
        }

        /**
         * @return geometry type has Z coordinate
         */
        public boolean isZCoordinate() {
            return zCoordinate;
        }

        /**
         * @return coordinates (or geometry collection) part of WKT
         */
        public String getData() {
            return data;
        }

        /**
         * @return is WKT full suitable of OGC WKT 1.1 version
         */
        public boolean isV1_1() {
            return isV1_1;
        }

        /**
         * @return is Multi geometry
         */
        public boolean isMulti() {
            return isMulti;
        }

        /**
         * @return is a Geometry Collection
         */
        public boolean isCollection() {
            return isCollection;
        }
        
        /**
         * @return geometry base type (one of 'POINT', 'LINESTRING', 'POLYGON')
         */
        public String getGeometryBaseType() {
            return geometryBaseType;
        }

      /**
       * @return original WKT without SRID definition part
       */
      public String getWktWithoutSRID() {
         return wktWithoutSRID;
      }
    }

   /**
    * Informs if OGC <code>geometryType</code> is defined with <em>Z</em>
    * coordinates.<br/>
    * Examples:<ul>
    * <li><code>Linestring z</code> return true </li>
    * <li><code>LinestringZ</code> return true </li>
    * <li><code>LinestringZm</code> return true </li>
    * <li><code>Linestring Zm</code> return true </li>
    * <li><code>Linestring m</code> return false </li>
    * <li><code>Linestringm</code> return false </li>
    * <li><code>Linestring</code> return false </li>
    * </ul>
    * 
    * @param geometryType
    * @return
    */
   public static boolean hasZGeometryType(String geometryType) {
      if (geometryType == null) {
         return false;
      }
      geometryType = geometryType.replaceAll("\\s","").toUpperCase();
      return geometryType.endsWith("Z") || geometryType.endsWith("ZM");
   }

   /**
    * Informs if OGC <code>geometryType</code> is defined with <em>Z</em>
    * coordinates.<br/>
    * Examples:<ul>
    * <li><code>Linestring z</code> return false </li>
    * <li><code>LinestringZ</code> return false </li>
    * <li><code>LinestringZM</code> return true </li>
    * <li><code>Linestring Zm</code> return true </li>
    * <li><code>Linestring m</code> return true </li>
    * <li><code>LinestringM</code> return true </li>
    * <li><code>Linestringm</code> return true </li>
    * <li><code>Linestring</code> return false </li>
    * </ul>
    * 
    * @param geometryType
    * @return
    */
   public static boolean hasMGeometryType(String geometryType) {
      if (geometryType == null) {
         return false;
      }
      geometryType = geometryType.replaceAll("\\s","").toUpperCase();
      return geometryType.endsWith("M");
   }
}
