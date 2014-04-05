Change: 'createSpatialIndex'
------------------------------------

Creates a spatial index on an existing column or set of columns.

<h3>Available Attributes</h3>
<table>
   <tr>
      <th>Attribute</th>
      <th>Description</th>
      <th>Required For</th>
      <th>Supports</th>
   </tr>
   <tr>
      <td>catalogName</td>
      <td>The name of the catalog.</td>
      <td></td>
      <td>all</td>
   </tr>
   <tr>
      <td>schemaName</td>
      <td>The name of the schema.</td>
      <td></td>
      <td>all</td>
   </tr>
   <tr>
      <td>tableName</td>
      <td>The name of the table to which the index will be added.</td>
      <td>all</td>
      <td>all</td>
   </tr>
   <tr>
      <td>indexName<sup>1</sup></td>
      <td>The name of the index to create.</td>
      <td>mysql, oracle, postgresql</td>
      <td>mysql, oracle, postgresql</td>
   </tr>
   <tr>
      <td>tablespace</td>
      <td>The tablespace in which the index will be created.</td>
      <td></td>
      <td>oracle, postgresql</td>
   </tr>
   <tr>
      <td>geometryType<sup>2</sup></td>
      <td>The geometry type of the data to be indexed (e.g. Geometry, Point, MultiLineString, 
      Polygon, GeometryCollection, etc).</td>
      <td></td>
      <td>oracle</td>
   </tr>
   <tr>
      <td>srid<sup>2</sup></td>
      <td>The Spatial Reference ID of the data to be indexed.</td>
      <td>derby, h2</td>
      <td>derby, h2, oracle</td>
   </tr>
</table>
1. For the greatest portability, always provide the attribute.
2. While the attribute is not necessarily required for your database(s), for the greatest 
portability, the attribute should always be provided and match the corresponding 
<code>&lt;createTable></code> or <code>&lt;addColumn></code> <a href="geometry_data_type.html">column 
type</a> parameter.

<h3>Nested Properties</h3>
<table>
   <tr>
      <th>Name</th>
      <th>Description</th>
      <th>Required For</th>
      <th>Supports</th>
      <th>Multiple Allowed</th>
   </tr>
   <tr>
      <td>columns</td>
      <td>Column(s) to add to the index<br/><br/>See the <a href="http://www.liquibase.org/documentation/column.html">column tag</a> document for more information.</td>
      <td>all</td>
      <td>all</td>
      <td>postgresql</td>
   </tr>
</table>

<h3>Example</h3>
```XML
<changeSet id="1" author="bob">
   <spatial:createSpatialIndex tableName="home" indexName="home_location_idx" geometryType="Point" srid="4326">
      <column name="location" />
   </spatial:createSpatialIndex>
</changeSet>
```

<h3>Database Support</h3>

<table>
   <tr>
      <th>Database</th>
      <th>Notes</th>
      <th>Auto Rollback</th>
   </tr>
   <tr>
      <td>Derby</td>
      <td>The table must contain a numeric primary key column. Only one spatial index per table is allowed.</td>
      <td><b>Yes</b></td>
   </tr>
   <tr>
      <td>H2</td>
      <td>The table must contain a numeric primary key column. Only one spatial index per table is allowed.</td>
      <td><b>Yes</b></td>
   </tr>
   <tr>
      <td>MySQL</td>
      <td><b>Supported</b></td>
      <td><b>Yes</b></td>
   </tr>
   <tr>
      <td>Oracle</td>
      <td><b>Supported</b></td>
      <td><b>Yes</b></td>
   </tr>
   <tr>
      <td>PostgreSQL</td>
      <td><b>Supported</b></td>
      <td><b>Yes</b></td>
   </tr>
</table>
