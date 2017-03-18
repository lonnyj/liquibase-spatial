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
import liquibase.statement.core.AddColumnStatement;
import liquibase.util.StringUtils;

/**
 * <code>AddGeometryColumnGeneratorGeoDB</code> is a SQL generator that
 * specializes in GeoDB. Regardless of the column type, the next SQL generator
 * in the chain is invoked to handle the normal column addition. If the column
 * to be added has a geometry type, the <code>AddGeometryColumn</code> stored
 * procedure is invoked to ensure that the necessary metadata is created in the
 * database.
 */
public class AddGeometryColumnGeneratorGeoDB extends
      AbstractSqlGenerator<AddColumnStatement> {
   /**
    * @see liquibase.sqlgenerator.core.AbstractSqlGenerator#supports(liquibase.statement.SqlStatement,
    *      liquibase.database.Database)
    */
   @Override
   public boolean supports(final AddColumnStatement statement,
         final Database database) {
      return database instanceof DerbyDatabase
            || database instanceof H2Database;
   }

   /**
    * @see liquibase.sqlgenerator.core.AbstractSqlGenerator#getPriority()
    */
   @Override
   public int getPriority() {
      return SqlGenerator.PRIORITY_DATABASE + 1;
   }

   @Override
   public ValidationErrors validate(final AddColumnStatement statement,
         final Database database, final SqlGeneratorChain sqlGeneratorChain) {
      final ValidationErrors errors = new ValidationErrors();
      if (statement != null && statement.getColumnType() != null) {
         final LiquibaseDataType dataType = DataTypeFactory.getInstance()
                 .fromDescription(statement.getColumnType(), database);

         // Ensure that the SRID parameter is provided.
         if (dataType instanceof GeometryType) {
            final GeometryType geometryType = (GeometryType) dataType;
            if (geometryType.getSRID() == null) {
               errors.addError("The SRID parameter is required on the geometry type");
            }
         }
      }
      final ValidationErrors chainErrors = sqlGeneratorChain.validate(
            statement, database);
      if (chainErrors != null) {
         errors.addAll(chainErrors);
      }
      return errors;
   }

   @Override
   public Sql[] generateSql(final AddColumnStatement statement,
         final Database database, final SqlGeneratorChain sqlGeneratorChain) {

      GeometryType geometryType = null;
      if (statement != null && statement.getColumnType() != null) {
         final LiquibaseDataType dataType = DataTypeFactory.getInstance()
                 .fromDescription(statement.getColumnType(), database);
         if (dataType instanceof GeometryType) {
            geometryType = (GeometryType) dataType;
         }
      }

      final boolean isGeometryColumn = geometryType != null;

      // The AddGeometryColumn procedure handles the column already being
      // present, so let a
      // downstream SQL generator handle the typical column addition logic (e.g.
      // placement in the
      // table) then invoke the procedure.
      final List<Sql> list = new ArrayList<Sql>();
      list.addAll(Arrays.asList(sqlGeneratorChain.generateSql(statement,
            database)));
      if (isGeometryColumn) {
         String schemaName = statement.getSchemaName();
         if (schemaName == null) {
            schemaName = database.getDefaultSchemaName();
         }
         final String tableName = statement.getTableName();
         final String columnName = statement.getColumnName();

         final int srid = geometryType.getSRID();
         final String geomType = StringUtils.trimToNull(geometryType
               .getGeometryType()) == null ? "'Geometry'" : "'"
               + database.escapeStringForDatabase(geometryType
                     .getGeometryType()) + "'";
         final String sql = "CALL AddGeometryColumn('" + schemaName + "', '"
               + tableName + "', '" + columnName + "', " + srid + ", "
               + geomType + ", 2)";
         final Sql addGeometryColumn = new UnparsedSql(sql);
         list.add(addGeometryColumn);
      }
      return list.toArray(new Sql[list.size()]);
   }
}
