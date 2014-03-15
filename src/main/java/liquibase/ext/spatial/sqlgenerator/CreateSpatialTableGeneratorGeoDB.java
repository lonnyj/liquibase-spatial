package liquibase.ext.spatial.sqlgenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import liquibase.database.Database;
import liquibase.database.core.DerbyDatabase;
import liquibase.database.core.H2Database;
import liquibase.datatype.LiquibaseDataType;
import liquibase.exception.ValidationErrors;
import liquibase.ext.spatial.datatype.GeometryType;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.AbstractSqlGenerator;
import liquibase.sqlgenerator.core.CreateTableGenerator;
import liquibase.statement.core.CreateTableStatement;

/**
 * <code>CreateSpatialTableGeneratorGeoDB</code> augments the built-in {@link CreateTableGenerator}
 * by invoking the <code>AddGeometryColumn</code> procedure to add the standard metadata for a
 * geometry column.
 */
public class CreateSpatialTableGeneratorGeoDB extends AbstractSqlGenerator<CreateTableStatement> {
   /**
    * @see liquibase.sqlgenerator.core.AbstractSqlGenerator#supports(liquibase.statement.SqlStatement,
    *      liquibase.database.Database)
    */
   @Override
   public boolean supports(final CreateTableStatement statement, final Database database) {
      return database instanceof DerbyDatabase || database instanceof H2Database;
   }

   /**
    * @see liquibase.sqlgenerator.core.AbstractSqlGenerator#getPriority()
    */
   @Override
   public int getPriority() {
      return super.getPriority() + 1;
   }

   @Override
   public ValidationErrors validate(final CreateTableStatement statement, final Database database,
         final SqlGeneratorChain sqlGeneratorChain) {
      return sqlGeneratorChain.validate(statement, database);
   }

   @Override
   public Sql[] generateSql(final CreateTableStatement statement, final Database database,
         final SqlGeneratorChain sqlGeneratorChain) {
      final List<Sql> list = new ArrayList<Sql>(Arrays.asList(sqlGeneratorChain.generateSql(
            statement, database)));
      for (final Entry<String, LiquibaseDataType> entry : statement.getColumnTypes().entrySet()) {
         if (entry.getValue() instanceof GeometryType) {
            final String schemaName = statement.getSchemaName();
            final String escapedSchemaName = schemaName == null ? "NULL" : "'"
                  + database.escapeStringForDatabase(schemaName) + "'";
            final String tableName = statement.getTableName();
            final String columnName = entry.getKey();

            final GeometryType geometryType = (GeometryType) entry.getValue();
            final Integer srid = geometryType.getSRID() == null ? 4326 : geometryType.getSRID();
            final String geomType = geometryType.getGeometryType() == null ? "'Geometry'" : "'"
                  + database.escapeStringForDatabase(geometryType.getGeometryType()) + "'";

            final String sql = "CALL AddGeometryColumn(" + escapedSchemaName + ", '" + tableName
                  + "', '" + columnName + "', " + srid + ", " + geomType + ", 2)";
            final Sql addGeometryColumn = new UnparsedSql(sql);
            list.add(addGeometryColumn);
         }
      }
      return list.toArray(new Sql[list.size()]);
   }
}
