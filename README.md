Liquibase Spatial
-----------------

Liquibase Spatial extends Liquibase to handle spatial types.  It requires Liquibase 3.1.1
or newer.

In order to use this extension, you must have <code>liquibase-spatial-1.0.0.jar</code>
in your classpath. For XML change logs, define the <code>spatial</code> namespace.

```XML
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
   xmlns:spatial="http://www.liquibase.org/xml/ns/dbchangelog-ext/liquibase-spatial"
   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
   xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
      http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.1.xsd 
   http://www.liquibase.org/xml/ns/dbchangelog-ext/liquibase-spatial 
      http://www.liquibase.org/xml/ns/dbchangelog-ext/liquibase-spatial.xsd">
</databaseChangeLog>
```

Using the preconditions or changes from this extension requires specifying the <code>spatial</code>
namespace prefixing on those elements.

```XML
<changeSet id="1" author="bob">
   <preConditions>
      <not>
         <columnExists tableName="SPATIAL_TABLE" columnName="GEOM" />
      </not>
   </preConditions>
   <addColumn tableName="SPATIAL_TABLE">
      <column name="GEOM" type="Geometry(Point, 4326)"/>
   </addColumn>
</changeSet>
<changeSet id="2" author="bob">
   <preConditions>
      <not>
         <spatial:spatialIndexExists tableName="SPATIAL_TABLE" columnNames="GEOM" />
      </not>
   </preConditions>
   <spatial:createSpatialIndex tableName="SPATIAL_TABLE" indexName="SPATIAL_TABLE_GEOM_IDX" 
      geometryType="Point" srid="4326">
      <column name="GEOM" />
   </spatial:createSpatialIndex>
</changeSet>
```

Refer to the [documentation](http://lonnyj.github.io/liquibase-spatial) for further information.
