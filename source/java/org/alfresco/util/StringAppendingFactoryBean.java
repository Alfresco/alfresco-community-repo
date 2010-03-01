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
