/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.jcr.importer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.importer.view.ElementContext;
import org.alfresco.repo.importer.view.NodeContext;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.view.ImporterException;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO9075;
import org.alfresco.util.TempFileProvider;


/**
 * Maintains state about currently imported Property
 * 
 * @author David Caruana
 *
 */
public class PropertyContext extends ElementContext
{
    private NodeContext parentContext;
    private QName propertyName;
    private QName propertyType;
    
    private List<StringBuffer> values = new ArrayList<StringBuffer>();
    private Map<QName, FileWriter> contentWriters = new HashMap<QName, FileWriter>();
    
    
    /**
     * Construct
     * 
     * @param elementName
     * @param parentContext
     * @param propertyName
     * @param propertyType
     */
    public PropertyContext(QName elementName, NodeContext parentContext, QName propertyName, QName propertyType)
    {
        super(elementName, parentContext.getDictionaryService(), parentContext.getImporter());
        this.parentContext = parentContext;
        this.propertyName = propertyName;
        this.propertyType = propertyType;
    }

    /**
     * Get node containing property
     * 
     * @return  node
     */
    public NodeContext getNode()
    {
        return parentContext;
    }
    
    /**
     * Get property name
     * 
     * @return  property name
     */
    public QName getName()
    {
        return propertyName;
    }

    /**
     * Get property type
     * 
     * @return  property type
     */
    public QName getType()
    {
        return propertyType;
    }
    
    /**
     * Is property multi-valued?
     * 
     * @return  true => multi-valued; false => single value
     */
    public boolean isMultiValue()
    {
        return values.size() > 1;
    }
    
    /**
     * Is null property value
     * 
     * @return  true => value has not been provided
     */
    public boolean isNull()
    {
        return values.size() == 0;
    }
    
    /**
     * Get property values
     * 
     * @return  values
     */
    public List<StringBuffer> getValues()
    {
        return values;
    }
    
    /**
     * Start a new property value
     */
    public void startValue()
    {
        StringBuffer buffer = new StringBuffer(128);
        if (propertyType.equals(DataTypeDefinition.CONTENT))
        {            
            // create temporary file to hold content
            File tempFile = TempFileProvider.createTempFile("import", ".tmp");
            try
            {
                FileWriter tempWriter = new FileWriter(tempFile);
                contentWriters.put(propertyName, tempWriter);
                ContentData contentData = new ContentData(tempFile.getAbsolutePath(), MimetypeMap.MIMETYPE_BINARY, 0, tempWriter.getEncoding());
                buffer.append(contentData.toString());
            }
            catch(IOException e)
            {
                throw new ImporterException("Failed to create temporary content holder for property " + propertyName, e);
            }
        }
        values.add(buffer);
    }

    /**
     * End a property value
     */
    public void endValue()
    {
        if (propertyType.equals(DataTypeDefinition.CONTENT))
        {
            // close content writer
            FileWriter tempWriter = contentWriters.get(propertyName);
            try
            {
                tempWriter.close();
                contentWriters.remove(propertyName);
            }
            catch(IOException e)
            {
                throw new ImporterException("Failed to create temporary content holder for property " + propertyName, e);
            }
        }
        else
        {
            // decode value
            StringBuffer buffer = values.get(values.size() -1);
            values.set(values.size() -1, new StringBuffer(ISO9075.decode(buffer.toString())));
        }
    }
    
    /**
     * Append property value characters
     * 
     * @param ch
     * @param start
     * @param length
     */
    public void appendCharacters(char[] ch, int start, int length)
    {
        if (propertyType.equals(DataTypeDefinition.CONTENT))
        {
            FileWriter tempWriter = contentWriters.get(propertyName);
            try
            {
                tempWriter.write(ch, start, length);
            }
            catch(IOException e)
            {
                throw new ImporterException("Failed to write temporary content for property " + propertyName, e);
            }
        }
        else
        {
            StringBuffer buffer = values.get(values.size() -1);
            buffer.append(ch, start, length);
        }
    }
    
}
