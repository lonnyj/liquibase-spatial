package liquibase.ext.spatial.sqlgenerator;

import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import liquibase.database.Database;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.InsertGenerator;
import liquibase.statement.core.InsertStatement;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Implementations of <code>AbstractSpatialInsertGenerator</code> convert a Well-Known Text string
 * and EPSG SRID string to the database-specific geometry.
 * 
 * @author Lonny
 */
public abstract class AbstractSpatialInsertGenerator extends InsertGenerator {
   /** The SRID regular expression. */
   private static final String SRID_REGEX = "SRID[\\s]*=[\\s]*([0-9]+)[\\s]*;";

   /** The WKT regular expression. */
   private static final String WKT_REGEX = "((((MULTI)?(POINT|LINESTRING|POLYGON)|GEOMETRYCOLLECTION)Z?M?)[(].*[)])";

   /** The PostGIS EWKT regular expression. */
   private static final String EWKT_REGEX = "(" + SRID_REGEX + "[\\s]*)?" + WKT_REGEX;

   /** The EWKT <code>Pattern</code> instance. */
   private static final Pattern EWKT_PATTERN = Pattern
         .compile(EWKT_REGEX, Pattern.CASE_INSENSITIVE);

   /**
    * Set the priority to {@link InsertGenerator#getPriority()} <code>+ 1</code>.
    */
   @Override
   public int getPriority() {
      return super.getPriority() + 1;
   }

   /**
    * Find any fields that look like WKT or EWKT and replace them with the database-specific value.
    */
   @Override
   public Sql[] generateSql(final InsertStatement statement, final Database database,
         final SqlGeneratorChain sqlGeneratorChain) {
      for (final Entry<String, Object> entry : statement.getColumnValues().entrySet()) {
         if (entry.getValue() instanceof Geometry) {
            final String geomFromTextFunction = getGeomFromWktFunction();
            String function = geomFromTextFunction + "(";

            final Geometry geometry = (Geometry) entry.getValue();
            final String wkt = geometry.toText();

            // If the WKT now looks like a function, don't wrap it in single quotes.
            if (looksLikeFunctionCall(wkt, database)) {
               function += wkt;
            } else {
               function += "'" + wkt + "'";
            }
            if (geometry.getSRID() > 0) {
               final int srid = geometry.getSRID();
               function += ", " + srid;
            }
            function += ")";
            entry.setValue(function);
         } else if (entry.getValue() instanceof String) {
            final String value = entry.getValue().toString().trim();
            final Matcher matcher = EWKT_PATTERN.matcher(value);
            if (matcher.matches()) {
               final String sridString = matcher.group(2);
               final String wkt = matcher.group(3);
               final String function = handleWkt(wkt, sridString);
               entry.setValue(function);
            }
         }
      }
      return super.generateSql(statement, database, sqlGeneratorChain);
   }

   /**
    * @param wkt
    *           the Well-Known Text string.
    * @param srid
    *           the SRID string which may be an empty string.
    * @return the string that converts the WKT to a database-specific geometry.
    */
   protected String handleWkt(final String wkt, final String srid) {
      final String geomFromTextFunction = getGeomFromWktFunction();
      String function = geomFromTextFunction + "('" + wkt + "'";
      if (srid != null && !srid.equals("")) {
         function += ", " + srid;
      }
      function += ")";
      return function;
   }

   /**
    * @see liquibase.sqlgenerator.core.AbstractSqlGenerator#looksLikeFunctionCall(java.lang.String,
    *      liquibase.database.Database)
    */
   @Override
   public boolean looksLikeFunctionCall(final String value, final Database database) {
      final boolean result;
      if (value.trim().toUpperCase().startsWith(getGeomFromWktFunction().trim().toUpperCase())) {
         result = true;
      } else {
         result = super.looksLikeFunctionCall(value, database);
      }
      return result;
   }

   /**
    * Returns the name of the function that converts Well-Known Text to a database-specific
    * geometry.
    * 
    * @return the name of the function that converts WKT to a geometry.
    */
   protected abstract String getGeomFromWktFunction();
}
