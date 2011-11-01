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


import java.util.Iterator;

import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.schemacomp.model.Column;
import org.alfresco.util.schemacomp.model.DbObject;
import org.alfresco.util.schemacomp.model.ForeignKey;
import org.alfresco.util.schemacomp.model.Index;
import org.alfresco.util.schemacomp.model.Schema;
import org.alfresco.util.schemacomp.model.Sequence;
import org.alfresco.util.schemacomp.model.Table;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

/**
 * Tests for the ExportDb class.
 * 
 * @author Matt Ward
 */
public class ExportDbTest
{
    private ApplicationContext ctx;
    private ExportDb exporter;
    
    @Before
    public void setUp() throws Exception
    {
        ctx = ApplicationContextHelper.getApplicationContext();
        exporter = new ExportDb(ctx);
    }

    
    @Test
    public void exportDb() throws Exception
    {       
       exporter.execute();
       
       Schema schema = exporter.getSchema();
       System.out.println(schema);
       
       Table appliedPatchTable = null;
       Table qNameTable = null;
       Sequence authoritySeq = null;
       
       for (DbObject dbo : schema)
       {
           if (dbo.getName().equals("alf_applied_patch"))
           {
               appliedPatchTable = (Table) dbo;
           }
           if (dbo.getName().equals("alf_qname"))
           {
               qNameTable = (Table) dbo;
           }
           if (dbo.getName().equals("alf_authority_seq"))
           {
               authoritySeq = (Sequence) dbo;
           }
       }
       
       checkAppliedPatchTable(appliedPatchTable);
       checkQNameTable(qNameTable);
       // TODO: what to do about sequences? They can't easily be retrieved with JDBC's DatabaseMetaData
       //checkAuthoritySequence(authoritySeq);
    }


    /**
     * @param qNameTable
     */
    private void checkQNameTable(Table qNameTable)
    {
        /*
            CREATE TABLE alf_qname
            (
                id INT8 NOT NULL,
                version INT8 NOT NULL,
                ns_id INT8 NOT NULL,
                local_name VARCHAR(200) NOT NULL,
                CONSTRAINT fk_alf_qname_ns FOREIGN KEY (ns_id) REFERENCES alf_namespace (id),    
                PRIMARY KEY (id)
            );
            CREATE UNIQUE INDEX ns_id ON alf_qname (ns_id, local_name);
            CREATE INDEX fk_alf_qname_ns ON alf_qname (ns_id);
         */
        
        assertNotNull("Couldn't find table alf_qname", qNameTable);
        
        Iterator<Column> colIt = qNameTable.getColumns().iterator();
        Column col = colIt.next();
        assertEquals("id", col.getName());
        assertEquals("int8", col.getType());
        assertEquals(false, col.isNullable());
        
        col = colIt.next();
        assertEquals("version", col.getName());
        assertEquals("int8", col.getType());
        assertEquals(false, col.isNullable());
        
        col = colIt.next();
        assertEquals("ns_id", col.getName());
        assertEquals("int8", col.getType());
        assertEquals(false, col.isNullable());
        
        col = colIt.next();
        assertEquals("local_name", col.getName());
        assertEquals("varchar(200)", col.getType());
        assertEquals(false, col.isNullable());
        
        assertEquals(2, qNameTable.getIndexes().size());
        Iterator<Index> indexIt = qNameTable.getIndexes().iterator();
        
        Index index = indexIt.next();
        assertEquals("ns_id", index.getName());
        assertEquals(true, index.isUnique());
        assertEquals(2, index.getColumnNames().size());
        assertEquals("ns_id", index.getColumnNames().get(0));
        assertEquals("local_name", index.getColumnNames().get(1));

        index = indexIt.next();
        assertEquals("fk_alf_qname_ns", index.getName());
        assertEquals(1, index.getColumnNames().size());
        assertEquals("ns_id", index.getColumnNames().get(0));
        
        assertEquals("id", qNameTable.getPrimaryKey().getColumnNames().get(0));
        
        assertEquals(1, qNameTable.getForeignKeys().size());
        ForeignKey fk = qNameTable.getForeignKeys().get(0);
        assertEquals("fk_alf_qname_ns", fk.getName());
        assertEquals("ns_id", fk.getLocalColumn());
        assertEquals("alf_namespace", fk.getTargetTable());
        assertEquals("id", fk.getTargetColumn());
    }


    /**
     * @param appliedPatch
     */
    private void checkAppliedPatchTable(Table appliedPatch)
    {
        /*
             CREATE TABLE alf_applied_patch
             (
                 id VARCHAR(64) NOT NULL,
                 description VARCHAR(1024),
                 fixes_from_schema INT4,
                 fixes_to_schema INT4,
                 applied_to_schema INT4,
                 target_schema INT4,
                 applied_on_date TIMESTAMP,
                 applied_to_server VARCHAR(64),
                 was_executed BOOL,
                 succeeded BOOL,
                 report VARCHAR(1024),
                 PRIMARY KEY (id)
             );
        */
        assertNotNull("Couldn't find alf_applied_patch", appliedPatch);
        
        assertEquals("alf_applied_patch", appliedPatch.getName());
        Iterator<Column> colIt = appliedPatch.getColumns().iterator();
        Column col = colIt.next();
        assertEquals("id", col.getName());
        assertEquals("varchar(64)", col.getType());
        assertEquals(false, col.isNullable());
        
        col = colIt.next();
        assertEquals("description", col.getName());
        assertEquals("varchar(1024)", col.getType());
        assertEquals(true, col.isNullable());
        
        col = colIt.next();
        assertEquals("fixes_from_schema", col.getName());
        assertEquals("int4", col.getType());
        assertEquals(true, col.isNullable());
        
        col = colIt.next();
        assertEquals("fixes_to_schema", col.getName());
        assertEquals("int4", col.getType());
        assertEquals(true, col.isNullable());
        
        col = colIt.next();
        assertEquals("applied_to_schema", col.getName());
        assertEquals("int4", col.getType());
        assertEquals(true, col.isNullable());
        
        col = colIt.next();
        assertEquals("target_schema", col.getName());
        assertEquals("int4", col.getType());
        assertEquals(true, col.isNullable());
        
        col = colIt.next();
        assertEquals("applied_on_date", col.getName());
        assertEquals("timestamp", col.getType());
        assertEquals(true, col.isNullable());
        
        col = colIt.next();
        assertEquals("applied_to_server", col.getName());
        assertEquals("varchar(64)", col.getType());
        assertEquals(true, col.isNullable());
        
        col = colIt.next();
        assertEquals("was_executed", col.getName());
        assertEquals("bool", col.getType());
        assertEquals(true, col.isNullable());
        
        col = colIt.next();
        assertEquals("succeeded", col.getName());
        assertEquals("bool", col.getType());
        assertEquals(true, col.isNullable());
        
        col = colIt.next();
        assertEquals("report", col.getName());
        assertEquals("varchar(1024)", col.getType());
        assertEquals(true, col.isNullable());
        
        assertEquals("id", appliedPatch.getPrimaryKey().getColumnNames().get(0));
    }
    
    
    public void checkAuthoritySequence(Sequence seq)
    {
        /*
            CREATE SEQUENCE alf_authority_seq START WITH 1 INCREMENT BY 1;
         */
        assertNotNull("Couldn't find sequence alf_authority_seq", seq);
        assertEquals("alf_authority_seq", seq.getName());
    }
}
