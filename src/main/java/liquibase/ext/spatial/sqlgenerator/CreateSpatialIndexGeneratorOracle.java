package liquibase.ext.spatial.sqlgenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import liquibase.database.Database;
import liquibase.database.core.OracleDatabase;
import liquibase.ext.spatial.statement.CreateSpatialIndexStatement;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;
import liquibase.structure.core.View;
import liquibase.util.StringUtils;

/**
 * <code>CreateSpatialIndexGeneratorOracle</code> generates the SQL for creating a spatial index in
 * Oracle.
 */
public class CreateSpatialIndexGeneratorOracle extends AbstractCreateSpatialIndexGenerator {
   @Override
   public boolean supports(final CreateSpatialIndexStatement statement, final Database database) {
      return database instanceof OracleDatabase;
   }

   @Override
   public Sql[] generateSql(final CreateSpatialIndexStatement statement, final Database database,
         final SqlGeneratorChain sqlGeneratorChain) {
      final View metadataView = new View().setName("USER_SDO_GEOM_METADATA");
      final String deleteMetadataSql = generateDeleteMetadataSql(statement, database);
      final Sql deleteMetadata = new UnparsedSql(deleteMetadataSql, metadataView);
      final String insertMetadataSql = generateInsertMetadataSql(statement, database);
      final Sql insertMetadata = new UnparsedSql(insertMetadataSql, metadataView);
      final String createIndexSql = generateCreateIndexSql(statement, database);
      final Sql createIndex = new UnparsedSql(createIndexSql, getAffectedIndex(statement));
      return new Sql[] { deleteMetadata, insertMetadata, createIndex };
   }

   /**
    * Generates the SQL for deleting any existing record from the
    * <code>USER_SDO_GEOM_METADATA</code> table. Typically this record shouldn't be present but we
    * must ensure that it does not already exist.
    * 
    * @param statement
    *           the create spatial index statement.
    * @param database
    *           the database instance.
    * @return the SQL to delete any existing metadata record.
    */
   protected String generateDeleteMetadataSql(final CreateSpatialIndexStatement statement,
         final Database database) {
      final StringBuilder sql = new StringBuilder();
      sql.append("DELETE FROM user_sdo_geom_metadata ");
      final String tableName = statement.getTableName().trim();
      sql.append("WHERE table_name = '").append(database.correctObjectName(tableName, Table.class));
      final String columnName = statement.getColumns()[0].trim();
      sql.append("' AND column_name = '").append(
            database.correctObjectName(columnName, Column.class));
      sql.append("'");
      return sql.toString();
   }

   /**
    * Generates the SQL for inserting the necessary record into the
    * <code>USER_SDO_GEOM_METADATA</code> table. This record must be present prior to creating the
    * spatial index.
    * 
    * @param statement
    *           the create spatial index statement.
    * @param database
    *           the database instance.
    * @return the SQL to insert the metadata record.
    */
   protected String generateInsertMetadataSql(final CreateSpatialIndexStatement statement,
         final Database database) {
      final Integer srid = statement.getSrid();
      final String ogcGeometryType = statement.getGeometryType();
      final boolean hasZ = WktConversionUtils.hasZGeometryType(ogcGeometryType);
      final boolean hasM = WktConversionUtils.hasMGeometryType(ogcGeometryType);

      final StringBuilder sql = new StringBuilder();
      sql.append("INSERT INTO user_sdo_geom_metadata ");
      sql.append("(table_name, column_name, diminfo, srid) ");
      final String tableName = statement.getTableName().trim();
      sql.append("VALUES ('").append(database.correctObjectName(tableName, Table.class));
      final String columnName = statement.getColumns()[0].trim();
      sql.append("', '").append(database.correctObjectName(columnName, Column.class));
      sql.append("', ");
      if (srid == null) {
         // Use standard (not projected) dimension array (handling Z and M)
         appendSDO_DIM_ARRAY(false,hasZ, hasM, sql);
              sql.append(", NULL");
      } else {
         // Include CASE expression to generate dimension array depending on
         // Oracle SRID is PROJECTED or not (handling Z and M)
         sql.append("CASE (SELECT COORD_REF_SYS_KIND from SDO_COORD_REF_SYSTEM where srid = ")
         .append(OracleSpatialUtils.getOracleSRIDExpression(srid)).append(")");
         sql.append(" WHEN 'PROJECTED' THEN ");
         appendSDO_DIM_ARRAY(true, hasZ, hasM, sql);
         sql.append(" ELSE ");
         appendSDO_DIM_ARRAY(false, hasZ, hasM, sql);
         sql.append(" END ");
         sql.append(", ").append(OracleSpatialUtils.getOracleSRIDExpression(srid));
      }
      sql.append(")");
      return sql.toString();
   }

   /**
    * Append to a StringBuilde the SDO_DIM_ARRAY expression based on parameters
    * 
    * @param projected 
    *           coordinates projected or not: 
    *           false appends lat/long coordinates definition
    *           true appends x/y coordinates definition
    * @param hasZ include Z value
    * @param hasM include M value
    * @param sql StringBuilder instance to append expression
    */
   private void appendSDO_DIM_ARRAY(final boolean projected, final boolean hasZ, final boolean hasM,
         final StringBuilder sql) {
      sql.append("SDO_DIM_ARRAY(");
      if (projected) {
         // Based on MAX UTM values
         sql.append("SDO_DIM_ELEMENT('X', 0.0, 41000000.0, 0.5), ");
         sql.append("SDO_DIM_ELEMENT('Y', 0.0, 9300000.0, 0.5)"); 
      } else {
         sql.append("SDO_DIM_ELEMENT('Longitude', -180, 180, 0.005), ");
         sql.append("SDO_DIM_ELEMENT('Latitude', -90, 90, 0.005)");
      }
      if (hasZ) {
         // use generic value for Z dimension
         sql.append(",SDO_DIM_ELEMENT('Z', -10000000, 10000000, 0.5)");
      }
      if (hasM) {
         // use generic value for M dimension
         sql.append(",SDO_DIM_ELEMENT('M', -10000000, 10000000, 0.5)");
      }
      sql.append(")");
   }

   /**
    * Generates the SQL for creating the spatial index.
    * 
    * @param statement
    *           the create spatial index statement.
    * @param database
    *           the database instance.
    * @return the SQL to create a spatial index.
    */
   protected String generateCreateIndexSql(final CreateSpatialIndexStatement statement,
         final Database database) {
      final StringBuilder sql = new StringBuilder();
      sql.append("CREATE INDEX ");
      final String schemaName = statement.getTableSchemaName();
      final String catalogName = statement.getTableCatalogName();
      final String indexName = statement.getIndexName();
      sql.append(database.escapeIndexName(catalogName, schemaName, indexName));
      sql.append(" ON ");
      final String tableName = statement.getTableName();
      sql.append(database.escapeTableName(catalogName, schemaName, tableName)).append(" (");
      final Iterator<String> iterator = Arrays.asList(statement.getColumns()).iterator();
      final String column = iterator.next();
      sql.append(database.escapeColumnName(catalogName, statement.getTableSchemaName(), tableName,
            column));
      sql.append(") INDEXTYPE IS mdsys.spatial_index");

      // Generate and add the optional parameters.
      final Collection<String> parameters = getParameters(statement);
      if (parameters != null && !parameters.isEmpty()) {
         sql.append(" PARAMETERS ('");
         sql.append(StringUtils.join(parameters, " "));
         sql.append("')");
      }
      return sql.toString();
   }

   /**
    * Creates the parameters to the spatial index creation statement.
    * 
    * @param statement
    *           the statement.
    * @return the optional parameters for the <code>CREATE INDEX</code> statement.
    */
   protected Collection<String> getParameters(final CreateSpatialIndexStatement statement) {
      final Collection<String> parameters = new ArrayList<String>();
      if (StringUtils.trimToNull(statement.getGeometryType()) != null) {
         final String gType = getGtype(statement.getGeometryType().trim());
         if (gType != null) {
            parameters.add("layer_gtype=" + gType);
         }
      }
      if (StringUtils.trimToNull(statement.getTablespace()) != null) {
         parameters.add("tablespace=" + statement.getTablespace().trim());
      }
      return parameters;
   }

   /**
    * Converts the OGC geometry type to Oracle's <code>SDO_GTYPE</code>.
    * 
    * @param ogcGeometryType the OGC geometry type.
    * @return the corresponding Oracle <code>SDO_GTYPE</code>.
    */
   protected String getGtype(final String ogcGeometryType) {
      final String gType;
      if (StringUtils.trimToNull(ogcGeometryType) == null) {
         gType = null;
      }
      else {
         // Check and Remove tailing M and Z chars
         String ogcGeometryTypeTmp = ogcGeometryType.trim().toUpperCase();
         if (ogcGeometryTypeTmp.endsWith("M")) {
            ogcGeometryTypeTmp = ogcGeometryTypeTmp.substring(0,
                  ogcGeometryTypeTmp.length() - 1);
         }
         if (ogcGeometryTypeTmp.endsWith("Z")) {
            ogcGeometryTypeTmp = ogcGeometryTypeTmp.substring(0,
                  ogcGeometryTypeTmp.length() - 1);
         }
         if ("LineString".equalsIgnoreCase(ogcGeometryTypeTmp)) {
            gType = "LINE";
         }
         else if ("MultiLineString".equalsIgnoreCase(ogcGeometryTypeTmp)) {
            gType = "MULTILINE";
         }
         else if ("Triangle".equalsIgnoreCase(ogcGeometryTypeTmp)) {
            gType = "POLYGON";
         }
         else if ("Point".equalsIgnoreCase(ogcGeometryTypeTmp)
               || "MultiPoint".equalsIgnoreCase(ogcGeometryTypeTmp)
               || "Curve".equalsIgnoreCase(ogcGeometryTypeTmp)
               || "MultiCurve".equalsIgnoreCase(ogcGeometryTypeTmp)
               || "Polygon".equalsIgnoreCase(ogcGeometryTypeTmp)
               || "MultiPolygon".equalsIgnoreCase(ogcGeometryTypeTmp)) {
            gType = ogcGeometryTypeTmp;
         }
         else {
            gType = "COLLECTION";
         }
      }
      return gType;
   }
}
