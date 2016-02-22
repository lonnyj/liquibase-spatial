Compiling
-------------------
In order to compile Liquibase Spatial, the Boundless Maven Repository must be configured in Maven's <code>settings.xml</code>.  Add this profile in order to resolve the necessary dependencies:

```XML
 <profile>
    <id>Repositories</id>
    <activation>
       <activeByDefault>true</activeByDefault>
    </activation>
    <repositories>
       <repository>
          <id>boundless</id>
          <name>Boundless Maven Repository</name>
          <releases>
             <enabled>true</enabled>
          </releases>
          <snapshots>
             <enabled>false</enabled>
          </snapshots>
          <url>http://repo.boundlessgeo.com/main</url>
       </repository>
    </repositories>
 </profile>
```