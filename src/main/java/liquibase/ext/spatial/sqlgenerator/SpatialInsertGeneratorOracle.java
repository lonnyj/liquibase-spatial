package liquibase.ext.spatial.sqlgenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import liquibase.database.Database;
import liquibase.database.core.OracleDatabase;
import liquibase.ext.spatial.sqlgenerator.WktConversionUtils.EWKTInfo;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.InsertStatement;

/**
 * The <code>SpatialInsertGeneratorOracle</code> generates the SQL for <code>INSERT</code>ing
 * geometries into Oracle.
 */
public class SpatialInsertGeneratorOracle extends AbstractSpatialInsertGenerator {
   
   private static final Sql[] EMPTY_SQL_ARRAY = new Sql[0];
   /**
    * Verifies that the <code>InsertStatement</code> has WKT or EWKT.
    */
   @Override
   public boolean supports(final InsertStatement statement, final Database database) {
      return database instanceof OracleDatabase;
   }

   /**
    * Returns the name of the function that converts Well-Known Text to a database-specific
    * geometry.
    * 
    * @return the name of the function that converts WKT to a geometry.
    */
   @Override
   public String getGeomFromWktFunction() {
      return "SDO_GEOMETRY";
   }

   /**
    * Handles the Well-Known Text and SRID for Oracle.
    */
   @Override
   public String convertToFunction(final String wkt, final String srid, final Database database) {
      // Check if WKT is OGC v1.1 compliant (supported version in Oracle)
      EWKTInfo info = WktConversionUtils.getWktInfo(wkt);
      if (info.isV1_1()) {
         // Use SDO_GEOMETRY(wkt) constructor
         final String oracleWkt = OracleSpatialUtils.getOracleWkt(wkt);
         final String oracleSrid = OracleSpatialUtils.getOracleSrid(srid, database);
         return super.convertToFunction(oracleWkt, oracleSrid, database);
      } else {
         // Use SDO_GEOMETRY native constructor
         return OracleSpatialUtils.getNativeOracleConstructor(info, srid, database);
      }
   }
   
   /** 
    * {@inheritDoc}
    * <br/>
    * Overwrite to prevent problems about numeric parse with locale definition
    *
    * @see liquibase.ext.spatial.sqlgenerator.AbstractSpatialInsertGenerator#generateSql(liquibase.statement.core.InsertStatement, liquibase.database.Database, liquibase.sqlgenerator.SqlGeneratorChain)
    */
   @Override
   public Sql[] generateSql(InsertStatement statement, Database database,
         SqlGeneratorChain sqlGeneratorChain) {
       // Prevent problems about numeric format (set numeric characters
       // to US default locale)
       List<Sql> sqls = new ArrayList<Sql>();
       sqls.add(
             new UnparsedSql(
                   "alter session set NLS_NUMERIC_CHARACTERS = '.,'", 
                   getAffectedTable(statement)));
       
       sqls.addAll(Arrays.asList(
             super.generateSql(statement, database, sqlGeneratorChain)));
       return sqls.toArray(EMPTY_SQL_ARRAY);
   }
}
