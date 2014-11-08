Liquibase Spatial
-----------------
Liquibase Spatial is an extension to Liquibase to handle spatial indexing and geometry types.  It 
requires Liquibase 3.1.1 or newer.

<table>
   <tr>
      <th>Liquibase Version</th>
      <th>Liquibase Spatial Version</th>
   </tr>
   <tr>
      <td>Liquibase 3.1.1</td>
      <td>Liquibase Spatial 1.0.x</td>
   </tr>
   <tr>
      <td>Liquibase 3.2.x</td>
      <td>Liquibase Spatial 1.1.x</td>
   </tr>
   <tr>
      <td>Liquibase 3.3.x</td>
      <td>Liquibase Spatial 1.2.x</td>
   </tr>
</table>

In order to use this extension, you must have the [<code>liquibase-spatial</code>](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22com.github.lonnyj%22%20AND%20a%3A%22liquibase-spatial%22)
jar in your classpath. For XML change logs, define the <code>spatial</code> namespace as below:

```XML
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
   xmlns:spatial="http://www.liquibase.org/xml/ns/dbchangelog-ext/liquibase-spatial"
   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
   xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
      http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.1.xsd 
   http://www.liquibase.org/xml/ns/dbchangelog-ext/liquibase-spatial 
      http://lonnyj.github.com/liquibase-spatial/liquibase-spatial.xsd">
</databaseChangeLog>
```

Using the XML elements defined by this extension requires specifying the <code>spatial</code>
namespace prefix on those elements.  Here is a quick example of how to create a table with a
geometry column, create a spatial index on that column, and insert data into the table:

```XML
<changeSet id="Create table" author="bob">
   <preConditions>
      <spatial:spatialSupported />
   </preConditions>
   <createTable tableName="EXAMPLE">
      <column name="ID" type="BIGINT">
         <constraints nullable="false" primaryKey="true" primaryKeyName="EXAMPLE_PK" />
      </column>
      <column name="GEOM" type="GEOMETRY(Point, 4326)">
         <constraints nullable="false" />
      </column>
   </createTable>
</changeSet>

<changeSet id="Create spatial index" author="bob">
   <preConditions>
      <not>
         <spatial:spatialIndexExists tableName="EXAMPLE" columnNames="GEOM" />
      </not>
   </preConditions>
   <spatial:createSpatialIndex tableName="EXAMPLE" indexName="EXAMPLE_GEOM_IDX" geometryType="Point" srid="4326">
      <column name="GEOM" />
   </spatial:createSpatialIndex>
</changeSet>

<changeSet id="Insert spatial data" author="bob">
   <insert tableName="EXAMPLE">
      <column name="ID" valueNumeric="1" />
      <column name="GEOM" value="SRID=4326;POINT(-5 -5)" />
   </insert>
   <insert tableName="EXAMPLE">
      <column name="ID" valueNumeric="2" />
      <column name="GEOM" value="SRID=4326;POINT(0 0)" />
   </insert>
   <insert tableName="EXAMPLE">
      <column name="ID" valueNumeric="3" />
      <column name="GEOM" value="SRID=4326;POINT(5 5)" />
   </insert>
</changeSet>
```

Refer to the [documentation](http://lonnyj.github.com/liquibase-spatial) for further information.