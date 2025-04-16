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
package org.alfresco.repo.management.subsystems;

import java.util.Collection;

/**
 * An event emitted before a {@link PropertyBackedBean} removes properties.
 * 
 * @author Alan Davis
 */
public class PropertyBackedBeanRemovePropertiesEvent extends PropertyBackedBeanEvent
{
    private static final long serialVersionUID = 7076784618042401540L;

    private Collection<String> properties;

    /**
     * The Constructor.
     * 
     * @param source
     *            the source of the event
     */
    public PropertyBackedBeanRemovePropertiesEvent(PropertyBackedBean source, Collection<String> properties)
    {
        super(source);
        this.properties = properties;
    }

    public Collection<String> getProperties()
    {
        return properties;
    }
}
