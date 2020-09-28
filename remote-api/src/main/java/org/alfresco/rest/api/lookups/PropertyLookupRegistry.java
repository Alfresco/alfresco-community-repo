/*-
 * #%L
 * Alfresco Remote API
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

package org.alfresco.rest.api.lookups;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Acts as a central point for property lookups
 *
 * @author Gethin James
 */
public class PropertyLookupRegistry
{
    private static Log logger = LogFactory.getLog(PropertyLookupRegistry.class);
    Map<String, PropertyLookup> propertyLookups = new HashMap<>();

    /**
     * Set the supported property lookup classes.
     * @param lookups
     */
    public void setLookups(List<PropertyLookup> lookups)
    {
        lookups.forEach(entry ->
        {
            entry.supports().forEach( propKey -> propertyLookups.put((String) propKey, entry) );
        });
    }

    /**
     * The list of property keys that are supported by this class
     * @return Set<String> property keys
     */
    public Set<String> supports()
    {
        return propertyLookups.keySet();
    }

    /**
     * Looks up the property value using a PropertyLookup
     * @param propertyName the property name/type
     * @param propertyValue the value to lookup
     * @return Object to be serialized as json
     */
    public Object lookup(String propertyName, String propertyValue)
    {
        PropertyLookup lookup = propertyLookups.get(propertyName);
        if (lookup != null)
        {
            return lookup.lookup(propertyValue);
        }
        return null;
    }
}
