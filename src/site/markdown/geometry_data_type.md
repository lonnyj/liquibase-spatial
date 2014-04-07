Data Type: 'Geometry'
------------------------------------

To create a spatially-enabled table, use the built-in <code>&lt;createTable></code> or 
<code>&lt;addColumn></code> tag.  On the nested <code>&lt;column></code> tag specify a type of
"Geometry".

<h3>Available Parameters</h3>
The geometry type may have the following parameters that mimic the format used by the PostGIS 2 
<a href="http://boundlessgeo.com/2012/03/postgis-2-0-new-features-typmod/">typmod</a>.
<table>
   <tr>
      <th>Parameter</th>
      <th>Description</th>
      <th>Required For</th>
      <th>Supports</th>
   </tr>
   <tr>
      <td>Geometry Type</td>
      <td>The geometry type of the data in the column (e.g. Geometry, Point, MultiLineString, 
      Polygon, GeometryCollection, etc).</td>
      <td></td>
      <td>derby, h2, postgresql</td>
   </tr>
   <tr>
      <td>SRID</td>
      <td>The Spatial Reference ID of the data in the column.</td>
      <td>derby, h2</td>
      <td>derby, h2, postgresql</td>
   </tr>
</table>
As a Best Practice, the parameters should always be provided and match those in
<a href="create_spatial_index.html"><code>&lt;createSpatialIndex></code></a>.

<h3>Example</h3>
```XML
<changeSet id="1" author="bob">
   <addColumn tableName="home">
      <column name="location" type="geometry(Point, 4326)"/>
   </addColumn>
</changeSet>
```

<h3>Database Support</h3>

<table>
   <tr>
      <th>Database</th>
      <th>Notes</th>
   </tr>
   <tr>
      <td>Derby</td>
      <td>Translates to the <code>VARCHAR(32672) FOR BIT DATA</code> type.</td>
   </tr>
   <tr>
      <td>H2</td>
      <td>Translates to the <code>BINARY</code> type.</td>
   </tr>
   <tr>
      <td>MySQL</td>
      <td>The parameters are ignored</td>
   </tr>
   <tr>
      <td>Oracle</td>
      <td>Translates to the <code>SDO_GEOMETRY</code> type.  The parameters are ignored</td>
   </tr>
   <tr>
      <td>PostgreSQL</td>
      <td>The parameters are optional</td>
   </tr>
</table>
