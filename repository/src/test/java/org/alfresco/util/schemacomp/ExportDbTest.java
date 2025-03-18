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
package org.alfresco.util.schemacomp;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import org.alfresco.repo.domain.dialect.Dialect;
import org.alfresco.repo.domain.dialect.MySQLInnoDBDialect;
import org.alfresco.repo.domain.dialect.PostgreSQLDialect;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.schemacomp.model.Schema;
import org.alfresco.util.schemacomp.test.exportdb.AbstractExportTester;
import org.alfresco.util.schemacomp.test.exportdb.MySQLDialectExportTester;
import org.alfresco.util.schemacomp.test.exportdb.PostgreSQLDialectExportTester;
import org.alfresco.util.testing.category.DBTests;

/**
 * Tests for the ExportDb class. Loads the database into an in-memory {@link Schema} representation.
 * <p>
 * This test is DBMS specific, if the test is run on a system configured against MySQL for example, it will run MySQL specific tests. If there is no test available for the configured DBMS then the test will pass - this allows addition of new DBMS-specific tests when available.
 * 
 * @see AbstractExportTester
 * @author Matt Ward
 */
@Category({OwnJVMTestsCategory.class, DBTests.class})
public class ExportDbTest
{
    private ApplicationContext ctx;
    private ExportDb exporter;
    private Dialect dialect;
    private DataSource dataSource;
    private JdbcTemplate jdbcTemplate;
    private PlatformTransactionManager tx;
    private AbstractExportTester exportTester;
    private static final Log logger = LogFactory.getLog(ExportDbTest.class);

    @Before
    public void setUp() throws Exception
    {
        ctx = ApplicationContextHelper.getApplicationContext();
        dataSource = (DataSource) ctx.getBean("dataSource");
        tx = (PlatformTransactionManager) ctx.getBean("transactionManager");
        jdbcTemplate = new JdbcTemplate(dataSource);
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
        else if (MySQLInnoDBDialect.class.isAssignableFrom(dialectClass))
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
