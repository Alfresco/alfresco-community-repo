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
 * Interface for persistence of the top level attribute map.
 * @author britt
 */
public interface GlobalAttributeEntryDAO
{
    /**
     * Save an entry.
     * @param entry To save.
     */
    @DirtySessionAnnotation(markDirty=true)
    public void save(GlobalAttributeEntry entry);
    
    /**
     * Delete an entry.
     * @param entry To delete.
     */
    @DirtySessionAnnotation(markDirty=true)
    public void delete(GlobalAttributeEntry entry);
    
    /**
     * Delete an entry by name.
     * @param name The name of the entry.
     */
    @DirtySessionAnnotation(markDirty=true)
    public void delete(String name);
    
    /**
     * Get an attribute by name.
     * @param name The name of the attribute.
     * @return The entry or null.
     */
    @DirtySessionAnnotation(markDirty=false)
    public GlobalAttributeEntry get(String name);
    
    /**
     * Get all keys for global attributes.
     * @return A list of all top level keys.
     */
    @DirtySessionAnnotation(markDirty=false)
    public List<String> getKeys();
}
