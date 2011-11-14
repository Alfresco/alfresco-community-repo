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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.alfresco.util.schemacomp.model.Column;
import org.alfresco.util.schemacomp.model.DbObject;
import org.alfresco.util.schemacomp.model.ForeignKey;
import org.alfresco.util.schemacomp.model.Index;
import org.alfresco.util.schemacomp.model.PrimaryKey;
import org.alfresco.util.schemacomp.model.Schema;
import org.alfresco.util.schemacomp.model.Sequence;
import org.alfresco.util.schemacomp.model.Table;
import org.alfresco.util.schemacomp.validator.DbValidator;
import org.alfresco.util.schemacomp.validator.NameValidator;
import org.junit.Before;
import org.junit.Test;

import com.sun.star.uno.RuntimeException;

/**
 * Tests for the {@link DbObjectXMLTransformer} class.
 * 
 * @author Matt Ward
 */
public class DbObjectXMLTransformerTest
{
    private DbObjectXMLTransformer transformer;
    private TransformerHandler xmlOut;
    private Writer writer;
    private boolean outputDumpEnabled = true;
    
    @Before
    public void setUp()
    {   
        final SAXTransformerFactory stf = (SAXTransformerFactory) TransformerFactory.newInstance();
        try
        {
            xmlOut = stf.newTransformerHandler();
        }
        catch (TransformerConfigurationException error)
        {
            throw new RuntimeException("Unable to create TransformerHandler.", error);
        }
        final Transformer t = xmlOut.getTransformer();
        try
        {
            t.setOutputProperty("{http://xml.apache.org/xalan}indent-amount", "2");
        }
        catch (final IllegalArgumentException e)
        {
            // It was worth a try
        }
        t.setOutputProperty(OutputKeys.INDENT, "yes");
    
        writer = new StringWriter();
        xmlOut.setResult(new StreamResult(writer));
        
        transformer = new DbObjectXMLTransformer(xmlOut);        
    }
    
    
    @Test
    public void transformColumn() throws IOException
    {
        Column column = new Column(null, "last_name", "VARCHAR2(100)", true);
        
        transformer.output(column);
        
        BufferedReader reader = new BufferedReader(new StringReader(writer.toString()));
        dumpOutput();
        assertHasPreamble(reader);
        assertEquals("<column name=\"last_name\">", reader.readLine());
        assertEquals("  <type>VARCHAR2(100)</type>", reader.readLine());
        assertEquals("  <nullable>true</nullable>", reader.readLine());        
        assertEquals("</column>", reader.readLine());
    }
    
    
    @Test
    public void transformForeignKey() throws IOException
    {
        ForeignKey fk = new ForeignKey(null, "fk_for_some_table", 
                    "local_column", "target_table", "target_column");
        
        transformer.output(fk);
        
        BufferedReader reader = new BufferedReader(new StringReader(writer.toString()));
        dumpOutput();
        assertHasPreamble(reader);
        assertEquals("<foreignkey name=\"fk_for_some_table\">", reader.readLine());
        assertEquals("  <localcolumn>local_column</localcolumn>", reader.readLine());
        assertEquals("  <targettable>target_table</targettable>", reader.readLine());
        assertEquals("  <targetcolumn>target_column</targetcolumn>", reader.readLine());
        assertEquals("</foreignkey>", reader.readLine());
    }

    
    @Test
    public void transformIndex() throws IOException
    {
        Index index = new Index(null, "index_name", Arrays.asList("first", "second"));
        
        transformer.output(index);
        
        BufferedReader reader = new BufferedReader(new StringReader(writer.toString()));
        dumpOutput();
        assertHasPreamble(reader);
        assertEquals("<index name=\"index_name\" unique=\"false\">", reader.readLine());
        assertEquals("  <columnnames>", reader.readLine());
        assertEquals("    <columnname>first</columnname>", reader.readLine());
        assertEquals("    <columnname>second</columnname>", reader.readLine());                
        assertEquals("  </columnnames>", reader.readLine());
        assertEquals("</index>", reader.readLine());
    }
    
    
    @Test
    public void transformPrimaryKey() throws IOException
    {
        PrimaryKey pk = new PrimaryKey(null, "pk_name", Arrays.asList("first", "second"));
        
        transformer.output(pk);
        
        BufferedReader reader = new BufferedReader(new StringReader(writer.toString()));
        dumpOutput();
        assertHasPreamble(reader);
        assertEquals("<primarykey name=\"pk_name\">", reader.readLine());        
        assertEquals("  <columnnames>", reader.readLine());
        assertEquals("    <columnname>first</columnname>", reader.readLine());
        assertEquals("    <columnname>second</columnname>", reader.readLine());                
        assertEquals("  </columnnames>", reader.readLine());
        assertEquals("</primarykey>", reader.readLine());
    }
    
    @Test
    public void transformSchema() throws IOException
    {
        Collection<Column> columns = columns("one VARCHAR2(100)", "two NUMBER(10)");
        PrimaryKey pk = new PrimaryKey(null, "pk_for_my_table", Arrays.asList("id")); 
        Collection<ForeignKey> fks = fkeys(fk("fk_one", "lc", "tt", "tc"), fk("fk_two", "lc", "tt", "tc"));
        Collection<Index> indexes = indexes("index_one col1 col2", "index_two col3 col4");
        
        Table tableOne = new Table(null, "table_one", columns, pk, fks, indexes);
        Table tableTwo = new Table(null, "table_two", columns, pk, fks, indexes);
        
        Schema schema = new Schema("my_schema");
        schema.add(tableOne);
        schema.add(tableTwo);
        schema.add(new Sequence(null, "sequence_one"));
        schema.add(new Sequence(null, "sequence_two"));
        schema.add(new Sequence(null, "sequence_three"));
        
        transformer.output(schema);
        
        BufferedReader reader = new BufferedReader(new StringReader(writer.toString()));
        dumpOutput();
        assertHasPreamble(reader);
        assertEquals("<schema name=\"my_schema\">", reader.readLine());
        assertEquals("  <objects>", reader.readLine());
        skipUntilEnd("       {table}", reader);
        skipUntilEnd("       {table}", reader);
        skipUntilEnd("       {sequence}", reader, true);
        skipUntilEnd("       {sequence}", reader, true);
        skipUntilEnd("       {sequence}", reader, true);
        assertEquals("  </objects>", reader.readLine());
        assertEquals("</schema>", reader.readLine());
    }
    
    @Test
    public void transformSequence() throws IOException
    {
        Sequence sequence = new Sequence(null, "my_sequence");
        
        transformer.output(sequence);
        
        BufferedReader reader = new BufferedReader(new StringReader(writer.toString()));
        dumpOutput();
        assertHasPreamble(reader);
        assertEquals("<sequence name=\"my_sequence\"/>", reader.readLine());
    }
    
    @Test
    public void transformTable() throws IOException
    {
        Collection<Column> columns = columns("one VARCHAR2(100)", "two NUMBER(10)");
        PrimaryKey pk = new PrimaryKey(null, "pk_for_my_table", Arrays.asList("id")); 
        Collection<ForeignKey> fks = fkeys(fk("fk_one", "lc", "tt", "tc"), fk("fk_two", "lc", "tt", "tc"));
        Collection<Index> indexes = indexes("index_one col1 col2", "index_two col3 col4");
        
        Table table = new Table(null, "my_table", columns, pk, fks, indexes);
        
        transformer.output(table);
        
        BufferedReader reader = new BufferedReader(new StringReader(writer.toString()));
        dumpOutput();
        assertHasPreamble(reader);
        assertEquals("<table name=\"my_table\">", reader.readLine());
        assertEquals("  <columns>", reader.readLine());
        skipUntilEnd("       {column}", reader);
        skipUntilEnd("       {column}", reader);
        assertEquals("  </columns>", reader.readLine());
        skipUntilEnd("  {primarykey}", reader);
        assertEquals("  <foreignkeys>", reader.readLine());
        skipUntilEnd("       {foreignkey}", reader);
        skipUntilEnd("       {foreignkey}", reader);
        assertEquals("  </foreignkeys>", reader.readLine());
        assertEquals("  <indexes>", reader.readLine());
        skipUntilEnd("       {index}", reader);
        skipUntilEnd("       {index}", reader);
        assertEquals("  </indexes>", reader.readLine());
        assertEquals("</table>", reader.readLine());
    }
    
    
    @Test
    public void transformObjectWithValidators() throws IOException
    {
        Collection<Column> columns = columns("one VARCHAR2(100)", "two NUMBER(10)");
        PrimaryKey pk = new PrimaryKey(null, "pk_for_my_table", Arrays.asList("id")); 
        Collection<ForeignKey> fks = fkeys(fk("fk_one", "lc", "tt", "tc"), fk("fk_two", "lc", "tt", "tc"));
        Collection<Index> indexes = indexes("index_one col1 col2", "index_two col3 col4");
        
        Table table = new Table(null, "my_table", columns, pk, fks, indexes);
        
        NameValidator nameValidator = new NameValidator();
        nameValidator.setPattern(Pattern.compile("match_me_if_you_can"));
        List<DbValidator<? extends DbObject>> validators = new ArrayList<DbValidator<? extends DbObject>>();
        validators.add(nameValidator);
        table.setValidators(validators);
        
        transformer.output(table);
        
        BufferedReader reader = new BufferedReader(new StringReader(writer.toString()));
        dumpOutput();
        assertHasPreamble(reader);
        assertEquals("<table name=\"my_table\">", reader.readLine());
        assertEquals("  <columns>", reader.readLine());
        skipUntilEnd("       {column}", reader);
        skipUntilEnd("       {column}", reader);
        assertEquals("  </columns>", reader.readLine());
        skipUntilEnd("  {primarykey}", reader);
        assertEquals("  <foreignkeys>", reader.readLine());
        skipUntilEnd("       {foreignkey}", reader);
        skipUntilEnd("       {foreignkey}", reader);
        assertEquals("  </foreignkeys>", reader.readLine());
        assertEquals("  <indexes>", reader.readLine());
        skipUntilEnd("       {index}", reader);
        skipUntilEnd("       {index}", reader);
        assertEquals("  </indexes>", reader.readLine());
        assertEquals("  <validators>", reader.readLine());
        assertEquals("    <validator class=\"org.alfresco.util.schemacomp.validator.NameValidator\">", reader.readLine());        
        assertEquals("      <properties>", reader.readLine());        
        assertEquals("        <property name=\"pattern\">match_me_if_you_can</property>", reader.readLine());        
        assertEquals("      </properties>", reader.readLine());        
        assertEquals("    </validator>", reader.readLine());        
        assertEquals("  </validators>", reader.readLine());
        assertEquals("</table>", reader.readLine());
    }
    
    
    /**
     * Ignore lines that are tested elsewhere, e.g. ignore serialized Column objects
     * in the context of a Table since we only need to know that there was text for a
     * Column object in the right place - the actual Column text being tested in its own test.
     * <p>
     * Leading and trailing spaces are ignored in the comparison.
     * 
     * @param textToFind
     * @param reader
     */
    private void skipUntilEnd(String textToFind, BufferedReader reader, boolean emptyTag)
    {
        // To aid test code clarity, and distinguish between text we're actually
        // testing for and text that needs to be ignored...
        // {mytag} becomes </mytag>
        // or if an empty tag is expected
        // {mytag} becomes <mytag .../>
        if (emptyTag)
        {
            textToFind = textToFind.trim().
                                replace("{", "<").
                                replace("}", "\\s+.*/>");            
        }
        else
        {
            textToFind = textToFind.trim().
                                replace("{", "</").
                                replace("}", ">");
        }
        try
        {
            String line;
            while ((line = reader.readLine()) != null)
            {
                if (line.trim().matches(textToFind))
                {
                    return;
                }
            }
            fail("Unable to find text: " + textToFind);
        }
        catch (IOException error)
        {
            throw new RuntimeException("Unable to skip text whilst looking for: " + textToFind, error);
        }
            
    }

    private void skipUntilEnd(String textToFind, BufferedReader reader)
    {
        skipUntilEnd(textToFind, reader, false);
    }

    private void assertHasPreamble(BufferedReader reader) throws IOException
    {
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", reader.readLine());
    }

    private void dumpOutput()
    {
        if (outputDumpEnabled)
        {
            System.out.println(writer.toString());
        }
    }
}
