/*
 * Copyright (C) 2005 Alfresco, Inc.
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
package org.alfresco.repo.importer.view;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.namespace.QName;


/**
 * Represents View Meta Data
 * 
 * @author David Caruana
 */
public class MetaDataContext extends ElementContext
{
    
    private Map<QName, String> properties = new HashMap<QName, String>();
    
    
    /**
     * Construct
     * 
     * @param elementName
     * @param dictionary
     * @param importer
     */
    public MetaDataContext(QName elementName, ElementContext context)
    {
        super(elementName, context.getDictionaryService(), context.getImporter());
    }
    
    
    /**
     * Set meta-data property
     * 
     * @param property  property name
     * @param value  property value
     */
    public void setProperty(QName property, String value)
    {
        properties.put(property, value);
    }
    
    
    /**
     * Get meta-data property
     * 
     * @param property  property name
     * @return  property value
     */
    public String getProperty(QName property)
    {
        return properties.get(property);
    }
    
    
    /**
     * Get all meta-data properties
     * 
     * @return all meta-data properties
     */
    public Map<QName, String> getProperties()
    {
        return properties;
    }
    

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "MetaDataContext[properties=" + properties.size() + "]";
    }
 
    
}
