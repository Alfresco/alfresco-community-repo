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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertNull;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Iterator;

import org.alfresco.util.schemacomp.model.DbObject;
import org.alfresco.util.schemacomp.model.Index;
import org.alfresco.util.schemacomp.model.Schema;
import org.alfresco.util.schemacomp.model.Sequence;
import org.alfresco.util.schemacomp.model.Table;
import org.alfresco.util.schemacomp.validator.DbValidator;
import org.alfresco.util.schemacomp.validator.NameValidator;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * Tests for the XMLToSchema class.
 * 
 * @author Matt Ward
 */
public class XMLToSchemaTest
{
    private XMLToSchema xmlToSchema;
    private InputStream in;
    
    @Before
    public void setUp() throws Exception
    {
        Resource testFileResource = new ClassPathResource("schemacomp/xml_to_schema_test.xml");
        in = new BufferedInputStream(testFileResource.getInputStream());
        xmlToSchema = new XMLToSchema(in);
    }

    @Test
    public void canReadSchemaXML()
    {
        xmlToSchema.parse();
        Schema schema = xmlToSchema.getSchema();
        
        assertNotNull("A null Schema object was returned", schema);
        assertNull("Schema isn't meant to have a parent", schema.getParent());
        assertEquals("alfresco", schema.getName());
        
        Iterator<DbObject> objects = schema.iterator();
        
        Table table = (Table) objects.next();
        assertSame("Wrong or no parent set on table", schema, table.getParent());
        assertEquals("node", table.getName());
        assertEquals(3, table.getColumns().size());
        
        assertSame("Wrong or no parent set", table, table.getColumns().get(0).getParent());
        assertEquals("id", table.getColumns().get(0).getName());
        assertEquals("NUMBER(10)", table.getColumns().get(0).getType());
        assertEquals(false, table.getColumns().get(0).isNullable());
        assertEquals(1, table.getColumns().get(0).getOrder());
        
        assertSame("Wrong or no parent set", table, table.getColumns().get(1).getParent());
        assertEquals("nodeRef", table.getColumns().get(1).getName());
        assertEquals("VARCHAR2(200)", table.getColumns().get(1).getType());
        assertEquals(false, table.getColumns().get(1).isNullable());        
        assertEquals(3, table.getColumns().get(1).getOrder());
        
        assertSame("Wrong or no parent set", table, table.getColumns().get(2).getParent());
        assertEquals("name", table.getColumns().get(2).getName());
        assertEquals("VARCHAR2(150)", table.getColumns().get(2).getType());
        assertEquals(true, table.getColumns().get(2).isNullable());
        assertEquals(2, table.getColumns().get(2).getOrder());
        
        assertSame("Wrong or no parent set", table, table.getPrimaryKey().getParent());
        assertEquals("pk_node", table.getPrimaryKey().getName());
        assertEquals(1, table.getPrimaryKey().getColumnNames().size());
        assertEquals("id", table.getPrimaryKey().getColumnNames().get(0));
        assertEquals(1, table.getPrimaryKey().getColumnOrders().get(0).intValue());
        
        assertEquals(1, table.getForeignKeys().size());
        assertSame("Wrong or no parent set", table, table.getForeignKeys().get(0).getParent());
        assertEquals("fk_node_noderef", table.getForeignKeys().get(0).getName());
        assertEquals("nodeRef", table.getForeignKeys().get(0).getLocalColumn());
        assertEquals("node", table.getForeignKeys().get(0).getTargetTable());
        assertEquals("nodeRef", table.getForeignKeys().get(0).getTargetColumn());
        
        assertEquals(1, table.getIndexes().size());
        Index index = table.getIndexes().get(0);
        assertSame("Wrong or no parent set on index", table, index.getParent());
        assertEquals("idx_node_by_id", index.getName());
        assertEquals(true, index.isUnique());        
        assertEquals(2, index.getColumnNames().size());
        assertEquals("id", index.getColumnNames().get(0));
        assertEquals("nodeRef", index.getColumnNames().get(1));
        assertEquals(1, index.getValidators().size());
        DbValidator validator = index.getValidators().get(0);
        assertEquals(NameValidator.class, validator.getClass());
        assertEquals(1, validator.getPropertyNames().size());
        assertEquals("idx_.+", validator.getProperty("pattern"));
        
        Sequence seq = (Sequence) objects.next();
        assertSame("Wrong or no parent set", schema, seq.getParent());
        assertEquals("node_seq", seq.getName());
        
        seq = (Sequence) objects.next();
        assertSame("Wrong or no parent set", schema, seq.getParent());
        assertEquals("person_seq", seq.getName());
        
        seq = (Sequence) objects.next();
        assertSame("Wrong or no parent set", schema, seq.getParent());
        assertEquals("content_seq", seq.getName());
        
        assertFalse("Should be no more DB objects", objects.hasNext());
    }
}
