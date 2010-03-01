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

package org.alfresco.repo.attributes;

/**
 * @author britt
 *
 */
public class GlobalAttributeEntryImpl implements GlobalAttributeEntry
{
    private String fName;
    
    private Attribute fAttribute;
    
    public GlobalAttributeEntryImpl()
    {
    }
    
    public GlobalAttributeEntryImpl(String name, Attribute attr)
    {
        fName = name;
        fAttribute = attr;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.GlobalAttributeEntry#getAttribute()
     */
    public Attribute getAttribute()
    {
        return fAttribute;
    }

    /**
     * Setter.
     */
    public void setAttribute(Attribute attr)
    {
        fAttribute = attr;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.GlobalAttributeEntry#getName()
     */
    public String getName()
    {
        return fName;
    }
    
    /**
     * Setter.
     */
    public void setName(String name)
    {
        fName = name;
    }
}
