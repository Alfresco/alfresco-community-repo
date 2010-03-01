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

import java.util.List;

import org.alfresco.repo.domain.hibernate.DirtySessionAnnotation;

/**
 * Interface for MapEntry persistence.
 * @author britt
 */
public interface MapEntryDAO
{
    /**
     * Save a MapEntry.
     * @param entry To save.
     */
    @DirtySessionAnnotation(markDirty=true)
    public void save(MapEntry entry);

    /**
     * Delete a MapEntry.
     * @param entry
     */
    @DirtySessionAnnotation(markDirty=true)
    public void delete(MapEntry entry);

    /**
     * Delete all entries for a map.
     * @param mapAttr The map to purge.
     */
    @DirtySessionAnnotation(markDirty=true)
    public void delete(MapAttribute mapAttr);

    /**
     * Get an entry by name.
     * @param key The key of the entry.
     * @return A MapEntry or null.
     */
    @DirtySessionAnnotation(markDirty=false)
    public MapEntry get(MapEntryKey key);

    /**
     * Retrieve all the entries in a map.
     * @param mapAttr
     * @return A List of all entries in the given map.
     */
    @DirtySessionAnnotation(markDirty=false)
    public List<MapEntry> get(MapAttribute mapAttr);

    /**
     * Get the number of entries in a MapAttribute.
     * @param mapAttr The MapAttribute/
     * @return The number of entries.
     */
    @DirtySessionAnnotation(markDirty=false)
    public int size(MapAttribute mapAttr);

    /**
     * Evict an entry.
     * @param entry
     */
    @DirtySessionAnnotation(markDirty=false)
    public void evict(MapEntry entry);
}
