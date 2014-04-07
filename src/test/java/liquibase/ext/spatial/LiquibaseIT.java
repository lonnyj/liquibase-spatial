package liquibase.ext.spatial;

import static org.testng.Assert.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.changelog.ChangeSet;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.logging.LogFactory;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
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
   @AfterMethod
   public void cleanUpDatabase() throws SQLException {
      Connection connection = null;
      try {
         connection = getConnection();

         // Drop the TEST table.
         Statement statement = connection.createStatement();
         try {
            statement.execute("DROP TABLE TEST");
         } finally {
            statement.close();
         }

         // Drop the DATABASECHANGELOG table.
         statement = connection.createStatement();
         try {
            statement.execute("DROP TABLE DATABASECHANGELOG");
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
   @Test(dataProvider = "databaseUrlProvider")
   public void testLiquibaseUpdateTestingRollback(final String changeLogFile)
         throws LiquibaseException, SQLException {
      final Connection connection = getConnection();
      final JdbcConnection jdbcConnection = new JdbcConnection(connection);
      try {
         final ResourceAccessor resourceAccessor = new ClassLoaderResourceAccessor();
         final Liquibase liquibase = new Liquibase(changeLogFile, resourceAccessor, jdbcConnection);
         liquibase.updateTestingRollback((Contexts) null);
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
