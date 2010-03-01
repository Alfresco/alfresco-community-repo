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
import org.alfresco.service.cmr.attributes.AttrQuery;
import org.springframework.extensions.surf.util.Pair;

/**
 * Interface for persistence operations on attributes.
 * @author britt
 */
public interface AttributeDAO
{
    /**
     * Save an attribute (recursively).
     * @param attr The attribute to save.
     */
    @DirtySessionAnnotation(markDirty=true)
    public void save(Attribute attr);

    /**
     * Delete an attribute (recursively).
     * @param attr The attribute to delete.
     */
    @DirtySessionAnnotation(markDirty=true)
    public void delete(Attribute attr);

    /**
     * Find all attributes that match a given path and AttrQuery.
     * @param map The map within which to query.
     * @param query The AttrQuery.
     * @return A List of key, attribute value pairs.
     */
    @DirtySessionAnnotation(markDirty=false)
    public List<Pair<String,Attribute>> find(MapAttribute map, AttrQuery query);

    /**
     * Delete entries from a map that match the given query.
     * @param map
     * @param query
     */
    @DirtySessionAnnotation(markDirty=true)
    public void delete(MapAttribute map, AttrQuery query);

    /**
     * Evict an Attribute from the session cache.
     * @param attr
     */
    @DirtySessionAnnotation(markDirty=false)
    public void evict(Attribute attr);

    /**
     * Evict an Attribute non-recursively.
     * @param attr
     */
    @DirtySessionAnnotation(markDirty=false)
    public void evictFlat(Attribute attr);

    /**
     * Force a flush.
     */
    @DirtySessionAnnotation(markDirty=false)
    public void flush();
}
