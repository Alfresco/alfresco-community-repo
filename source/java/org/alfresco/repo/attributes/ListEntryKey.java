/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
 * http://www.alfresco.com/legal/licensing
 */

package org.alfresco.repo.attributes;

import java.io.Serializable;

/**
 * Key class for the ListEntry entity.
 * @author britt
 */
public class ListEntryKey implements Serializable
{
    private static final long serialVersionUID = 7314576560198411815L;

    private ListAttribute fList;
    
    private int fIndex;
    
    public ListEntryKey()
    {
    }
    
    public ListEntryKey(ListAttribute list, int index)
    {
        fList = list;
        fIndex = index;
    }

    /**
     * @return the Index
     */
    public int getIndex()
    {
        return fIndex;
    }

    /**
     * @param index the fIndex to set
     */
    public void setIndex(int index)
    {
        fIndex = index;
    }

    /**
     * @return the fList
     */
    public ListAttribute getList()
    {
        return fList;
    }

    /**
     * @param list the fList to set
     */
    public void setList(ListAttribute list)
    {
        fList = list;
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
        if (!(obj instanceof ListEntryKey))
        {
            return false;
        }
        ListEntryKey other = (ListEntryKey)obj;
        return fIndex == other.getIndex() &&
               fList.equals(other.getList());
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return fIndex + fList.hashCode();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "[ListEntryKey:" + fIndex + ']';
    }
}
