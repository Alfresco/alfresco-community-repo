package org.alfresco.repo.domain.schema.script;

import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.sql.DataSource;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.util.ApplicationContextHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.MySQLInnoDBDialect;
import org.hibernate.dialect.PostgreSQLDialect;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Integration tests for the {@link ScriptExecutorImpl} class.
 * 
 * @author Matt Ward
 */
public class ScriptExecutorImplIntegrationTest
{
    private final static Log log = LogFactory.getLog(ScriptExecutorImplIntegrationTest.class);
    private static ApplicationContext ctx;
    private ScriptExecutor scriptExecutor;
    private DataSource dataSource;
    private JdbcTemplate jdbcTmpl;
    private Dialect dialect;
    
    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        String[] config = new String[] {
                    "classpath:alfresco/application-context.xml",
                    "classpath:scriptexec/script-exec-test.xml"
        };
        ctx = ApplicationContextHelper.getApplicationContext(config);
    }

    @Before
    public void setUp() throws Exception
    {
        scriptExecutor = ctx.getBean("simpleScriptExecutor", ScriptExecutorImpl.class);
        dataSource = ctx.getBean("dataSource", DataSource.class);
        dialect = ctx.getBean("dialect", Dialect.class); 
        jdbcTmpl = new JdbcTemplate(dataSource);
    }

    /**
     * Check that we can execute a simple script, without any dialect-specific loading.
     * 
     * @throws Exception
     */
    @Test
    public void canExecuteBasicScript() throws Exception
    {
        scriptExecutor.executeScriptUrl("scriptexec/basic.sql");

        String select = "select textfield from alf_test_script_exec order by textfield asc";
        List<String> res = jdbcTmpl.queryForList(select, String.class);
        assertEquals(2, res.size());
        assertEquals("hello", res.get(0));
        assertEquals("world", res.get(1));
    }
    
    /**
     * Check that a script designed to be run for all varieties of DBMS
     * (i.e. in subdirectory org.hibernate.dialect.Dialect) will run
     * regardless of specific dialect (e.g. MySQL or PostgreSQL)
     * 
     * @throws Exception 
     */
    @Test
    public void canExecuteGenericDialectScript() throws Exception
    {
        scriptExecutor.executeScriptUrl("scriptexec/${db.script.dialect}/generic.sql");
        
        String select = "select message from alf_test_script_exec_generic";
        List<String> res = jdbcTmpl.queryForList(select, String.class);
        assertEquals(1, res.size());
        assertEquals("generic", res.get(0));
    }
    
    /**
     * Test the case of executing a specific (e.g. PostgreSQL) database script
     * when no general script is present (therefore no overriding mechanism is required).
     * 
     * @throws Exception 
     */
    @Test
    public void canExecuteSpecificDialectScript() throws Exception
    {
        scriptExecutor.executeScriptUrl("scriptexec/${db.script.dialect}/specific.sql");
        
        String select = "select message from alf_test_script_exec_specific";
        List<String> res = jdbcTmpl.queryForList(select, String.class);
        assertEquals(1, res.size());
        if (dialect.getClass().equals(MySQLInnoDBDialect.class))
        {
            assertEquals("mysql", res.get(0));
        }
        else if (dialect.getClass().equals(PostgreSQLDialect.class))
        {
            assertEquals("postgresql", res.get(0));            
        }
        else
        {
            log.warn("No suitable dialect-specific DB script for test canExecuteSpecificDialectScript()");
        }
    }
    
    /**
     * Test the case of executing a specific database script (e.g. PostgreSQL) when
     * a more generic script also exists -- the more generic script is not run.
     * 
     * @throws Exception
     */
    @Test
    public void canExecuteSpecificDialectOverridingGenericScript() throws Exception
    {
        scriptExecutor.executeScriptUrl("scriptexec/${db.script.dialect}/override.sql");
        
        String select = "select message from alf_test_script_exec_override";
        List<String> res = jdbcTmpl.queryForList(select, String.class);
        assertEquals(1, res.size());
        if (dialect.getClass().equals(MySQLInnoDBDialect.class))
        {
            assertEquals("mysql", res.get(0));
        }
        else if (dialect.getClass().equals(PostgreSQLDialect.class))
        {
            assertEquals("postgresql", res.get(0));            
        }
        else
        {
            log.warn("No suitable dialect-specific DB script for test canExecuteSpecificDialectOverridingGenericScript()");
        }
    }
    
    @Test()
    public void exceptionThrownWhenNoMatchingScriptFound() throws Exception
    {
        try
        {
            scriptExecutor.executeScriptUrl("scriptexec/${db.script.dialect}/non-existent-file.sql");
        }
        catch (AlfrescoRuntimeException e)
        {
            assertEquals("schema.update.err.script_not_found", e.getMsgId());
        }
    }
}
