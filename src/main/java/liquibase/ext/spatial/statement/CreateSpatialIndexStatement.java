package liquibase.ext.spatial.statement;

import liquibase.statement.AbstractSqlStatement;

/**
 * <code>CreateSpatialIndexStatement</code> represents a <code>CREATE SPATIAL INDEX</code>
 * statement.
 */
public class CreateSpatialIndexStatement extends AbstractSqlStatement {

   private final String tableCatalogName;
   private final String tableSchemaName;
   private final String indexName;
   private final String tableName;
   private final String[] columns;
   private String tablespace;

   /** The WKT geometry type (e.g. Geometry, Point, etc). */
   private String geometryType;

   /** The Spatial Reference ID (e.g. 4326). */
   private Integer srid;

   /**
    * @param indexName
    * @param tableCatalogName
    * @param tableSchemaName
    * @param tableName
    *           the table name.
    * @param columns
    *           the array of column names.
    * @param tablespace
    *           the optional table space name.
    * @param geometryType
    *           the optional geometry type.
    * @param srid
    *           the optional Spatial Reference ID.
    */
   public CreateSpatialIndexStatement(final String indexName, final String tableCatalogName,
         final String tableSchemaName, final String tableName, final String[] columns,
         final String tablespace, final String geometryType, final Integer srid) {
      this.indexName = indexName;
      this.tableCatalogName = tableCatalogName;
      this.tableSchemaName = tableSchemaName;
      this.tableName = tableName;
      this.columns = columns.clone();
      this.tablespace = tablespace;
      this.geometryType = geometryType;
      this.srid = srid;
   }

   public String getTableCatalogName() {
      return this.tableCatalogName;
   }

   public String getTableSchemaName() {
      return this.tableSchemaName;
   }

   public String getIndexName() {
      return this.indexName;
   }

   public String getTableName() {
      return this.tableName;
   }

   public String[] getColumns() {
      return this.columns;
   }

   public String getTablespace() {
      return this.tablespace;
   }

   public CreateSpatialIndexStatement setTablespace(final String tablespace) {
      this.tablespace = tablespace;
      return this;
   }

   /**
    * Sets the WKT geometry type (e.g. Geometry, Point, etc).
    * 
    * @param geometryType
    *           the geometry type.
    */
   public void setGeometryType(final String geometryType) {
      this.geometryType = geometryType;
   }

   /**
    * Returns the WKT geometry type (e.g. Geometry, Point, etc).
    * 
    * @return the geometry type.
    */
   public String getGeometryType() {
      return this.geometryType;
   }

   /**
    * Sets the Spatial Reference ID (e.g. 4326).
    * 
    * @param srid
    *           the SRID.
    */
   public void setSrid(final Integer srid) {
      this.srid = srid;
   }

   /**
    * Returns the Spatial Reference ID (e.g. 4326).
    * 
    * @return the SRID.
    */
   public Integer getSrid() {
      return this.srid;
   }
}
