/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

package org.alfresco.repo.attributes;

/**
 * Implementation of ListEntry
 * @author britt
 */
public class ListEntryImpl implements ListEntry
{
    private static final long serialVersionUID = 1573391734169157835L;

    private ListEntryKey fKey;
    
    private Attribute fAttribute;

    /**
     * Default constructor.
     */
    public ListEntryImpl()
    {
    }
    
    public ListEntryImpl(ListEntryKey key, Attribute attr)
    {
        fKey = key;
        fAttribute = attr;
    }
    
    public void setKey(ListEntryKey key)
    {
        fKey = key;
    }
    
    public ListEntryKey getKey()
    {
        return fKey;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.ListEntry#getAttribute()
     */
    public Attribute getAttribute()
    {
        return fAttribute;
    }
    
    public void setAttribute(Attribute attr)
    {
        fAttribute = attr;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!(obj instanceof ListEntry))
        {
            return false;
        }
        return fKey.equals(((ListEntry)obj).getKey());
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return fKey.hashCode();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("[ListEntry:");
        builder.append(fKey.toString());
        builder.append(':');
        builder.append(fAttribute.toString());
        builder.append(']');
        return builder.toString();
    }
}
