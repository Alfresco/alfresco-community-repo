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
