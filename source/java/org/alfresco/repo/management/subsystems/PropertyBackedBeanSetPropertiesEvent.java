/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.repo.management.subsystems;

import java.util.Map;


/**
 * An event emitted before a {@link PropertyBackedBean} updates its properties.
 * 
 * @author Alan Davis
 */
public class PropertyBackedBeanSetPropertiesEvent extends PropertyBackedBeanEvent
{
    private static final long serialVersionUID = 7530572539759535003L;
    
    private Map<String, String> properties;

    /**
     * The Constructor.
     * 
     * @param source
     *            the source of the event
     */
    public PropertyBackedBeanSetPropertiesEvent(PropertyBackedBean source, Map<String, String> properties)
    {
        super(source);
        this.properties = properties;
    }

    public Map<String, String> getProperties()
    {
        return properties;
    }    
}
