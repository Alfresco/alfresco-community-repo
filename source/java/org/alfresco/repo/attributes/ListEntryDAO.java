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

import java.util.List;

import org.alfresco.repo.domain.hibernate.DirtySessionAnnotation;

/**
 * DAO interface for ListEntries.
 * @author britt
 */
public interface ListEntryDAO
{
    /**
     * Save a new Entry.
     * @param entry
     */
    @DirtySessionAnnotation(markDirty=true)
    public void save(ListEntry entry);
    
    /**
     * Get the entry for the give list and index.
     * @param list The ListAttribute.
     * @param index The index.
     * @return The ListEntry.
     */
    @DirtySessionAnnotation(markDirty=false)
    public ListEntry get(ListEntryKey key);
    
    /**
     * Get all entries for a given list.
     * @param list The ListAttribute.
     * @return The entries.
     */
    @DirtySessionAnnotation(markDirty=false)
    public List<ListEntry> get(ListAttribute list);
    
    /**
     * Delete a list entry.
     * @param entry
     */
    @DirtySessionAnnotation(markDirty=true)
    public void delete(ListEntry entry);
    
    /**
     * Delete all entries from a list.
     * @param list
     */
    @DirtySessionAnnotation(markDirty=true)
    public void delete(ListAttribute list);
    
    /**
     * Get the size of the entries.
     * @param list The list.
     * @return The count of entries.
     */
    @DirtySessionAnnotation(markDirty=false)
    public int size(ListAttribute list);
}
