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
package org.alfresco.util.schemacomp;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.util.Iterator;

import javax.sql.DataSource;

import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.schemacomp.model.Column;
import org.alfresco.util.schemacomp.model.DbObject;
import org.alfresco.util.schemacomp.model.ForeignKey;
import org.alfresco.util.schemacomp.model.Index;
import org.alfresco.util.schemacomp.model.PrimaryKey;
import org.alfresco.util.schemacomp.model.Schema;
import org.alfresco.util.schemacomp.model.Sequence;
import org.alfresco.util.schemacomp.model.Table;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.PostgreSQLDialect;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Tests for the ExportDb class.
 * 
 * @author Matt Ward
 */
public class ExportDbTest
{
    private ApplicationContext ctx;
    private ExportDb exporter;
    private Dialect dialect;
    private DataSource dataSource;
    private SimpleJdbcTemplate jdbcTemplate;
    private PlatformTransactionManager tx;
    
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
        if (dialect.getClass().equals(PostgreSQLDialect.class))
        {
            exportPostgreSQL();
        }
    }
    
    
    private void exportPostgreSQL() throws Exception
    {
       setupPostgreSQL();

       exporter.execute();
       
       Schema schema = exporter.getSchema();
       
       assertNull("Schema shouldn't have a parent", schema.getParent());
       System.out.println(schema);
       
       Table exampleTable = null;
       Table otherTable = null;
       Sequence exampleSeq = null;
       
       for (DbObject dbo : schema)
       {
           if (dbo.getName().equals("export_test_example"))
           {
               exampleTable = (Table) dbo;
           }
           if (dbo.getName().equals("export_test_other"))
           {
               otherTable = (Table) dbo;
           }
           if (dbo.getName().equals("export_test_example_seq"))
           {
               exampleSeq = (Sequence) dbo;
           }
       }
       
       checkResultsFiltered(schema, "export_test_");
       checkExampleTable(schema, exampleTable);
       checkOtherTable(schema, otherTable);
       checkExampleSequence(schema, exampleSeq);
    }
   
    
    private void setupPostgreSQL()
    {
        // Create database objects: this decouples test code from the actual schema which is
        // free to change without breaking these tests.
        
        final String[] createStatements = new String[]
        {
                    "DROP TABLE IF EXISTS export_test_example CASCADE",
                    "CREATE TABLE export_test_example" + 
                    "             (" + 
                    "                 id INT8 NOT NULL," + 
                    "                 description VARCHAR(1024)," + 
                    "                 fixes_from_schema INT4," + 
                    "                 fixes_to_schema INT4," + 
                    "                 applied_to_schema INT4," + 
                    "                 target_schema INT4," + 
                    "                 applied_on_date TIMESTAMP," + 
                    "                 applied_to_server VARCHAR(64)," + 
                    "                 was_executed BOOL," + 
                    "                 succeeded BOOL," + 
                    "                 report VARCHAR(1024)," + 
                    "                 PRIMARY KEY (id)" + 
                    "             )",
                    
                    "DROP TABLE IF EXISTS export_test_other CASCADE",
                    "CREATE TABLE export_test_other" + 
                    "            (" + 
                    "                id INT8 NOT NULL," + 
                    "                version INT8 NOT NULL," + 
                    "                ex_id INT8 NOT NULL," + 
                    "                local_name VARCHAR(200) NOT NULL," + 
                    "                CONSTRAINT export_test_fk_example FOREIGN KEY (ex_id) REFERENCES export_test_example (id)," + 
                    "                PRIMARY KEY (id)" + 
                    "            )",
                    
                    "DROP INDEX IF EXISTS export_test_idx_other_1",
                    "CREATE UNIQUE INDEX export_test_idx_other_1 ON export_test_other (ex_id, local_name)",
                    
                    "DROP INDEX IF EXISTS export_test_idx_other_2",
                    "CREATE INDEX export_test_idx_other_2 ON export_test_other (ex_id)",
                    
                    "DROP SEQUENCE IF EXISTS export_test_example_seq",
                    "CREATE SEQUENCE export_test_example_seq START WITH 1 INCREMENT BY 1"
        };
        
        TransactionTemplate tt = new TransactionTemplate(tx);
        tt.execute(new TransactionCallbackWithoutResult()
        {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status)
            {
                for (String sql : createStatements)
                {            
                    jdbcTemplate.update(sql);
                }
            }   
        });
    }


    /**
     * Check that all top level database objects are prefixed as expected
     * (no other objects should have been retrieved)
     * 
     * @param schema
     * @param prefix
     */
    private void checkResultsFiltered(Schema schema, String prefix)
    {
        for (DbObject dbo : schema)
        {
            if (!dbo.getName().startsWith(prefix))
            {
                fail("Database object's name does not start with '" + prefix + "': " + dbo);
            }
        }
    }


    private void checkOtherTable(Schema schema, Table otherTable)
    {
        assertNotNull("Couldn't find table export_test_other", otherTable);
        assertSame("Incorrect parent or no parent set", schema, otherTable.getParent());
        
        Iterator<Column> colIt = otherTable.getColumns().iterator();
        Column col = colIt.next();
        assertSame("Incorrect parent or no parent set", otherTable, col.getParent());
        assertEquals("id", col.getName());
        assertEquals("int8", col.getType());
        assertEquals(false, col.isNullable());
        
        col = colIt.next();
        assertSame("Incorrect parent or no parent set", otherTable, col.getParent());
        assertEquals("version", col.getName());
        assertEquals("int8", col.getType());
        assertEquals(false, col.isNullable());
        
        col = colIt.next();
        assertSame("Incorrect parent or no parent set", otherTable, col.getParent());
        assertEquals("ex_id", col.getName());
        assertEquals("int8", col.getType());
        assertEquals(false, col.isNullable());
        
        col = colIt.next();
        assertSame("Incorrect parent or no parent set", otherTable, col.getParent());
        assertSame("Incorrect parent or no parent set", otherTable, col.getParent());
        assertEquals("local_name", col.getName());
        assertEquals("varchar(200)", col.getType());
        assertEquals(false, col.isNullable());
        
        assertEquals(2, otherTable.getIndexes().size());
        Iterator<Index> indexIt = otherTable.getIndexes().iterator();
        
        Index index = indexIt.next();
        assertSame("Incorrect parent or no parent set", otherTable, index.getParent());
        assertEquals("export_test_idx_other_1", index.getName());
        assertEquals(true, index.isUnique());
        assertEquals(2, index.getColumnNames().size());
        assertEquals("ex_id", index.getColumnNames().get(0));
        assertEquals("local_name", index.getColumnNames().get(1));

        index = indexIt.next();
        assertSame("Incorrect parent or no parent set", otherTable, index.getParent());
        assertEquals("export_test_idx_other_2", index.getName());
        assertEquals(1, index.getColumnNames().size());
        assertEquals("ex_id", index.getColumnNames().get(0));
        
        PrimaryKey pk = otherTable.getPrimaryKey();
        assertSame("Incorrect parent or no parent set", otherTable, pk.getParent());
        assertEquals("id", pk.getColumnNames().get(0));
        
        assertEquals(1, otherTable.getForeignKeys().size());
        ForeignKey fk = otherTable.getForeignKeys().get(0);
        assertSame("Incorrect parent or no parent set", otherTable, fk.getParent());
        assertEquals("export_test_fk_example", fk.getName());
        assertEquals("ex_id", fk.getLocalColumn());
        assertEquals("export_test_example", fk.getTargetTable());
        assertEquals("id", fk.getTargetColumn());
    }


    private void checkExampleTable(Schema schema, Table exampleTable)
    {
        assertNotNull("Couldn't find export_test_example", exampleTable);
        
        assertSame("Incorrect parent or no parent set", schema, exampleTable.getParent());
        assertEquals("export_test_example", exampleTable.getName());
        Iterator<Column> colIt = exampleTable.getColumns().iterator();
        Column col = colIt.next();
        assertSame("Incorrect parent or no parent set", exampleTable, col.getParent());
        assertEquals("id", col.getName());
        assertEquals("int8", col.getType());
        assertEquals(false, col.isNullable());
        
        col = colIt.next();
        assertSame("Incorrect parent or no parent set", exampleTable, col.getParent());
        assertEquals("description", col.getName());
        assertEquals("varchar(1024)", col.getType());
        assertEquals(true, col.isNullable());
        
        col = colIt.next();
        assertSame("Incorrect parent or no parent set", exampleTable, col.getParent());
        assertEquals("fixes_from_schema", col.getName());
        assertEquals("int4", col.getType());
        assertEquals(true, col.isNullable());
        
        col = colIt.next();
        assertSame("Incorrect parent or no parent set", exampleTable, col.getParent());
        assertEquals("fixes_to_schema", col.getName());
        assertEquals("int4", col.getType());
        assertEquals(true, col.isNullable());
        
        col = colIt.next();
        assertSame("Incorrect parent or no parent set", exampleTable, col.getParent());
        assertEquals("applied_to_schema", col.getName());
        assertEquals("int4", col.getType());
        assertEquals(true, col.isNullable());
        
        col = colIt.next();
        assertSame("Incorrect parent or no parent set", exampleTable, col.getParent());
        assertEquals("target_schema", col.getName());
        assertEquals("int4", col.getType());
        assertEquals(true, col.isNullable());
        
        col = colIt.next();
        assertSame("Incorrect parent or no parent set", exampleTable, col.getParent());
        assertEquals("applied_on_date", col.getName());
        assertEquals("timestamp", col.getType());
        assertEquals(true, col.isNullable());
        
        col = colIt.next();
        assertSame("Incorrect parent or no parent set", exampleTable, col.getParent());
        assertEquals("applied_to_server", col.getName());
        assertEquals("varchar(64)", col.getType());
        assertEquals(true, col.isNullable());
        
        col = colIt.next();
        assertSame("Incorrect parent or no parent set", exampleTable, col.getParent());
        assertEquals("was_executed", col.getName());
        assertEquals("bool", col.getType());
        assertEquals(true, col.isNullable());
        
        col = colIt.next();
        assertSame("Incorrect parent or no parent set", exampleTable, col.getParent());
        assertEquals("succeeded", col.getName());
        assertEquals("bool", col.getType());
        assertEquals(true, col.isNullable());
        
        col = colIt.next();
        assertSame("Incorrect parent or no parent set", exampleTable, col.getParent());
        assertEquals("report", col.getName());
        assertEquals("varchar(1024)", col.getType());
        assertEquals(true, col.isNullable());
        
        PrimaryKey pk = exampleTable.getPrimaryKey();
        assertSame("Incorrect parent or no parent set", exampleTable, pk.getParent());
        assertEquals("id", pk.getColumnNames().get(0));
    }
    
    
    public void checkExampleSequence(Schema schema, Sequence seq)
    {
        assertNotNull("Couldn't find sequence", seq);
        assertSame("Incorrect parent or no parent set", schema, seq.getParent());
        assertEquals("export_test_example_seq", seq.getName());
    }
}
