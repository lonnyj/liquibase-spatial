package liquibase.ext.spatial;

import static org.testng.Assert.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.changelog.ChangeSet;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.logging.LogFactory;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * @author Lonny
 */
public class H2IT {
   private Liquibase liquibase;
   private JdbcConnection jdbcConnection;

   /**
    * @throws java.lang.Exception
    */
   @BeforeTest
   public void setUp() throws Exception {
      LogFactory.setLoggingLevel("debug");
      final Connection connection = DriverManager.getConnection("jdbc:h2:file:target/test");
      this.jdbcConnection = new JdbcConnection(connection);
      final String changeLogFile = "integration-test.xml";
      final ResourceAccessor resourceAccessor = new ClassLoaderResourceAccessor();
      this.liquibase = new Liquibase(changeLogFile, resourceAccessor, this.jdbcConnection);
   }

   /**
    * @throws java.lang.Exception
    */
   @AfterTest
   public void tearDown() throws Exception {
      this.jdbcConnection.rollback();
      this.jdbcConnection.close();
   }

   @Test
   public void test() throws LiquibaseException {
      this.liquibase.update((Contexts) null);
      final List<ChangeSet> unrunChangeSets = this.liquibase.listUnrunChangeSets((Contexts) null);
      assertTrue(unrunChangeSets.isEmpty(), "All change sets should have run");
   }
}
