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
import java.util.List;

import org.alfresco.util.schemacomp.model.Column;
import org.alfresco.util.schemacomp.model.DbObject;
import org.alfresco.util.schemacomp.model.ForeignKey;
import org.alfresco.util.schemacomp.model.Index;
import org.alfresco.util.schemacomp.model.PrimaryKey;
import org.alfresco.util.schemacomp.model.Schema;
import org.alfresco.util.schemacomp.model.Table;
import org.alfresco.util.schemacomp.validator.DbValidator;
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
        final AttributesImpl attribs = new AttributesImpl();
        attribs.addAttribute("", "", XML.ATTR_NAME, "CDATA", dbObject.getName());
        // Add class-specific attributes.
        addAttributes(dbObject, attribs);
        String tagName = dbObject.getClass().getSimpleName().toLowerCase();   
        xmlOut.startElement("", "", tagName, attribs);
        
        
        // The element's contents can optionally be populated with class-specific content.
        transformDbObject(dbObject);
        
        // All DbObjects potentially have validator configuration present in the XML.
        transformValidators(dbObject.getValidators());
        
        // Provide the end tag, or close an empty element.
        xmlOut.endElement("", "", tagName);
    }
    
    
    /**
     * @param validators
     * @throws SAXException 
     */
    private void transformValidators(List<DbValidator> validators) throws SAXException
    {
        if (validators.size() > 0)
        {
            simpleStartTag(XML.EL_VALIDATORS);
            for (DbValidator dbv : validators)
            {
                final AttributesImpl attribs = new AttributesImpl();
                attribs.addAttribute("", "", XML.ATTR_CLASS, "CDATA", dbv.getClass().getName());
                xmlOut.startElement("", "", XML.EL_VALIDATOR, attribs);
                
                if (dbv.getPropertyNames().size() > 0)
                {
                    simpleStartTag(XML.EL_PROPERTIES);
                    for (String propName : dbv.getPropertyNames())
                    {
                        final AttributesImpl propAttrs = new AttributesImpl();
                        propAttrs.addAttribute("", "", XML.ATTR_NAME, "CDATA", propName);
                        xmlOut.startElement("", "", XML.EL_PROPERTY, propAttrs);
                        String propValue = dbv.getProperty(propName);
                        char[] chars = propValue.toCharArray();
                        xmlOut.characters(chars, 0, chars.length);
                        simpleEndTag(XML.EL_PROPERTY);
                    }
                    simpleEndTag(XML.EL_PROPERTIES);
                }
                
                simpleEndTag(XML.EL_VALIDATOR);
            }
            simpleEndTag(XML.EL_VALIDATORS);
        }
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
            attribs.addAttribute("", "", XML.ATTR_UNIQUE, "CDATA", Boolean.toString(index.isUnique()));
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
        simpleStartTag(XML.EL_OBJECTS);
        for (DbObject dbo : schema)
        {
            output(dbo);
        }
        simpleEndTag(XML.EL_OBJECTS);
    }
    
    private void transformTable(Table table) throws SAXException
    {
        // Output columns
        simpleStartTag(XML.EL_COLUMNS);
        for (Column column : table.getColumns())
        {
            output(column);
        }
        simpleEndTag(XML.EL_COLUMNS);
        
        // Output primary key
        output(table.getPrimaryKey());
        
        // Output foreign keys
        simpleStartTag(XML.EL_FOREIGN_KEYS);
        for (ForeignKey fk : table.getForeignKeys())
        {
            output(fk);
        }
        simpleEndTag(XML.EL_FOREIGN_KEYS);

        // Output indexes
        simpleStartTag(XML.EL_INDEXES);
        for (Index index : table.getIndexes())
        {
            output(index);
        }
        simpleEndTag(XML.EL_INDEXES);
    }
    
    private void transformColumn(Column column) throws SAXException
    {
        simpleElement(XML.EL_TYPE, column.getType());
        simpleElement(XML.EL_NULLABLE, Boolean.toString(column.isNullable()));
    }

    private void transformForeignKey(ForeignKey fk) throws SAXException
    {
        simpleElement(XML.EL_LOCAL_COLUMN, fk.getLocalColumn());
        simpleElement(XML.EL_TARGET_TABLE, fk.getTargetTable());
        simpleElement(XML.EL_TARGET_COLUMN, fk.getTargetColumn()); 
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
        simpleStartTag(XML.EL_COLUMN_NAMES);
        for (String columnName : columnNames)
        {
            simpleElement(XML.EL_COLUMN_NAME, columnName);
        }
        simpleEndTag(XML.EL_COLUMN_NAMES);
    }
}
