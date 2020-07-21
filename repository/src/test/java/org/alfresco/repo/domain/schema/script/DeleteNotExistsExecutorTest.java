/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
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
import static org.mockito.Mockito.when;

import java.io.File;
import java.sql.Connection;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import org.alfresco.util.ApplicationContextHelper;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Integration tests for the {@link DeleteNotExistsExecutor} class.
 * 
 * @author Cristian Turlica
 */
public class DeleteNotExistsExecutorTest
{
    private static ApplicationContext ctx;
    private ScriptExecutor scriptExecutor;
    private DataSource dataSource;
    private JdbcTemplate jdbcTmpl;

    @BeforeClass
    public static void setUpBeforeClass()
    {
        String[] config = new String[] { "classpath:alfresco/application-context.xml", "classpath:scriptexec/script-exec-test.xml" };
        ctx = ApplicationContextHelper.getApplicationContext(config);
    }

    @Before
    public void setUp() throws Exception
    {
        scriptExecutor = ctx.getBean("simpleScriptExecutor", ScriptExecutorImpl.class);
        dataSource = ctx.getBean("dataSource", DataSource.class);
        jdbcTmpl = new JdbcTemplate(dataSource);
    }

    @Test()
    public void testDefaultBehaviour() throws Exception
    {
        scriptExecutor.executeScriptUrl("scriptexec/${db.script.dialect}/delete-not-exists/test-data1.sql");

        String sql = "--DELETE_NOT_EXISTS temp_tst_tbl_1.id,temp_tst_tbl_2.tbl_2_id,temp_tst_tbl_3.tbl_3_id,temp_tst_tbl_4.tbl_4_id system.delete_not_exists.batchsize";
        int line = 1;
        File scriptFile = Mockito.mock(File.class);
        Properties properties = Mockito.mock(Properties.class);

        String select = "select id from temp_tst_tbl_1 order by id ASC";

        try (Connection connection = dataSource.getConnection())
        {
            connection.setAutoCommit(true);

            // Test read only
            {
                when(properties.getProperty(DeleteNotExistsExecutor.PROPERTY_READ_ONLY)).thenReturn("true");
                when(properties.getProperty(DeleteNotExistsExecutor.PROPERTY_TIMEOUT_SECONDS)).thenReturn("-1");
                DeleteNotExistsExecutor deleteNotExistsExecutor = new DeleteNotExistsExecutor(connection, sql, line, scriptFile, properties);
                deleteNotExistsExecutor.execute();

                List<String> res = jdbcTmpl.queryForList(select, String.class);
                assertEquals(7, res.size());
            }

            {
                when(properties.getProperty(DeleteNotExistsExecutor.PROPERTY_READ_ONLY)).thenReturn("false");
                when(properties.getProperty(DeleteNotExistsExecutor.PROPERTY_TIMEOUT_SECONDS)).thenReturn("-1");
                DeleteNotExistsExecutor deleteNotExistsExecutor = new DeleteNotExistsExecutor(connection, sql, line, scriptFile, properties);
                deleteNotExistsExecutor.execute();

                List<String> res = jdbcTmpl.queryForList(select, String.class);
                assertEquals(5, res.size());

                assertEquals("1", res.get(0));
                assertEquals("2", res.get(1));
                assertEquals("4", res.get(2));
                assertEquals("10", res.get(3));
                assertEquals("11", res.get(4));
            }
        }
    }

    @Test()
    public void testDeleteBatch() throws Exception
    {
        scriptExecutor.executeScriptUrl("scriptexec/${db.script.dialect}/delete-not-exists/test-data1.sql");

        String sql = "--DELETE_NOT_EXISTS temp_tst_tbl_1.id,temp_tst_tbl_2.tbl_2_id,temp_tst_tbl_3.tbl_3_id,temp_tst_tbl_4.tbl_4_id system.delete_not_exists.batchsize";
        int line = 1;
        File scriptFile = Mockito.mock(File.class);
        Properties properties = Mockito.mock(Properties.class);

        String select = "select id from temp_tst_tbl_1 order by id ASC";

        try (Connection connection = dataSource.getConnection())
        {
            connection.setAutoCommit(true);
            {
                when(properties.getProperty(DeleteNotExistsExecutor.PROPERTY_DELETE_BATCH_SIZE)).thenReturn("1");
                when(properties.getProperty(DeleteNotExistsExecutor.PROPERTY_READ_ONLY)).thenReturn("false");
                DeleteNotExistsExecutor deleteNotExistsExecutor = new DeleteNotExistsExecutor(connection, sql, line, scriptFile, properties);
                deleteNotExistsExecutor.execute();

                List<String> res = jdbcTmpl.queryForList(select, String.class);
                assertEquals(5, res.size());

                assertEquals("1", res.get(0));
                assertEquals("2", res.get(1));
                assertEquals("4", res.get(2));
                assertEquals("10", res.get(3));
                assertEquals("11", res.get(4));
            }
        }
    }

    @Test()
    public void testBatchExecute() throws Exception
    {
        scriptExecutor.executeScriptUrl("scriptexec/${db.script.dialect}/delete-not-exists/test-data1.sql");

        String sql = "--DELETE_NOT_EXISTS temp_tst_tbl_1.id,temp_tst_tbl_2.tbl_2_id,temp_tst_tbl_3.tbl_3_id,temp_tst_tbl_4.tbl_4_id system.delete_not_exists.batchsize";
        int line = 1;
        File scriptFile = Mockito.mock(File.class);
        Properties properties = Mockito.mock(Properties.class);

        String select = "select id from temp_tst_tbl_1 order by id ASC";

        try (Connection connection = dataSource.getConnection())
        {
            connection.setAutoCommit(true);
            {
                when(properties.getProperty(DeleteNotExistsExecutor.PROPERTY_BATCH_SIZE)).thenReturn("2");
                when(properties.getProperty(DeleteNotExistsExecutor.PROPERTY_READ_ONLY)).thenReturn("false");
                when(properties.getProperty(DeleteNotExistsExecutor.PROPERTY_TIMEOUT_SECONDS)).thenReturn("-1");
                DeleteNotExistsExecutor deleteNotExistsExecutor = new DeleteNotExistsExecutor(connection, sql, line, scriptFile, properties);
                deleteNotExistsExecutor.execute();

                List<String> res = jdbcTmpl.queryForList(select, String.class);
                assertEquals(5, res.size());

                assertEquals("1", res.get(0));
                assertEquals("2", res.get(1));
                assertEquals("4", res.get(2));
                assertEquals("10", res.get(3));
                assertEquals("11", res.get(4));
            }
        }
    }
}
