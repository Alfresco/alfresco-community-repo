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

import java.util.Collection;

import org.alfresco.util.schemacomp.model.Column;
import org.alfresco.util.schemacomp.model.DbObject;
import org.alfresco.util.schemacomp.model.ForeignKey;
import org.alfresco.util.schemacomp.model.Index;
import org.alfresco.util.schemacomp.model.PrimaryKey;
import org.alfresco.util.schemacomp.model.Schema;
import org.alfresco.util.schemacomp.model.Table;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Converts DbObject instances into an XML output stream.
 * 
 * @author Matt Ward
 */
public class DbObjectXMLTransformer
{
    private static final Attributes EMPTY_ATTRIBUTES = new AttributesImpl();
    private ContentHandler xmlOut;
    
    
    public DbObjectXMLTransformer(ContentHandler contentHandler)
    {
        this.xmlOut = contentHandler;
    }
    
    public void output(DbObject dbObject)
    {
        try
        {
            attemptOutput(dbObject);
        }
        catch (SAXException e)
        {
            throw new RuntimeException("Unable to output " + dbObject, e);
        }
    }
    
    private void attemptOutput(DbObject dbObject) throws SAXException
    {
        // All DbObjects result in an XML element with the DbObject's class name as the tag
        // and a name attribute corresponding to the value of getName(),
        // e.g. For an instance of a Column: <column name="the_column_name"/>
        String tagName = dbObject.getClass().getSimpleName().toLowerCase();   
        final AttributesImpl attribs = new AttributesImpl();
        attribs.addAttribute("", "", "name", "CDATA", dbObject.getName());
        // Add class-specific attributes.
        addAttributes(dbObject, attribs);
        xmlOut.startElement("", "", tagName, attribs);
        
        
        // The element's contents can optionally be populated with class-specific content.
        transformDbObject(dbObject);
        
        // Provide the end tag, or close an empty element.
        xmlOut.endElement("", "", tagName);
    }
    
    /**
     * Add class-specific attributes.
     * 
     * @param dbObject
     * @param attribs
     */
    private void addAttributes(DbObject dbObject, AttributesImpl attribs)
    {
        if (dbObject instanceof Index)
        {
            Index index = (Index) dbObject;
            attribs.addAttribute("", "", "unique", "CDATA", Boolean.toString(index.isUnique()));
        }
    }

    private void transformDbObject(DbObject dbObject) throws SAXException
    {
        if (dbObject instanceof Schema)
        {
            transformSchema((Schema) dbObject);
        }
        else if (dbObject instanceof Table)
        {
            transformTable((Table) dbObject);
        }
        else if (dbObject instanceof Column)
        {
            transformColumn((Column) dbObject);
        }
        else if (dbObject instanceof ForeignKey)
        {
            transformForeignKey((ForeignKey) dbObject);
        }
        else if (dbObject instanceof Index)
        {
            transformIndex((Index) dbObject);
        }
        else if (dbObject instanceof PrimaryKey)
        {
            transformPrimaryKey((PrimaryKey) dbObject);
        }
    }
    
    

    private void transformSchema(Schema schema) throws SAXException
    {
        simpleStartTag("objects");
        for (DbObject dbo : schema)
        {
            output(dbo);
        }
        simpleEndTag("objects");
    }
    
    private void transformTable(Table table) throws SAXException
    {
        // Output columns
        simpleStartTag("columns");
        for (Column column : table.getColumns())
        {
            output(column);
        }
        simpleEndTag("columns");
        
        // Output primary key
        output(table.getPrimaryKey());
        
        // Output foreign keys
        simpleStartTag("foreignkeys");
        for (ForeignKey fk : table.getForeignKeys())
        {
            output(fk);
        }
        simpleEndTag("foreignkeys");

        // Output indexes
        simpleStartTag("indexes");
        for (Index index : table.getIndexes())
        {
            output(index);
        }
        simpleEndTag("indexes");
    }
    
    private void transformColumn(Column column) throws SAXException
    {
        simpleElement("type", column.getType());
        simpleElement("nullable", Boolean.toString(column.isNullable()));
    }

    private void transformForeignKey(ForeignKey fk) throws SAXException
    {
        simpleElement("localcolumn", fk.getLocalColumn());
        simpleElement("targettable", fk.getTargetTable());
        simpleElement("targetcolumn", fk.getTargetColumn()); 
    }

    private void transformIndex(Index index) throws SAXException
    {
        columnNameList(index.getColumnNames());
    }
    
    private void transformPrimaryKey(PrimaryKey pk) throws SAXException
    {
        columnNameList(pk.getColumnNames());
    }
    
    
    /**
     * Create a simple element of the form:
     * <pre>
     *    &lt;tag&gt;content&lt;/tag&gt;
     * </pre>
     * 
     * @param tag
     * @param content
     * @throws SAXException
     */
    private void simpleElement(String tag, String content) throws SAXException
    {
        simpleStartTag(tag);
        char[] chars = content.toCharArray();
        xmlOut.characters(chars, 0, chars.length);
        simpleEndTag(tag);
    }
    
    private void simpleStartTag(String tag) throws SAXException
    {
        xmlOut.startElement("", "", tag, DbObjectXMLTransformer.EMPTY_ATTRIBUTES);
    }
    
    private void simpleEndTag(String tag) throws SAXException
    {
        xmlOut.endElement("", "", tag);        
    }
    
    private void columnNameList(Collection<String> columnNames) throws SAXException
    {
        simpleStartTag("columnnames");
        for (String columnName : columnNames)
        {
            simpleElement("columnname", columnName);
        }
        simpleEndTag("columnnames");
    }
}
