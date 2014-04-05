Change: 'insert'
-------------------
The <code>&lt;insert></code> change works the same as before except that Well-Known Text and Extended
Well-Known Text values are supported.

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
