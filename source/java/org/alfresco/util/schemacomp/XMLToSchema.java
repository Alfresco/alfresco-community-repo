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

import java.io.IOException;
import java.io.InputStream;
import java.util.Stack;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.alfresco.util.schemacomp.model.Column;
import org.alfresco.util.schemacomp.model.DbObject;
import org.alfresco.util.schemacomp.model.ForeignKey;
import org.alfresco.util.schemacomp.model.Index;
import org.alfresco.util.schemacomp.model.PrimaryKey;
import org.alfresco.util.schemacomp.model.Schema;
import org.alfresco.util.schemacomp.model.Sequence;
import org.alfresco.util.schemacomp.model.Table;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Parse an XML document representing a database schema.
 * 
 * @author Matt Ward
 */
public class XMLToSchema extends DefaultHandler
{
    private SAXParser parser;
    private InputStream in;
    private Schema schema;
    private Stack<DbObject> dboStack = new Stack<DbObject>();
    private String lastTag;
    private String lastText;
    
    
    public XMLToSchema(InputStream in, SAXParserFactory saxParserFactory)
    {
        this.in = in;
        try
        {
            parser = saxParserFactory.newSAXParser();
        }
        catch (Throwable e)
        {
            throw new RuntimeException("Cannot create parser.", e);
        }
    }
    
    public XMLToSchema(InputStream in)
    {
        this(in, SAXParserFactory.newInstance());
    }
    
    public void parse()
    {
        try
        {
            parser.parse(in, this);
        }
        catch (SAXException error)
        {
            throw new RuntimeException("Unable to parse input stream.", error);
        }
        catch (IOException error)
        {
            throw new RuntimeException("Unable to parse input stream.", error);
        }
    }
    
    public Schema getSchema()
    {
        return this.schema;
    }


    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        if (qName.equals(XML.EL_TABLE))
        {
            Table table = (Table) dboStack.pop();
            schema.add(table);
        }
        else if (qName.equals(XML.EL_COLUMN))
        {
            Column column = (Column) dboStack.pop();
            Table table = (Table) dboStack.peek();
            table.getColumns().add(column);
        }
        else if (qName.equals(XML.EL_PRIMARY_KEY))
        {
            PrimaryKey pk = (PrimaryKey) dboStack.pop();
            Table table = (Table) dboStack.peek();
            table.setPrimaryKey(pk);
        }
        else if (qName.equals(XML.EL_FOREIGN_KEY))
        {
            ForeignKey fk = (ForeignKey) dboStack.pop();
            Table table = (Table) dboStack.peek();
            table.getForeignKeys().add(fk);
        }
        else if (qName.equals(XML.EL_INDEX))
        {
            Index index = (Index) dboStack.pop();
            Table table = (Table) dboStack.peek();
            table.getIndexes().add(index);
        }
        else if (qName.equals(XML.EL_SEQUENCE))
        {
            Sequence seq = (Sequence) dboStack.pop();
            schema.add(seq);
        }
    }


    
    
    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts)
                throws SAXException
    {
        lastTag = qName;
        
        if (qName.equals(XML.EL_SCHEMA))
        {
            schema = new Schema(atts.getValue(XML.ATTR_NAME));
        }
        else if (qName.equals(XML.EL_TABLE))
        {
            dboStack.push(new Table(atts.getValue(XML.ATTR_NAME)));
        }
        else if (qName.equals(XML.EL_COLUMN))
        {
            dboStack.push(new Column(atts.getValue(XML.ATTR_NAME)));
        }
        else if (qName.equals(XML.EL_PRIMARY_KEY))
        {
            dboStack.push(new PrimaryKey(atts.getValue(XML.ATTR_NAME)));
        }
        else if (qName.equals(XML.EL_FOREIGN_KEY))
        {
            dboStack.push(new ForeignKey(atts.getValue(XML.ATTR_NAME)));
        }
        else if (qName.equals(XML.EL_INDEX))
        {
            Index index = new Index(atts.getValue(XML.ATTR_NAME));
            boolean unique = Boolean.parseBoolean(atts.getValue(XML.ATTR_UNIQUE));
            index.setUnique(unique);
            dboStack.push(index);
        }
        else if (qName.equals(XML.EL_SEQUENCE))
        {
            dboStack.push(new Sequence(atts.getValue(XML.ATTR_NAME)));
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException
    {
        lastText = new String(ch, start, length).trim();            
        
        if (lastText.length() != 0)
        {
            if (lastTag.equals(XML.EL_TYPE))
            {
                Column column = (Column) dboStack.peek();
                column.setType(lastText);
            }
            else if (lastTag.equals(XML.EL_NULLABLE))
            {
                Column column = (Column) dboStack.peek();
                column.setNullable(Boolean.parseBoolean(lastText));
            }
            else if (lastTag.equals(XML.EL_COLUMN_NAME))
            {
                if (dboStack.peek() instanceof PrimaryKey)
                {
                    PrimaryKey pk = (PrimaryKey) dboStack.peek();
                    pk.getColumnNames().add(lastText);
                }
                else if (dboStack.peek() instanceof Index)
                {
                    Index index = (Index) dboStack.peek();
                    index.getColumnNames().add(lastText);
                }
            }
            else if (lastTag.equals(XML.EL_LOCAL_COLUMN))
            {
                ForeignKey fk = (ForeignKey) dboStack.peek();
                fk.setLocalColumn(lastText);
            }
            else if (lastTag.equals(XML.EL_TARGET_TABLE))
            {
                ForeignKey fk = (ForeignKey) dboStack.peek();
                fk.setTargetTable(lastText);
            }
            else if (lastTag.equals(XML.EL_TARGET_COLUMN))
            {
                ForeignKey fk = (ForeignKey) dboStack.peek();
                fk.setTargetColumn(lastText);
            }
        }
    }
}
