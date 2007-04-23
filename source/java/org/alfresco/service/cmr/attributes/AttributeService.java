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
 * http://www.alfresco.com/legal/licensing"
 */

package org.alfresco.service.cmr.attributes;

import java.util.List;

import org.alfresco.repo.attributes.Attribute;
import org.alfresco.util.Pair;

/**
 * This provides services for reading, writing, and querying global attributes.
 * @author britt
 */
public interface AttributeService 
{
    /**
     * Get an Attribute.
     * @param path The path of the Attribute.
     * @return The value of the attribute or null.
     */
    public Attribute getAttribute(String path);
    
    /**
     * Get an attribute.
     * @param keys The keys in the attribute path.
     * @return The value of the attribute or null.
     */
    public Attribute getAttribute(List<String> keys);
    
    
    /**
     * Set an attribute. Overwrites if it exists.
     * @param name The name of the Attribute.
     * @param value The value to set.
     */
    public void setAttribute(String path, String name, Attribute value);
    
    /**
     * Set an attribute
     * @param keys List of attribute path keys.
     * @param name The name of the attribute to set.
     * @param value The Attribute to set.
     */
    public void setAttribute(List<String> keys, String name, Attribute value);
    
    /**
     * Remove an Attribute.
     * @param name The name of the Attribute.
     */
    public void removeAttribute(String path, String name);
    
    /**
     * Remove an Attribute.
     * @param keys List of attribute path keys.
     * @param name The name of the attribute to remove.
     */
    public void removeAttribute(List<String> keys, String name);

    /**
     * Query for a list of attributes which are contained in the map
     * defined by the given path and meet the query criteria.
     * @param path
     * @param query
     * @return A List of matching attributes.
     */
    public List<Pair<String, Attribute>> query(String path, AttrQuery query);
    
    /**
     * Query for a list of attributes which are contained in a map defined by the
     * given path and meet the query criteria.
     * @param keys The list of attribute path keys.
     * @param query
     * @return A list of matching attributes.
     */
    public List<Pair<String, Attribute>> query(List<String> keys, AttrQuery query);
    
    /**
     * Get all the keys for a given attribute path.
     * @param path The attribute path.
     * @return A list of all keys.
     */
    public List<String> getKeys(String path);
    
    /**
     * Get all the keys for a give attribute path.
     * @param keys The keys of the attribute path.
     * @return A list of all keys.
     */
    public List<String> getKeys(List<String> keys);
}
