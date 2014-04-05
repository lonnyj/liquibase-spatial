Change: 'loadData'
-------------------
The <code>&lt;loadData></code> change works the same as before except that Well-Known Text and 
Extended Well-Known Text values are supported.

<h3>Example</h3>
```XML
<changeSet id="1" author="bob">
   <loadData tableName="home" file="home.csv">
      <column name="id" type="numeric"/>
   </loadData>
</changeSet>
```

<h5>home.csv</h5>
```
id, location
1, POINT(-106.445305 39.117769)
2, SRID=4326;POINT(-106.47556 39.1875)
3, SRID=4326;POINT(-106.32056 38.92444)
```