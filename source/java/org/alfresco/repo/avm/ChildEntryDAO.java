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

import java.util.List;

/**
 * DAO for ChildEntries.
 * @author britt
 */
public interface ChildEntryDAO
{
    /**
     * Save an unsaved ChildEntry.
     * @param entry The entry to save.
     */
    public void save(ChildEntry entry);
    
    /**
     * Get an entry by name and parent.
     * @param name The name of the child to find.
     * @param parent The parent to look in.
     * @return The ChildEntry or null if not foun.
     */
    public ChildEntry get(ChildKey key);
    
    /**
     * Get all the children of a given parent.
     * @param parent The parent.
     * @return A List of ChildEntries.
     */
    public List<ChildEntry> getByParent(DirectoryNode parent);
    
    /**
     * Get the entry for a given child in a given parent.
     * @param parent The parent.
     * @param child The child.
     * @return The ChildEntry or null.
     */
    public ChildEntry getByParentChild(DirectoryNode parent, AVMNode child);
    
    /**
     * Get all the ChildEntries corresponding to the given child.
     * @param child The child for which to look up entries.
     * @return The matching entries.
     */
    public List<ChildEntry> getByChild(AVMNode child);
    
    /**
     * Update a dirty ChildEntry.
     * @param child The dirty entry.
     */
    public void update(ChildEntry child);
    
    /**
     * Delete one.
     * @param child The one to delete.
     */
    public void delete(ChildEntry child);
    
    /**
     * Delete all children of the given parent.
     * @param parent The parent.
     */
    public void deleteByParent(AVMNode parent);
}
