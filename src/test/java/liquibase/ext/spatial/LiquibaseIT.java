package liquibase.ext.spatial;

import static org.testng.Assert.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.changelog.ChangeSet;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.logging.LogFactory;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Performs a full integration test of all preconditions, changes, statements and SQL generators
 * with Liquibase.
 */
public abstract class LiquibaseIT {
   /**
    * Returns the database name.
    * 
    * @return the database name.
    */
   protected String getDatabaseName() {
      return "test";
   }

   /**
    * Returns the database connection to the current database.
    * 
    * @return the database connection.
    * @throws SQLException
    *            if unable to get the current database connection.
    */
   protected abstract Connection getConnection() throws SQLException;

   /**
    * Initialization for each test.
    */
   @SuppressWarnings("deprecation")
   @BeforeTest
   public void setUp() throws Exception {
      LogFactory.setLoggingLevel("debug");
   }

   /**
    * Runs the integration tests.
    * 
    * @param changeLogFile
    *           the database change log to use in the {@link Liquibase#update(Contexts) update}.
    * @throws LiquibaseException
    *            if Liquibase fails to initialize or run the update.
    * @throws SQLException
    *            if unable to get the database connection.
    */
   @Test(dataProvider = "databaseUrlProvider")
   public void testLiquibase(final String changeLogFile) throws LiquibaseException, SQLException {
      final Connection connection = getConnection();
      final JdbcConnection jdbcConnection = new JdbcConnection(connection);
      try {
         final ResourceAccessor resourceAccessor = new ClassLoaderResourceAccessor();
         final Liquibase liquibase = new Liquibase(changeLogFile, resourceAccessor, jdbcConnection);
         liquibase.update((Contexts) null);
         final List<ChangeSet> unrunChangeSets = liquibase.listUnrunChangeSets((Contexts) null);
         assertTrue(unrunChangeSets.isEmpty(), "All change sets should have run");
      } finally {
         jdbcConnection.rollback();
         jdbcConnection.close();
      }
   }

   /**
    * Provides the test data for {@link #testLiquibase(String)}.
    * 
    * @return the test data.
    */
   @DataProvider
   public Object[][] databaseUrlProvider() {
      return new Object[][] { new Object[] { "create-table-index-drop-index-table.xml" },
            new Object[] { "create-table-index-drop-table.xml" },
            new Object[] { "add-column-create-index-drop-column.xml" } };
   }
}
