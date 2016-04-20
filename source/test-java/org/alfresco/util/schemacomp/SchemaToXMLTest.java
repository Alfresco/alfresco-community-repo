package org.alfresco.util.schemacomp;


import static org.alfresco.util.schemacomp.SchemaCompTestingUtils.columns;
import static org.alfresco.util.schemacomp.SchemaCompTestingUtils.fk;
import static org.alfresco.util.schemacomp.SchemaCompTestingUtils.fkeys;
import static org.alfresco.util.schemacomp.SchemaCompTestingUtils.indexes;
import static org.alfresco.util.schemacomp.SchemaCompTestingUtils.pk;
import static org.alfresco.util.schemacomp.SchemaCompTestingUtils.table;
import static org.alfresco.util.schemacomp.SchemaCompTestingUtils.sequence;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
        
        Schema schema = new Schema("alfresco", "my-prefix", 501, true);
        
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
        String firstLine = reader.readLine();
        assertTrue(firstLine.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
        String xsd =
            "xmlns=\"http://www.alfresco.org/repo/db-schema\" " + 
            "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
            "xsi:schemaLocation=\"http://www.alfresco.org/repo/db-schema db-schema.xsd\"";
        assertTrue(firstLine.endsWith("<schema " + xsd + " name=\"alfresco\" dbprefix=\"my-prefix\" version=\"501\" tablecolumnorder=\"true\">"));        
        assertEquals("  <validators>", reader.readLine());
    }
}
