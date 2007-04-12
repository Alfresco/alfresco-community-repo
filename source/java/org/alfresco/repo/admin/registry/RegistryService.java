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
package org.alfresco.repo.admin.registry;

import java.io.Serializable;
import java.util.Collection;

/**
 * Interface for service providing access to key-value pairs for storage
 * of system-controlled metadata.
 * 
 * @author Derek Hulley
 */
public interface RegistryService
{
    /**
     * Assign a value to the registry key, which must be of the form <b>/a/b/c</b>.
     * 
     * @param key           the registry key.
     * @param value         any value that can be stored in the repository.
     */
    void addValue(RegistryKey key, Serializable value);
    
    /**
     * @param key           the registry key.
     * @return              Returns the value stored in the key.
     * 
     * @see #addValue(String, Serializable)
     */
    Serializable getValue(RegistryKey key);
    
    /**
     * Fetches all child elements for the given path.  So for a registry key <b>key=/a/b/ignored</b>, the
     * results of <code>getChildElements(key)</b> would be <code>b</code>.  The key's last path
     * element represents the key's property name, and can therefore be any value without affecting
     * the outcome of the call.
     * 
     * @param key           the registry key with the path.  The last element in the path
     *                      will be ignored, and can be any acceptable property localname.
     * @return              Returns all child elements (not values) for the given key, ignoring
     *                      the last element in the key.
     */
    Collection<String> getChildElements(RegistryKey key);
}
