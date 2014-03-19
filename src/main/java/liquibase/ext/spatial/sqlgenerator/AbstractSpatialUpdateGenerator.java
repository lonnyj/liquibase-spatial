package liquibase.ext.spatial.sqlgenerator;

import java.util.Map.Entry;

import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.UpdateGenerator;
import liquibase.statement.core.UpdateStatement;

/**
 * Implementations of <code>AbstractSpatialInsertGenerator</code> convert a Well-Known Text string
 * and EPSG SRID string to the database-specific geometry.
 * 
 * @author Lonny
 */
public abstract class AbstractSpatialUpdateGenerator extends UpdateGenerator implements
      WktInsertOrUpdateGenerator {
   @Override
   public int getPriority() {
      return super.getPriority() + 1;
   }

   @Override
   public ValidationErrors validate(final UpdateStatement statement, final Database database,
         final SqlGeneratorChain sqlGeneratorChain) {
      return sqlGeneratorChain.validate(statement, database);
   }

   /**
    * Find any fields that look like WKT or EWKT and replace them with the database-specific value.
    */
   @Override
   public Sql[] generateSql(final UpdateStatement statement, final Database database,
         final SqlGeneratorChain sqlGeneratorChain) {
      for (final Entry<String, Object> entry : statement.getNewColumnValues().entrySet()) {
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
    * Indicates if the SRID parameter is required in the function returned from
    * {@link #getGeomFromWktFunction()}.
    * 
    * @param database
    *           the database instance.
    * 
    * @return <code>true</code> if the SRID parameter is required in order to invoke the function.
    */
   @Override
   public boolean isSridRequiredInFunction(final Database database) {
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
      final Object newValue = WktConversionUtils.handleColumnValue(oldValue, database, this);
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
   @Override
   public String convertToFunction(final String wkt, final String srid, final Database database) {
      final String function = WktConversionUtils.convertToFunction(wkt, srid, database, this);
      return function;
   }
}
