Change: 'createTable'
-------------------
The <code>&lt;createTable></code> change works the same as before except that now columns may have
a <a href="geometry_data_type.html">geometry</a> type.

<h3>Example</h3>
```XML
<changeSet id="1" author="bob">
   <createTable tableName="home">
      <column name="id" type="bigint">
         <constraints nullable="false" primaryKey="true" primaryKeyName="home_pk" />
      </column>
      <column name="address" type="varchar(255)" />
      <column name="location" type="geometry(Point, 4326)"/>
   </createTable>
</changeSet>
```
