package liquibase.ext.spatial.sqlgenerator;

import liquibase.database.Database;
import liquibase.database.core.H2Database;
import liquibase.exception.ValidationErrors;
import liquibase.ext.spatial.statement.CreateSpatialIndexStatement;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.structure.core.Table;

/**
 * The <code>CreateSpatialIndexGeneratorH2</code>....
 */
public class CreateSpatialIndexGeneratorH2 extends
      AbstractCreateSpatialIndexGenerator {

   /**
    * @see liquibase.sqlgenerator.core.AbstractSqlGenerator#supports(liquibase.statement.SqlStatement,
    *      liquibase.database.Database)
    */
   @Override
   public boolean supports(CreateSpatialIndexStatement statement,
         Database database) {
      return database instanceof H2Database;
   }

   /**
    * @see liquibase.sqlgenerator.SqlGenerator#validate(liquibase.statement.SqlStatement,
    *      liquibase.database.Database,
    *      liquibase.sqlgenerator.SqlGeneratorChain)
    */
   public ValidationErrors validate(CreateSpatialIndexStatement statement,
         Database database, SqlGeneratorChain sqlGeneratorChain) {
      // TODO Auto-generated method stub
      return null;
   }

   /**
    * @see liquibase.sqlgenerator.SqlGenerator#generateSql(liquibase.statement.SqlStatement,
    *      liquibase.database.Database,
    *      liquibase.sqlgenerator.SqlGeneratorChain)
    */
   public Sql[] generateSql(CreateSpatialIndexStatement statement,
         Database database, SqlGeneratorChain sqlGeneratorChain) {
      // TODO: Set the HATBOX table name.
      Table table = new Table();
      table.setName(statement.getTableName());
      UnparsedSql spatialize = new UnparsedSql(
            "HATBOX_SPATIALIZE(schema, table, geomColumn, geomType, srid, 'exposePK', 'maxEntries');",
            table);
      UnparsedSql buildIndex = new UnparsedSql(
            "HATBOX_BUILD_INDEX(schema,table, commitInterval, null)", table);
      return new Sql[] { spatialize, buildIndex };
   }
}
