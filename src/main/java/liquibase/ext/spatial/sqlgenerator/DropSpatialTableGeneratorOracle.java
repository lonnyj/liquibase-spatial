package liquibase.ext.spatial.sqlgenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import liquibase.database.Database;
import liquibase.database.core.OracleDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.AbstractSqlGenerator;
import liquibase.statement.core.DropTableStatement;
import liquibase.structure.core.Table;
import liquibase.structure.core.View;

/**
 * <code>DropSpatialTableGeneratorOracle</code> generates the SQL statement for cleaning up any
 * metadata prior to dropping the table.
 */
public class DropSpatialTableGeneratorOracle extends AbstractSqlGenerator<DropTableStatement> {
   @Override
   public boolean supports(final DropTableStatement statement, final Database database) {
      return database instanceof OracleDatabase;
   }

   @Override
   public int getPriority() {
      return super.getPriority() + 1;
   }

   @Override
   public ValidationErrors validate(final DropTableStatement statement, final Database database,
         final SqlGeneratorChain sqlGeneratorChain) {
      return sqlGeneratorChain.validate(statement, database);
   }

   @Override
   public Sql[] generateSql(final DropTableStatement statement, final Database database,
         final SqlGeneratorChain sqlGeneratorChain) {
      final StringBuilder sql = new StringBuilder();
      sql.append("DELETE FROM user_sdo_geom_metadata ");
      sql.append("WHERE table_name = '").append(
            database.correctObjectName(statement.getTableName(), Table.class));
      sql.append("'");
      final UnparsedSql deleteMetadata = new UnparsedSql(sql.toString(),
            new View().setName("user_sdo_geom_metadata"));

      // First delete the record then perform the standard behavior.
      final List<Sql> list = new ArrayList<Sql>();
      list.add(deleteMetadata);
      list.addAll(Arrays.asList(sqlGeneratorChain.generateSql(statement, database)));
      return list.toArray(new Sql[list.size()]);
   }
}
