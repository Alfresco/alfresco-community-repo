/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.util.schemacomp.test.exportdb;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.alfresco.util.schemacomp.ExportDb;
import org.alfresco.util.schemacomp.ExportDbTest;
import org.alfresco.util.schemacomp.model.DbObject;
import org.alfresco.util.schemacomp.model.Schema;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Base class for DBMS-specific ExportDb tests.
 * 
 * @see ExportDbTest
 * @author Matt Ward
 */
public abstract class AbstractExportTester
{
    protected ExportDb exporter;
    protected PlatformTransactionManager tx;
    protected SimpleJdbcTemplate jdbcTemplate;
    private final static Log log = LogFactory.getLog(AbstractExportTester.class);
    
    public AbstractExportTester(ExportDb exporter, PlatformTransactionManager tx, SimpleJdbcTemplate jdbcTemplate)
    {
        this.exporter = exporter;
        this.tx = tx;
        this.jdbcTemplate = jdbcTemplate;
    }
    
    protected abstract void doExportTest() throws Exception;

    protected abstract void doDatabaseSetup();
    
    public void runExportTest() throws Exception
    {
        doDatabaseSetup();
        exporter.execute();
        // Log the schema for diagnostics
        dumpSchema();
        commonPostExportChecks();
        doExportTest();
    }
    
    
    /**
     * Common checks that do not need to be coded into every test implementation.
     * May be overridden if required.
     */
    protected void commonPostExportChecks()
    {
        Schema schema = getSchema();
        assertNull("Schema shouldn't have a parent", getSchema().getParent());
        checkResultsFiltered(schema, "export_test_");
    }

    public Schema getSchema()
    {
        return exporter.getSchema();
    }

    /**
     * Check that all top level database objects are prefixed as expected
     * (no other objects should have been retrieved)
     * 
     * @param schema
     * @param prefix
     */
    protected void checkResultsFiltered(Schema schema, String prefix)
    {
        for (DbObject dbo : schema)
        {
            if (!dbo.getName().startsWith(prefix))
            {
                fail("Database object's name does not start with '" + prefix + "': " + dbo);
            }
        }
    }
    
    
    private void dumpSchema()
    {
        if (log.isDebugEnabled())
        {
            log.debug("Iterating through Schema objects:");
        }
        int i = 0;
        for (DbObject dbo : getSchema())
        {
            i++;
            if (log.isDebugEnabled())
            {
                // Log the object's toString() - indented for clarity.
                log.debug("    " + dbo);
            }   
        }
        if (log.isDebugEnabled())
        {
            log.debug("Schema object contains " + i + " objects.");
        }
    }
}
