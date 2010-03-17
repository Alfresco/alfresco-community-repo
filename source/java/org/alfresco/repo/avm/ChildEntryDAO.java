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
     * @param childNamePattern - achild name pattern to match - null is supported for match all
     * @return A List of ChildEntries.
     */
    public List<ChildEntry> getByParent(DirectoryNode parent, String childNamePattern);

    /**
     * Does the entry exist for a given child in a given parent.
     * @param parent The parent.
     * @param child The child.
     * @return True if it exists
     */
    public boolean existsParentChild(DirectoryNode parent, AVMNode child);

    /**
     * Get all the ChildEntries corresponding to the given child.
     * @param child The child for which to look up entries.
     * @return The matching entries.
     */
    public List<ChildEntry> getByChild(AVMNode child);

    /**
     * Rename a child entry (specific rename 'case' only)
     * @param child The one to rename.
     */
    public void rename(ChildKey key, String newName);

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

    /**
     * Evict a child entry.
     * @param entry
     * 
     * @deprecated
     */
    public void evict(ChildEntry entry);
}
