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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import java.util.Iterator;

import org.alfresco.util.schemacomp.ExportDb;
import org.alfresco.util.schemacomp.model.Column;
import org.alfresco.util.schemacomp.model.DbObject;
import org.alfresco.util.schemacomp.model.ForeignKey;
import org.alfresco.util.schemacomp.model.Index;
import org.alfresco.util.schemacomp.model.PrimaryKey;
import org.alfresco.util.schemacomp.model.Schema;
import org.alfresco.util.schemacomp.model.Table;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * MySQL specific test for the ExportDb class.
 * 
 * @author Matt Ward
 */
public class MySQLDialectExportTester extends AbstractExportTester
{
    /**
     * Constructor.
     * 
     * @param exporter
     * @param tx
     * @param jdbcTemplate
     */
    public MySQLDialectExportTester(ExportDb exporter, PlatformTransactionManager tx,
                SimpleJdbcTemplate jdbcTemplate)
    {
        super(exporter, tx, jdbcTemplate);
    }

    
    @Override
    protected void doExportTest() throws Exception
    {
       Schema schema = getSchema();
       Table exampleTable = null;
       Table otherTable = null;
       
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
       }
       
       checkExampleTable(schema, exampleTable);
       checkOtherTable(schema, otherTable);
    }
    


    private void checkOtherTable(Schema schema, Table otherTable)
    {
        assertNotNull("Couldn't find table export_test_other", otherTable);
        assertSame("Incorrect parent or no parent set", schema, otherTable.getParent());
        
        Iterator<Column> colIt = otherTable.getColumns().iterator();
        Column col = colIt.next();
        assertSame("Incorrect parent or no parent set", otherTable, col.getParent());
        assertEquals("id", col.getName());
        assertEquals("bigint", col.getType());
        assertEquals(false, col.isNullable());
        assertEquals(1, col.getOrder());
        
        col = colIt.next();
        assertSame("Incorrect parent or no parent set", otherTable, col.getParent());
        assertEquals("version", col.getName());
        assertEquals("bigint", col.getType());
        assertEquals(false, col.isNullable());
        assertEquals(2, col.getOrder());
        
        col = colIt.next();
        assertSame("Incorrect parent or no parent set", otherTable, col.getParent());
        assertEquals("ex_id", col.getName());
        assertEquals("bigint", col.getType());
        assertEquals(false, col.isNullable());
        assertEquals(3, col.getOrder());
        
        col = colIt.next();
        assertSame("Incorrect parent or no parent set", otherTable, col.getParent());
        assertSame("Incorrect parent or no parent set", otherTable, col.getParent());
        assertEquals("local_name", col.getName());
        assertEquals("varchar(200)", col.getType());
        assertEquals(false, col.isNullable());
        assertEquals(4, col.getOrder());
        
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
        assertEquals(1, pk.getColumnOrders().get(0).intValue());
        
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
        assertEquals("bigint", col.getType());
        assertEquals(false, col.isNullable());
        assertEquals(1, col.getOrder());
        
        col = colIt.next();
        assertSame("Incorrect parent or no parent set", exampleTable, col.getParent());
        assertEquals("description", col.getName());
        assertEquals("text", col.getType());
        assertEquals(true, col.isNullable());
        assertEquals(2, col.getOrder());
        
        col = colIt.next();
        assertSame("Incorrect parent or no parent set", exampleTable, col.getParent());
        assertEquals("fixes_from_schema", col.getName());
        assertEquals("int", col.getType());
        assertEquals(true, col.isNullable());
        assertEquals(3, col.getOrder());
        
        col = colIt.next();
        assertSame("Incorrect parent or no parent set", exampleTable, col.getParent());
        assertEquals("fixes_to_schema", col.getName());
        assertEquals("int", col.getType());
        assertEquals(true, col.isNullable());
        assertEquals(4, col.getOrder());
        
        col = colIt.next();
        assertSame("Incorrect parent or no parent set", exampleTable, col.getParent());
        assertEquals("applied_to_schema", col.getName());
        assertEquals("int", col.getType());
        assertEquals(true, col.isNullable());
        assertEquals(5, col.getOrder());
        
        col = colIt.next();
        assertSame("Incorrect parent or no parent set", exampleTable, col.getParent());
        assertEquals("target_schema", col.getName());
        assertEquals("int", col.getType());
        assertEquals(true, col.isNullable());
        assertEquals(6, col.getOrder());
        
        col = colIt.next();
        assertSame("Incorrect parent or no parent set", exampleTable, col.getParent());
        assertEquals("applied_on_date", col.getName());
        assertEquals("datetime", col.getType());
        assertEquals(true, col.isNullable());
        assertEquals(7, col.getOrder());
        
        col = colIt.next();
        assertSame("Incorrect parent or no parent set", exampleTable, col.getParent());
        assertEquals("applied_to_server", col.getName());
        assertEquals("varchar(64)", col.getType());
        assertEquals(true, col.isNullable());
        assertEquals(8, col.getOrder());
        
        col = colIt.next();
        assertSame("Incorrect parent or no parent set", exampleTable, col.getParent());
        assertEquals("was_executed", col.getName());
        assertEquals("bit", col.getType());
        assertEquals(true, col.isNullable());
        assertEquals(9, col.getOrder());
        
        col = colIt.next();
        assertSame("Incorrect parent or no parent set", exampleTable, col.getParent());
        assertEquals("succeeded", col.getName());
        assertEquals("bit", col.getType());
        assertEquals(true, col.isNullable());
        assertEquals(10, col.getOrder());
        
        col = colIt.next();
        assertSame("Incorrect parent or no parent set", exampleTable, col.getParent());
        assertEquals("report", col.getName());
        assertEquals("text", col.getType());
        assertEquals(true, col.isNullable());
        assertEquals(11, col.getOrder());
        
        PrimaryKey pk = exampleTable.getPrimaryKey();
        assertSame("Incorrect parent or no parent set", exampleTable, pk.getParent());
        assertEquals("id", pk.getColumnNames().get(0));
        assertEquals(1, pk.getColumnOrders().get(0).intValue());
    }
    
   
    @Override
    protected void doDatabaseSetup()
    {
        // Create database objects: this decouples test code from the actual schema which is
        // free to change without breaking these tests.
        
        final String[] createStatements = new String[]
        {
                    "DROP TABLE IF EXISTS export_test_other CASCADE",
                    "DROP TABLE IF EXISTS export_test_example CASCADE",
                    
                    "CREATE TABLE export_test_example" + 
                    "             (" + 
                    "                 id BIGINT NOT NULL AUTO_INCREMENT," + 
                    "                 description TEXT," + 
                    "                 fixes_from_schema INTEGER," + 
                    "                 fixes_to_schema INTEGER," + 
                    "                 applied_to_schema INTEGER," + 
                    "                 target_schema INTEGER," + 
                    "                 applied_on_date DATETIME," + 
                    "                 applied_to_server VARCHAR(64)," + 
                    "                 was_executed BIT," + 
                    "                 succeeded BIT," + 
                    "                 report TEXT," + 
                    "                 PRIMARY KEY (id)" + 
                    "             ) ENGINE=InnoDB",
                    
                    "CREATE TABLE export_test_other" + 
                    "            (" + 
                    "                id BIGINT NOT NULL AUTO_INCREMENT," + 
                    "                version BIGINT NOT NULL," + 
                    "                ex_id BIGINT NOT NULL," + 
                    "                local_name VARCHAR(200) NOT NULL," + 
                    "                CONSTRAINT export_test_fk_example FOREIGN KEY (ex_id) REFERENCES export_test_example (id)," + 
                    "                PRIMARY KEY (id)" + 
                    "            ) ENGINE=InnoDB",
                    
                    "CREATE UNIQUE INDEX export_test_idx_other_1 ON export_test_other (ex_id, local_name)",
                    
                    "CREATE INDEX export_test_idx_other_2 ON export_test_other (ex_id)"
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
}
