package liquibase.ext.spatial.sqlgenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import liquibase.database.Database;
import liquibase.database.core.DerbyDatabase;
import liquibase.database.core.H2Database;
import liquibase.datatype.DataTypeFactory;
import liquibase.datatype.LiquibaseDataType;
import liquibase.exception.ValidationErrors;
import liquibase.ext.spatial.datatype.GeometryType;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.AbstractSqlGenerator;
import liquibase.sqlgenerator.core.DropColumnGenerator;
import liquibase.statement.core.AddColumnStatement;

/**
 * <code>AddSpatialColumnGeneratorGeoDB</code> ... <code>DropSpatialColumnGeneratorGeoDB</code> is a
 * {@link DropColumnGenerator} that specializes in GeoDB. If the column to be dropped is the
 * geometry column, the <code>DropGeometryColumn</code> is invoked instead of performing the typical
 * <code>ALTER TABLE</code> statement. Otherwise, the next SQL generator in the chain is invoked to
 * handle the request.
 */
public class AddSpatialColumnGeneratorGeoDB extends AbstractSqlGenerator<AddColumnStatement> {
   /**
    * @see liquibase.sqlgenerator.core.AbstractSqlGenerator#supports(liquibase.statement.SqlStatement,
    *      liquibase.database.Database)
    */
   @Override
   public boolean supports(final AddColumnStatement statement, final Database database) {
      return database instanceof DerbyDatabase || database instanceof H2Database;
   }

   /**
    * @see liquibase.sqlgenerator.core.AbstractSqlGenerator#getPriority()
    */
   @Override
   public int getPriority() {
      return SqlGenerator.PRIORITY_DATABASE + 1;
   }

   @Override
   public ValidationErrors validate(final AddColumnStatement statement, final Database database,
         final SqlGeneratorChain sqlGeneratorChain) {
      return sqlGeneratorChain.validate(statement, database);
   }

   @Override
   public Sql[] generateSql(final AddColumnStatement statement, final Database database,
         final SqlGeneratorChain sqlGeneratorChain) {

      GeometryType geometryType = null;
      final LiquibaseDataType dataType = DataTypeFactory.getInstance().fromDescription(
            statement.getColumnType());
      if (dataType instanceof GeometryType) {
         geometryType = (GeometryType) dataType;
      }

      final boolean isGeometryColumn = geometryType != null;

      // The AddGeometryColumn procedure handles the column already being present, so let a
      // downstream SQL generator handle the typical column addition logic (e.g. placement in the
      // table) then invoke the procedure.
      final List<Sql> list = new ArrayList<Sql>();
      list.addAll(Arrays.asList(sqlGeneratorChain.generateSql(statement, database)));
      if (isGeometryColumn) {
         String schemaName = statement.getSchemaName();
         if (schemaName == null) {
            schemaName = database.getDefaultSchemaName();
         }
         final String tableName = statement.getTableName();
         final String columnName = statement.getColumnName();

         final int srid = geometryType.getSRID() == null ? 4326 : geometryType.getSRID();
         final String geomType = geometryType.getGeometryType() == null ? "'Geometry'" : "'"
               + database.escapeStringForDatabase(geometryType.getGeometryType()) + "'";
         final String sql = "CALL AddGeometryColumn('" + schemaName + "', '" + tableName + "', '"
               + columnName + "', " + srid + ", " + geomType + ", 2)";
         final Sql addGeometryColumn = new UnparsedSql(sql);
         list.add(addGeometryColumn);
      }
      return list.toArray(new Sql[list.size()]);
   }
}
