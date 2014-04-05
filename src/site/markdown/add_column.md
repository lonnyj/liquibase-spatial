Change: 'addColumn'
-------------------
The <code>&lt;addColumn></code> change works the same as before except that now the column may have
a <a href="geometry_data_type.html">geometry</a> type.

<h3>Example</h3>
```XML
<changeSet id="1" author="bob">
   <addColumn tableName="home">
      <column name="location" type="geometry(Point, 4326)"/>
   </addColumn>
</changeSet>
```
