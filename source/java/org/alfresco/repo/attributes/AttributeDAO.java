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

import java.util.List;

import org.alfresco.service.cmr.attributes.AttrQuery;
import org.alfresco.util.Pair;

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
    public void save(Attribute attr);

    /**
     * Delete an attribute (recursively).
     * @param attr The attribute to delete.
     */
    public void delete(Attribute attr);

    /**
     * Find all attributes that match a given path and AttrQuery.
     * @param map The map within which to query.
     * @param query The AttrQuery.
     * @return A List of key, attribute value pairs.
     */
    public List<Pair<String,Attribute>> find(MapAttribute map, AttrQuery query);

    /**
     * Delete entries from a map that match the given query.
     * @param map
     * @param query
     */
    public void delete(MapAttribute map, AttrQuery query);

    /**
     * Evict an Attribute from the session cache.
     * @param attr
     */
    public void evict(Attribute attr);

    /**
     * Evict an Attribute non-recursively.
     * @param attr
     */
    public void evictFlat(Attribute attr);

    /**
     * Force a flush.
     */
    public void flush();
}
