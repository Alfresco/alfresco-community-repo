/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.util.schemacomp;

import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import org.alfresco.util.schemacomp.model.Column;
import org.alfresco.util.schemacomp.model.DbObject;
import org.alfresco.util.schemacomp.model.ForeignKey;
import org.alfresco.util.schemacomp.model.Index;
import org.alfresco.util.schemacomp.model.PrimaryKey;
import org.alfresco.util.schemacomp.model.Schema;
import org.alfresco.util.schemacomp.model.Table;
import org.alfresco.util.schemacomp.validator.DbValidator;

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
        if (dbObject instanceof Schema)
        {
            // XML Schema (XSD) declarations.
            attribs.addAttribute("", "", "xmlns", "CDATA", "http://www.alfresco.org/repo/db-schema");
            attribs.addAttribute("", "", "xmlns:xsi", "CDATA", "http://www.w3.org/2001/XMLSchema-instance");
            attribs.addAttribute("", "", "xsi:schemaLocation", "CDATA", "http://www.alfresco.org/repo/db-schema db-schema.xsd");
        }
        attribs.addAttribute("", "", XML.ATTR_NAME, "CDATA", dbObject.getName());
        // Add class-specific attributes (after common DbObject attributes).
        addAttributes(dbObject, attribs);
        String tagName = dbObject.getClass().getSimpleName().toLowerCase();
        xmlOut.startElement("", "", tagName, attribs);

        // All DbObjects potentially have validator configuration present in the XML.
        transformValidators(dbObject.getValidators());

        // The element's contents can optionally be populated with class-specific content.
        transformDbObject(dbObject);

        // Provide the end tag, or close an empty element.
        xmlOut.endElement("", "", tagName);
    }

    /**
     * @param validators
     *            List<DbValidator>
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
     *            DbObject
     * @param attribs
     *            AttributesImpl
     */
    private void addAttributes(DbObject dbObject, AttributesImpl attribs)
    {
        if (dbObject instanceof Schema)
        {
            Schema schema = (Schema) dbObject;
            attribs.addAttribute("", "", XML.ATTR_DB_PREFIX, "CDATA", schema.getDbPrefix());
            attribs.addAttribute("", "", XML.ATTR_VERSION, "CDATA", Integer.toString(schema.getVersion()));
            attribs.addAttribute("", "", XML.ATTR_TABLE_COLUMN_ORDER, "CDATA", Boolean.toString(schema.isCheckTableColumnOrder()));
        }
        else if (dbObject instanceof Index)
        {
            Index index = (Index) dbObject;
            attribs.addAttribute("", "", XML.ATTR_UNIQUE, "CDATA", Boolean.toString(index.isUnique()));
        }
        else if (dbObject instanceof Column)
        {
            Column column = (Column) dbObject;
            attribs.addAttribute("", "", XML.ATTR_ORDER, "CDATA", Integer.toString(column.getOrder()));
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
        if (table.hasPrimaryKey())
        {
            output(table.getPrimaryKey());
        }

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
        simpleElement(XML.EL_AUTOINCREMENT, Boolean.toString(column.isAutoIncrement()));
    }

    private void transformForeignKey(ForeignKey fk) throws SAXException
    {
        simpleElement(XML.EL_LOCAL_COLUMN, fk.getLocalColumn());
        simpleElement(XML.EL_TARGET_TABLE, fk.getTargetTable());
        simpleElement(XML.EL_TARGET_COLUMN, fk.getTargetColumn());
    }

    private void transformIndex(Index index) throws SAXException
    {
        columnNameList(index.getColumnNames(), null);
    }

    private void transformPrimaryKey(PrimaryKey pk) throws SAXException
    {
        columnNameList(pk.getColumnNames(), pk.getColumnOrders());
    }

    /**
     * Create a simple element of the form:
     * 
     * <pre>
     *    &lt;tag&gt;content&lt;/tag&gt;
     * </pre>
     * 
     * @param tag
     *            String
     * @param content
     *            String
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

    /**
     * Outputs a list of columnname elements sandwiched within a columnnames element.
     * <p>
     * The columnOrders parameter will provide a corresponding list of integers that will be provided in each columnname element's order attribute. This parameter may be null in which case order attributes will be ommitted.
     * 
     * @param columnNames
     *            List<String>
     * @param columnOrders
     *            List<Integer>
     * @throws SAXException
     */
    private void columnNameList(List<String> columnNames,
            List<Integer> columnOrders) throws SAXException
    {
        simpleStartTag(XML.EL_COLUMN_NAMES);
        for (int i = 0; i < columnNames.size(); i++)
        {
            String columnName = columnNames.get(i);

            final AttributesImpl attribs = new AttributesImpl();
            if (columnOrders != null)
            {
                int columnOrder = columnOrders.get(i);
                attribs.addAttribute("", "", XML.ATTR_ORDER, "CDATA", Integer.toString(columnOrder));
            }
            // Create a <columnname> or <columnname order="n"> start tag
            xmlOut.startElement("", "", XML.EL_COLUMN_NAME, attribs);

            // Provide the elements content
            char[] chars = columnName.toCharArray();
            xmlOut.characters(chars, 0, chars.length);

            // Provide the closing tag
            simpleEndTag(XML.EL_COLUMN_NAME);
        }
        simpleEndTag(XML.EL_COLUMN_NAMES);
    }
}
