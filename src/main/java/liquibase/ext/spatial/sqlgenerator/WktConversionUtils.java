package liquibase.ext.spatial.sqlgenerator;

import java.util.regex.Pattern;

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
}
