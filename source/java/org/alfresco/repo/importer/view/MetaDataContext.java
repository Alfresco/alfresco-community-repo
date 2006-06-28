/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
