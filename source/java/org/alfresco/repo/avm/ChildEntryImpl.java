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
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>. */

package org.alfresco.repo.avm;


/**
 * An entry in a directory. Contains a name, parent, and child.
 * @author britt
 */
public class ChildEntryImpl implements ChildEntry
{
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
    public ChildEntryImpl()
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
