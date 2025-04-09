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
     *            QName
     * @param context
     *            ElementContext
     */
    public MetaDataContext(QName elementName, ElementContext context)
    {
        super(elementName, context.getDictionaryService(), context.getImporter());
    }

    /**
     * Set meta-data property
     * 
     * @param property
     *            property name
     * @param value
     *            property value
     */
    public void setProperty(QName property, String value)
    {
        properties.put(property, value);
    }

    /**
     * Get meta-data property
     * 
     * @param property
     *            property name
     * @return property value
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
     * 
     * @see java.lang.Object#toString() */
    @Override
    public String toString()
    {
        return "MetaDataContext[properties=" + properties.size() + "]";
    }

}
