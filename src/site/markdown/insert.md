Change: 'insert'
-------------------
The <code>&lt;insert></code> change works the same as before except that 
[Well-Known Text](http://en.wikipedia.org/wiki/Well-known_text) and 
[Extended Well-Known Text](http://postgis.org/docs/ST_GeomFromEWKT.html) formats are supported.

As a Best Practice, use the Extended Well-Known Text format and match the SRID to the value given 
in the column's <a href="geometry_data_type.html">geometry</a> column type.

<h3>Example</h3>
```XML
<changeSet id="1" author="bob">
   <insert tableName="home">
      <column name="id" valueNumeric="1" />
      <column name="location" value="POINT(-106.445305 39.117769)" />
   </insert>
   <insert tableName="home">
      <column name="id" valueNumeric="1" />
      <column name="location" value="SRID=4326;POINT(-106.445305 39.117769)" />
   </insert>
</changeSet>
```
