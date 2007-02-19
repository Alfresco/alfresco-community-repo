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
 * http://www.alfresco.com/legal/licensing" */

package org.alfresco.repo.avm;

import java.io.Serializable;

/**
 * An entry in a directory. Contains a name, parent, and child.
 * @author britt
 */
public class ChildEntryImpl implements ChildEntry, Serializable
{
    private static final long serialVersionUID = -307752114272916930L;

    /**
     * The key.
     */
    private ChildKey fKey;
    
    /**
     * The child.
     */
    private AVMNode fChild;
    
    /**
     * Default constructor for Hibernate.
     */
    protected ChildEntryImpl()
    {
    }

    /**
     * Make up a brand new entry.
     * @param key The ChildKey.
     * @param child The child.
     */
    public ChildEntryImpl(ChildKey key,
                          AVMNode child)
    {
        fKey = key;
        fChild = child;
    }

    /**
     * Set the key for this ChildEntry.
     * @param key The ChildKey.
     */
    public void setKey(ChildKey key)
    {
        fKey = key;
    }
    
    /**
     * Get the ChildKey for this ChildEntry.
     * @return
     */
    public ChildKey getKey()
    {
        return fKey;
    }
    
    /**
     * Set the child in this entry.
     * @param child
     */
    public void setChild(AVMNode child)
    {
        fChild = child;
    }

    /**
     * Get the child in this entry.
     * @return The child.
     */
    public AVMNode getChild()
    {
        return fChild;
    }

    /**
     * Equals override.
     * @param obj
     * @return Equality.
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!(obj instanceof ChildEntry))
        {
            return false;
        }
        ChildEntry other = (ChildEntry)obj;
        return fKey.equals(other.getKey());
    }

    /**
     * Get the hash code.
     * @return The hash code.
     */
    @Override
    public int hashCode()
    {
        return fKey.hashCode();
    }
}
