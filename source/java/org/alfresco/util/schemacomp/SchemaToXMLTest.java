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


import static org.alfresco.util.schemacomp.SchemaCompTestingUtils.columns;
import static org.alfresco.util.schemacomp.SchemaCompTestingUtils.fk;
import static org.alfresco.util.schemacomp.SchemaCompTestingUtils.fkeys;
import static org.alfresco.util.schemacomp.SchemaCompTestingUtils.indexes;
import static org.alfresco.util.schemacomp.SchemaCompTestingUtils.pk;
import static org.alfresco.util.schemacomp.SchemaCompTestingUtils.table;
import static org.alfresco.util.schemacomp.SchemaCompTestingUtils.sequence;
import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.transform.stream.StreamResult;

import org.alfresco.util.schemacomp.model.Schema;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the SchemaToXML class.
 * @author Matt Ward
 */
public class SchemaToXMLTest
{
    @Before
    public void setUp() throws Exception
    {
    }

    @Test
    public void canTransformSchemaToXML() throws IOException
    {
        Writer writer = new StringWriter();
        StreamResult out = new StreamResult(writer);
        
        Schema schema = new Schema("alfresco");
        
        schema.add(
                    table("node",
                          columns("id NUMBER(10)",
                                  "nodeRef VARCHAR2(200)",
                                  "name VARCHAR2(150)"), 
                          pk("pk_node", "id"),
                          fkeys(fk("fk_node_noderef", "nodeRef", "node", "nodeRef")),
                          indexes("idx_node_by_id id nodeRef")));
        schema.add(sequence("node_seq"));
        schema.add(sequence("content_seq"));
        
        SchemaToXML transformer = new SchemaToXML(schema, out);
        
        transformer.execute();
        
        System.out.println(writer.toString());
        
        // Check the first couple of lines, details tests of the actual content
        // are performed by DbObjectXMLTransformerTest
        BufferedReader reader = new BufferedReader(new StringReader(writer.toString()));
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", reader.readLine());
        assertEquals("<schema name=\"alfresco\">", reader.readLine());        
        assertEquals("  <objects>", reader.readLine());
    }
}
