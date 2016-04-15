package org.alfresco.util.schemacomp;


import javax.sql.DataSource;

import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.schemacomp.model.Schema;
import org.alfresco.util.schemacomp.test.exportdb.AbstractExportTester;
import org.alfresco.util.schemacomp.test.exportdb.MySQLDialectExportTester;
import org.alfresco.util.schemacomp.test.exportdb.PostgreSQLDialectExportTester;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.dialect.PostgreSQLDialect;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Tests for the ExportDb class. Loads the database into an in-memory {@link Schema} representation.
 * <p>
 * This test is DBMS specific, if the test is run on a system configured against MySQL for example,
 * it will run MySQL specific tests. If there is no test available for the configured DBMS then
 * the test will pass - this allows addition of new DBMS-specific tests when available.
 * 
 * @see AbstractExportTester
 * @author Matt Ward
 */
@Category(OwnJVMTestsCategory.class)
public class ExportDbTest
{
    private ApplicationContext ctx;
    private ExportDb exporter;
    private Dialect dialect;
    private DataSource dataSource;
    private SimpleJdbcTemplate jdbcTemplate;
    private PlatformTransactionManager tx;
    private AbstractExportTester exportTester;
    private static final Log logger = LogFactory.getLog(ExportDbTest.class);
    
    
    @Before
    public void setUp() throws Exception
    {
        ctx = ApplicationContextHelper.getApplicationContext();
        dataSource = (DataSource) ctx.getBean("dataSource");
        tx = (PlatformTransactionManager) ctx.getBean("transactionManager"); 
        jdbcTemplate = new SimpleJdbcTemplate(dataSource);
        exporter = new ExportDb(ctx);
        exporter.setNamePrefix("export_test_");
        dialect = (Dialect) ctx.getBean("dialect");
    }

    
    @Test
    public void exportDb() throws Exception
    {
        Class dialectClass = dialect.getClass();
        
        if (logger.isDebugEnabled())
        {
            logger.debug("Using dialect class " + dialectClass.getName());
        }
        
        if (PostgreSQLDialect.class.isAssignableFrom(dialectClass))
        {
            exportTester = new PostgreSQLDialectExportTester(exporter, tx, jdbcTemplate);
        }
        else if (MySQLDialect.class.isAssignableFrom(dialectClass))
        {
            exportTester = new MySQLDialectExportTester(exporter, tx, jdbcTemplate);
        }
        
        if (exportTester != null)
        {
            // Run the DBMS specific tests.
            exportTester.runExportTest();
        }
        else
        {
            if (logger.isInfoEnabled())
            {
                logger.info("Unsupported dialect for this test " + dialectClass.getName());
            }
        }
    }
}
