package org.alfresco.repo.domain.schema.script;

import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.sql.DataSource;

import org.alfresco.util.ApplicationContextHelper;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Integration tests for the {@link ScriptBundleExecutorImpl} class.
 * 
 * @author Matt Ward
 */
public class ScriptBundleExecutorImplIntegrationTest
{
    private static ApplicationContext ctx;
    private ScriptBundleExecutor bundleExecutor;
    private DataSource dataSource;
    private JdbcTemplate jdbcTmpl;
    
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
        bundleExecutor = ctx.getBean("bundleExecutor", ScriptBundleExecutorImpl.class);
        dataSource = ctx.getBean("dataSource", DataSource.class);
        jdbcTmpl = new JdbcTemplate(dataSource);
    }
    
    @Test
    public void canExecuteBundle()
    {
        bundleExecutor.exec("scriptexec/${db.script.dialect}/bundle", "script_a.sql", "script_b.sql", "script_c.sql");
        
        String select = "select message from alf_test_bundle order by message asc";
        List<String> res = jdbcTmpl.queryForList(select, String.class);
        assertEquals(2, res.size());
        // script_c deleted "script_a message 1"
        assertEquals("script_a message 2", res.get(0));
        assertEquals("script_b", res.get(1));
    }
    
    @Test
    public void postScriptIsRunFinallyEvenAfterEarlierFailure()
    {
        // script_b.sql will fail
        bundleExecutor.execWithPostScript("scriptexec/${db.script.dialect}/bundle2",
                    "post_script.sql", "script_a.sql", "script_b.sql");
        
        String select = "select message from alf_test_bundle2 order by message asc";
        List<String> res = jdbcTmpl.queryForList(select, String.class);
        assertEquals(1, res.size());
        // post_script deleted "script_a message 1"
        assertEquals("script_a message 2", res.get(0));
    }
}
