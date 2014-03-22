package liquibase.ext.spatial.sqlgenerator;

import java.util.Arrays;
import java.util.Iterator;

import liquibase.database.Database;
import liquibase.ext.spatial.statement.CreateSpatialIndexStatement;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;

/**
 * <code>CreateSpatialIndexGeneratorMySQL</code> generates the SQL for creating a spatial index in
 * MySQL.
 */
public class CreateSpatialIndexGeneratorMySQL extends AbstractCreateSpatialIndexGenerator {
   @Override
   public Sql[] generateSql(final CreateSpatialIndexStatement statement, final Database database,
         final SqlGeneratorChain sqlGeneratorChain) {
      final StringBuilder sql = new StringBuilder();
      sql.append("CREATE SPATIAL INDEX ");
      final String indexSchema = statement.getTableSchemaName();
      sql.append(database.escapeIndexName(statement.getTableCatalogName(), indexSchema,
            statement.getIndexName()));
      sql.append(" ON ");
      sql.append(
            database.escapeTableName(statement.getTableCatalogName(),
                  statement.getTableSchemaName(), statement.getTableName())).append("(");
      final Iterator<String> iterator = Arrays.asList(statement.getColumns()).iterator();
      final String column = iterator.next();
      sql.append(database.escapeColumnName(statement.getTableCatalogName(),
            statement.getTableSchemaName(), statement.getTableName(), column));
      sql.append(")");
      final Sql createIndex = new UnparsedSql(sql.toString(), getAffectedIndex(statement));
      return new Sql[] { createIndex };
   }
}
