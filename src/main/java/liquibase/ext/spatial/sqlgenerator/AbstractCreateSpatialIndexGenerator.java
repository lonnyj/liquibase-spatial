package liquibase.ext.spatial.sqlgenerator;

import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.ext.spatial.statement.CreateSpatialIndexStatement;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.AbstractSqlGenerator;
import liquibase.structure.core.Index;
import liquibase.structure.core.Table;

/**
 * <code>AbstractCreateSpatialIndexGenerator</code> provides a minimal implementation of a
 * <code>CreateSpatialIndexGenerator</code>.
 */
public abstract class AbstractCreateSpatialIndexGenerator extends
      AbstractSqlGenerator<CreateSpatialIndexStatement> {

   /**
    * Ensures that the table name and columns are populated.
    * 
    * @see SqlGenerator#validate(liquibase.statement.SqlStatement, Database, SqlGeneratorChain)
    */
   @Override
   public ValidationErrors validate(final CreateSpatialIndexStatement statement,
         final Database database, final SqlGeneratorChain sqlGeneratorChain) {
      final ValidationErrors validationErrors = new ValidationErrors();
      validationErrors.checkRequiredField("tableName", statement.getTableName());
      validationErrors.checkRequiredField("columns", statement.getColumns());
      return validationErrors;
   }

   protected Index getAffectedIndex(final CreateSpatialIndexStatement statement) {
      return new Index().setName(statement.getIndexName()).setTable(
            (Table) new Table().setName(statement.getTableName()).setSchema(
                  statement.getTableCatalogName(), statement.getTableSchemaName()));
   }
}
