/*
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
package org.alfresco.rest.framework.jacksonextensions;

import java.util.Set;

/**
 * Basic bean filtering.
 * 
 * It's based on Jackson's BeanPropertyFilter but uses slightly different method signatures
 *
 * @author Gethin James
 */
public class BeanPropertiesFilter
{
    private final Set<String> filteredProperties;
    public static final BeanPropertiesFilter ALLOW_ALL = new AllProperties();
    
    public BeanPropertiesFilter(Set<String> properties) {
        filteredProperties = properties;
 //       properties.add("id"); //always need id
    }
    
    /**
     * Indicates if the given property name is permitted to be used ie. is not filtered out
     * @param propertyName - bean property name
     * @return true - if the property is allowed to be used.
     */
    public boolean isAllowed(String propertyName)
    {
      return filteredProperties.contains(propertyName);  
    }
    
    /*
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("BeanPropertiesFilter [filteredProperties=").append(this.filteredProperties).append("]");
        return builder.toString();
    }
    
    /**
     * Default All properties filter
     *
     * @author Gethin James
     */
    public static class AllProperties extends BeanPropertiesFilter
    {

        public AllProperties()
        {
            super(null);
        }

        /*
         * @see org.alfresco.rest.framework.jacksonextensions.BeanPropertiesFilter#isAllowed(java.lang.String)
         */
        @Override
        public boolean isAllowed(String propertyName)
        {
            return true;
        }
        
        
    }
                
}
