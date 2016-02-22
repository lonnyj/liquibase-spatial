package liquibase.ext.spatial;

import static org.testng.Assert.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.changelog.ChangeSet;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.logging.LogFactory;
import liquibase.logging.LogLevel;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;

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
    * Returns the database connection URL.
    *
    * @return the connection URL.
    */
   protected abstract String getUrl();

   /**
    * Returns the login user name.
    *
    * @return the user name.
    */
   protected String getUserName() {
      return null;
   }

   /**
    * Returns the login password.
    *
    * @return the password.
    */
   protected String getPassword() {
      return null;
   }

   /**
    * Returns the database connection to the current database.
    *
    * @return the database connection.
    * @throws SQLException
    *            if unable to get the current database connection.
    */
   protected final Connection getConnection() throws SQLException {
      final String url = getUrl();
      final String username = getUserName();
      final String password = getPassword();
      if (username != null) {
         return DriverManager.getConnection(url, username, password);
      }
      return DriverManager.getConnection(url);
   }

   @BeforeMethod
   public void cleanUpDatabase() throws SQLException {
      Connection connection = null;
      try {
         connection = getConnection();

         // Drop the DATABASECHANGELOG table.
         Statement statement = connection.createStatement();
         try {
            statement.execute("DROP TABLE DATABASECHANGELOG");
         } catch (final Exception ignore) {
         } finally {
            statement.close();
         }

         // Drop the DATABASECHANGELOGLOCK table.
         statement = connection.createStatement();
         try {
            statement.execute("DROP TABLE DATABASECHANGELOGLOCK");
         } catch (final Exception ignore) {
         } finally {
            statement.close();
         }
      } catch (final Exception ignore) {
      } finally {
         if (connection != null) {
            connection.close();
         }
      }
   }

   /**
    * Initialization for each test.
    */
   @SuppressWarnings("deprecation")
   @BeforeTest
   public void setUp() throws Exception {
      LogFactory.setLoggingLevel("debug");
   }

   /**
    * Tests Liquibase updating and rolling back the database.
    *
    * @param changeLogFile
    *           the database change log to use in the {@link Liquibase#update(Contexts) update}.
    * @throws LiquibaseException
    *            if Liquibase fails to initialize or run the update.
    * @throws SQLException
    *            if unable to get the database connection.
    */
   @Test(dataProvider = "databaseUrlProvider", enabled = false)
   public void testLiquibaseUpdateTestingRollback(final String changeLogFile)
         throws LiquibaseException, SQLException {
      final Connection connection = getConnection();
      final JdbcConnection jdbcConnection = new JdbcConnection(connection);
      try {
         final ResourceAccessor resourceAccessor = new ClassLoaderResourceAccessor();
         final Liquibase liquibase = createLiquibase(changeLogFile, resourceAccessor,
               jdbcConnection);
         final Contexts contexts = null;
         final LabelExpression labels = new LabelExpression();
         liquibase.updateTestingRollback(contexts, labels);
         final List<ChangeSet> unrunChangeSets = liquibase.listUnrunChangeSets(contexts, labels);
         assertTrue(unrunChangeSets.isEmpty(), "All change sets should have run");
      } finally {
         jdbcConnection.rollback();
         jdbcConnection.close();
      }
   }

   /**
    * Tests Liquibase updating the database.
    *
    * @param changeLogFile
    *           the database change log to use in the {@link Liquibase#update(Contexts) update}.
    * @throws LiquibaseException
    *            if Liquibase fails to initialize or run the update.
    * @throws SQLException
    *            if unable to get the database connection.
    */
   @Test(dataProvider = "databaseUrlProvider")
   public void testLiquibaseUpdate(final String changeLogFile) throws LiquibaseException,
   SQLException {
      final Connection connection = getConnection();
      final JdbcConnection jdbcConnection = new JdbcConnection(connection);
      try {
         final ResourceAccessor resourceAccessor = new ClassLoaderResourceAccessor();
         final Liquibase liquibase = createLiquibase(changeLogFile, resourceAccessor,
               jdbcConnection);
         final Contexts contexts = null;
         liquibase.update(contexts);
         final List<ChangeSet> unrunChangeSets = liquibase.listUnrunChangeSets(contexts);
         assertTrue(unrunChangeSets.isEmpty(), "All change sets should have run");
      } finally {
         jdbcConnection.rollback();
         jdbcConnection.close();
      }
   }

   /**
    * Creates the <code>Liquibase</code> instance.
    *
    * @param changeLogFile
    *           the database change log file name.
    * @param resourceAccessor
    *           the resource accessor.
    * @param databaseConnection
    *           the database connection.
    * @return returns the new instance.
    * @throws LiquibaseException
    *            if unable to create the instance.
    */
   protected Liquibase createLiquibase(final String changeLogFile,
         final ResourceAccessor resourceAccessor, final DatabaseConnection databaseConnection)
               throws LiquibaseException {
      final Liquibase liquibase = new Liquibase(changeLogFile, resourceAccessor, databaseConnection);
      liquibase.getLog().setLogLevel(LogLevel.DEBUG);
      return liquibase;
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
