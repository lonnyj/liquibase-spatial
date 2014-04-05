Change: 'dropSpatialIndex'
------------------------------------

Drops an existing spatial index.

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
      <td>tableName<sup>1</sup></td>
      <td>The name of the indexed table.</td>
      <td>h2, derby</td>
      <td>all</td>
   </tr>
   <tr>
      <td>indexName<sup>1</sup></td>
      <td>The name of the index to drop.</td>
      <td>mysql, oracle, postgresql</td>
      <td>mysql, oracle, postgresql</td>
   </tr>
</table>
1. For the greatest portability, always provide the attribute.

<h3>Example</h3>
```XML
<changeSet id="1" author="bob">
   <spatial:dropSpatialIndex tableName="home" indexName="home_location_idx"/>
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
      <td><b>Supported</b></td>
      <td>No</td>
   </tr>
   <tr>
      <td>H2</td>
      <td><b>Supported</b></td>
      <td>No</td>
   </tr>
   <tr>
      <td>MySQL</td>
      <td><b>Supported</b></td>
      <td>No</td>
   </tr>
   <tr>
      <td>Oracle</td>
      <td><b>Supported</b></td>
      <td>No</td>
   </tr>
   <tr>
      <td>PostgreSQL</td>
      <td><b>Supported</b></td>
      <td>No</td>
   </tr>
</table>
