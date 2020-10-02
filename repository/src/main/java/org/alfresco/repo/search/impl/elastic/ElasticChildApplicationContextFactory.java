/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
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
package org.alfresco.repo.search.impl.elastic;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.alfresco.repo.management.subsystems.ChildApplicationContextFactory;

/**
 * Context Factory for Elasticsearch Search subsystem.
 * 
 * This class includes some properties (like "Id for last Node in index" or "Approx. nodes remaining")
 * that are NOT updateable but can be used for Administering purposes.
 *
 */
public class ElasticChildApplicationContextFactory extends ChildApplicationContextFactory
{
    
    /**
     * List of administering properties
     */
    // TBD Define the set of properties to be obtained from Elastic Server
    private static List<String> ADM_PROPERTY_NAMES = List.of("");
    
    /**
     * Check updateable status for a property name
     * @param name property name
     * @return true if the name of the property is not 
     */
    @Override
    public boolean isUpdateable(String name)
    {
        return super.isUpdateable(name) && !ADM_PROPERTY_NAMES.contains(name); 
    }
    
    /**
     * Sets the value of a property
     * @param name property name
     * @param value property value
     */
    @Override
    public void setProperty(String name, String value)
    {
        if (!isUpdateable(name))
        {
            throw new IllegalStateException("Illegal write to property \"" + name + "\"");
        }
        super.setProperty(name, value);
    }  
    
    /**
     * Gets the value of a property
     * @param name property name
     * @return value of the property
     */
    @Override
    public String getProperty(String name)
    {
        if (!isUpdateable(name))
        {
            // TODO The property value needs to be recovered from Elastic Server 
            return null;
        }
        else
        {
            return super.getProperty(name);
        }
    }
    
    /**
     * Gets all the property names, including administering properties
     * @return Set of property names
     */
    @Override
    public Set<String> getPropertyNames()
    {
        Set<String> result = new TreeSet<String>();
        result.addAll(ADM_PROPERTY_NAMES);
        result.addAll(super.getPropertyNames());
        return result;
    }


}
