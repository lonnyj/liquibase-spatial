package liquibase.ext.spatial.sqlgenerator;

import java.util.Map.Entry;
import java.util.regex.Matcher;

import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
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
   @Override
   public int getPriority() {
      return super.getPriority() + 1;
   }

   @Override
   public ValidationErrors validate(final InsertStatement statement, final Database database,
         final SqlGeneratorChain sqlGeneratorChain) {
      return sqlGeneratorChain.validate(statement, database);
   }

   /**
    * Find any fields that look like WKT or EWKT and replace them with the database-specific value.
    */
   @Override
   public Sql[] generateSql(final InsertStatement statement, final Database database,
         final SqlGeneratorChain sqlGeneratorChain) {
      for (final Entry<String, Object> entry : statement.getColumnValues().entrySet()) {
         entry.setValue(handleColumnValue(entry.getValue(), database));
      }
      return super.generateSql(statement, database, sqlGeneratorChain);
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

   /**
    * Indicates if the SRID parameter is required in the function returned from
    * {@link #getGeomFromWktFunction()}.
    * 
    * @param database
    *           the database instance.
    * 
    * @return <code>true</code> if the SRID parameter is required in order to invoke the function.
    */
   protected boolean isSridRequiredInFunction(final Database database) {
      return false;
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
   protected Object handleColumnValue(final Object oldValue, final Database database) {
      Object newValue = oldValue;
      if (oldValue instanceof Geometry) {
         final Geometry geometry = (Geometry) oldValue;
         final String wkt = geometry.toText();
         String sridString = null;
         if (geometry.getSRID() > 0) {
            sridString = String.valueOf(geometry.getSRID());
         }
         newValue = convertToFunction(wkt, sridString, database);
      } else if (oldValue instanceof String) {
         final String value = oldValue.toString().trim();
         final Matcher matcher = WktConversionUtils.EWKT_PATTERN.matcher(value);
         if (matcher.matches()) {
            final String sridString = matcher.group(2);
            final String wkt = matcher.group(3);
            final String function = convertToFunction(wkt, sridString, database);
            newValue = function;
         }
      }
      return newValue;
   }

   /**
    * @param wkt
    *           the Well-Known Text string.
    * @param srid
    *           the SRID string which may be an empty string.
    * @param database
    *           the database instance.
    * @return the string that converts the WKT to a database-specific geometry.
    */
   protected String convertToFunction(final String wkt, final String srid, final Database database) {
      final String geomFromTextFunction = getGeomFromWktFunction();
      String function = geomFromTextFunction + "('" + wkt + "'";
      if (srid != null && !srid.equals("")) {
         function += ", " + srid;
      } else if (isSridRequiredInFunction(database)) {
         throw new IllegalArgumentException("An SRID was not provided with '" + wkt
               + "' but is required in call to '" + geomFromTextFunction + "'");
      }
      function += ")";
      return function;
   }
}
