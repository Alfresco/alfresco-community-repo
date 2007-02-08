/*
 * Copyright (C) 2006 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.alfresco.repo.avm;

/**
 * This interface represents an entry in a directory.
 * @author britt
 */
public interface ChildEntry
{
    /**
     * Set the key for this ChildEntry.
     * @param key The ChildKey.
     */
    public void setKey(ChildKey key);
    
    /**
     * Get the ChildKey for this ChildEntry.
     * @return
     */
    public ChildKey getKey();
    
    /**
     * Set the child in this entry.
     * @param child
     */
    public void setChild(AVMNode child);
    
    /**
     * Get the child in this entry.
     * @return The child.
     */
    public AVMNode getChild();
}
