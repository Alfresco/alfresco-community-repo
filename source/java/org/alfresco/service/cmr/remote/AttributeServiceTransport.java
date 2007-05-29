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

package org.alfresco.service.cmr.remote;

import java.util.List;
import java.util.Map;

import org.alfresco.repo.attributes.Attribute;
import org.alfresco.service.cmr.attributes.AttrQuery;
import org.alfresco.util.Pair;

/**
 * The transport wrapper for remoted AttributeService.
 * @author britt
 */
public interface AttributeServiceTransport
{
    /**
     * Get an Attribute.
     * @param ticket The authentication ticket.
     * @param path The path of the Attribute.
     * @return The value of the attribute or null.
     */
    public Attribute getAttribute(String ticket, String path);
    
    /**
     * Get an attribute.
     * @param ticket The authentication ticket.
     * @param keys The keys in the attribute path.
     * @return The value of the attribute or null.
     */
    public Attribute getAttribute(String ticket, List<String> keys);
    
    /**
     * Set an attribute. Overwrites if it exists.
     * @param ticket The authentication ticket.
     * @param name The name of the Attribute.
     * @param value The value to set.
     */
    public void setAttribute(String ticket, String path, String name, Attribute value);
    
    /**
     * Set an attribute
     * @param ticket The authentication ticket.
     * @param keys List of attribute path keys.
     * @param name The name of the attribute to set.
     * @param value The Attribute to set.
     */
    public void setAttribute(String ticket, List<String> keys, String name, Attribute value);
    
    /**
     * Set an attribute in a list.
     * @param ticket The authentication ticket.
     * @param path The path to the list.
     * @param index The list index.
     * @param value The Attribute to set.
     */
    public void setAttribute(String ticket, String path, int index, Attribute value);
    
    /**
     * Set an attribute in a list.
     * @param ticket The authentication ticket.
     * @param keys The path components to the list.
     * @param index The list index.
     * @param value The Attribute to set.
     */
    public void setAttribute(String ticket, List<String> keys, int index, Attribute value);
    
    /**
     * Add an attribute to a List Attribute
     * @param ticket The authentication ticket.
     * @param path The path to the list.
     * @param value The Attribute to add.
     */
    public void addAttribute(String ticket, String path, Attribute value);
    
    /**
     * Add an attribute to a List Attribute.
     * @param ticket The authentication ticket.
     * @param keys The path components to the list.
     * @param value The Attribute to add.
     */
    public void addAttribute(String ticket, List<String> keys, Attribute value);
    
    /**
     * Remove an Attribute.
     * @param ticket The authentication ticket.
     * @param name The name of the Attribute.
     */
    public void removeAttribute(String ticket, String path, String name);
    
    /**
     * Remove an Attribute.
     * @param ticket The authentication ticket.
     * @param keys List of attribute path keys.
     * @param name The name of the attribute to remove.
     */
    public void removeAttribute(String ticket, List<String> keys, String name);
    
    /**
     * Remove an attribute from a list.
     * @param ticket The authentication ticket.
     * @param path The path to the list.
     * @param index The index to remove.
     */
    public void removeAttribute(String ticket, String path, int index);

    /**
     * Remove an attribute from a list.
     * @param ticket The authentication ticket.
     * @param keys The components of the path to the list.
     * @param index The index to remove.
     */
    public void removeAttribute(String ticket, List<String> keys, int index);
    
    /**
     * Query for a list of attributes which are contained in the map
     * defined by the given path and meet the query criteria.
     * @param ticket The authentication ticket.
     * @param path
     * @param query
     * @return A List of matching attributes.
     */
    public List<Pair<String, Attribute>> query(String ticket, String path, AttrQuery query);
    
    /**
     * Query for a list of attributes which are contained in a map defined by the
     * given path and meet the query criteria.
     * @param ticket The authentication ticket.
     * @param keys The list of attribute path keys.
     * @param query
     * @return A list of matching attributes.
     */
    public List<Pair<String, Attribute>> query(String ticket, List<String> keys, AttrQuery query);
    
    /**
     * Get all the keys for a given attribute path.
     * @param ticket The authentication ticket.
     * @param path The attribute path.
     * @return A list of all keys.
     */
    public List<String> getKeys(String ticket, String path);
    
    /**
     * Get all the keys for a give attribute path.
     * @param ticket The authentication ticket.
     * @param keys The keys of the attribute path.
     * @return A list of all keys.
     */
    public List<String> getKeys(String ticket, List<String> keys);

    /**
     * Get the size of a map or list.
     * @param keys List of attribute path keys.
     * @return The size of of the list or map.
     */
    public int getCount(String ticket, List<String> keys);
    
    /**
     * Get the size of a map or list.
     * @param path The path to the map or list.
     * @return The size of the list or map.
     */
    public int getCount(String ticket, String path);
    
    /**
     * Does an attribute exist.
     * @param keys List of attribute path keys.
     * @return Whether the attribute exists.
     */
    public boolean exists(String ticket, List<String> keys);
 
    /**
     * Does an attribute exist.
     * @param path The path to the attribute.
     * @return Whether the attribute exists.
     */
    public boolean exists(String ticket, String path);

    /**
     * Add a list of attributes.
     * @param ticket
     * @param keys
     * @param values
     */
    public void addAttributes(String ticket, List<String> keys, List<Attribute> values);

    /**
     * Add a list of attributes.
     * @param ticket
     * @param path
     * @param values
     */
    public void addAttributes(String ticket, String path, List<Attribute> values);

    /**
     * Add a set of attributes.
     * @param ticket
     * @param keys
     * @param entries
     */
    public void setAttributes(String ticket, List<String> keys, Map<String, Attribute> entries);

    /**
     * Add a set of attributes.
     * @param ticket
     * @param path
     * @param entries
     */
    public void setAttributes(String ticket, String path, Map<String, Attribute> entries);

    /**
     * Remove entries from a map that match a query.
     * @param ticket
     * @param keys
     * @param query
     */
    public void removeEntries(String ticket, List<String> keys, AttrQuery query);

    /**
     * Remove entries from a map that match a query.
     * @param ticket
     * @param path
     * @param query
     */
    public void removeEntries(String ticket, String path, AttrQuery query);
}
