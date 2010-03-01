/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.jcr.item;

import java.util.List;

import javax.jcr.Property;
import javax.jcr.PropertyIterator;

import org.alfresco.jcr.util.AbstractRangeIterator;


/**
 * Alfresco implementation of a Property Iterator
 * 
 * @author David Caruana
 */
public class PropertyListIterator extends AbstractRangeIterator
    implements PropertyIterator
{
    private List<PropertyImpl> properties;
    
    
    /**
     * Construct
     * 
     * @param context  session context
     * @param properties  property list
     */
    public PropertyListIterator(List<PropertyImpl> properties)
    {
        this.properties = properties;
    }
    
    /* (non-Javadoc)
     * @see javax.jcr.PropertyIterator#nextProperty()
     */
    public Property nextProperty()
    {
        long position = skip();
        PropertyImpl propertyImpl = properties.get((int)position);
        return propertyImpl.getProxy();
    }

    
    /* (non-Javadoc)
     * @see javax.jcr.RangeIterator#getSize()
     */
    public long getSize()
    {
        return properties.size();
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#next()
     */
    public Object next()
    {
        return nextProperty();
    }

}
