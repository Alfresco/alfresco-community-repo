/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
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
