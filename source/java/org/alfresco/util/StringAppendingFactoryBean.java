/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.util;

import org.springframework.beans.factory.FactoryBean;

/**
 * A simple factory for glueing together multiple arguments as a string
 * 
 * @author dward
 */
public class StringAppendingFactoryBean implements FactoryBean
{

    /** The items. */
    private Object[] items;

    /**
     * Sets the items to be appended together.
     * 
     * @param items
     *            the items
     */
    public void setItems(Object[] items)
    {
        this.items = items;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.beans.factory.FactoryBean#getObject()
     */
    public Object getObject() throws Exception
    {
        if (this.items == null)
        {
            return "";
        }
        StringBuilder buff = new StringBuilder(1024);
        for (Object item : this.items)
        {
            buff.append(item);
        }
        return buff.toString();
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.beans.factory.FactoryBean#getObjectType()
     */
    public Class<?> getObjectType()
    {
        return String.class;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.beans.factory.FactoryBean#isSingleton()
     */
    public boolean isSingleton()
    {
        return true;
    }

}
