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
package org.alfresco.util;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.namespace.QName;

/**
 * Property map helper class.  
 * <p>
 * This class can be used as a short hand when a class of type
 * Map<QName, Serializable> is required.
 * 
 * @author Roy Wetherall
 */
public class PropertyMap extends HashMap<QName, Serializable>
{
    private static final long serialVersionUID = 8052326301073209645L;
    
    /**
     * A static empty map to us when having to deal with nulls
     */
    public static final Map<QName, Serializable> EMPTY_MAP = Collections.<QName, Serializable>emptyMap();
    
    /**
     * @see HashMap#HashMap(int, float)
     */
    public PropertyMap(int initialCapacity, float loadFactor)
    {
        super(initialCapacity, loadFactor);
    }
    
    /**
     * @see HashMap#HashMap(int)
     */
    public PropertyMap(int initialCapacity)
    {
        super(initialCapacity);
    }
    
    /**
     * @see HashMap#HashMap()
     */
    public PropertyMap()
    {
        super();
    }
    
    /**
     * Utility method to work out what changed between two property maps and to generate a difference map.
     * 
     * @param before                the properties before (may be <tt>null</tt>)
     * @param after                 the properties after (may be <tt>null</tt>)
     * @return                      Return a map of values that <b>changed</b> from before to after.
     *                              <tt>null</tt> values will have keys in the map, but no value.
     * 
     * @since 3.2
     */
    public static Map<QName, Serializable> getDelta(Map<QName, Serializable> before, Map<QName, Serializable> after)
    {
        // Shortcuts
        if (before == null && after == null)
        {
            return Collections.emptyMap();
        }
        else if (before == null || before.isEmpty())
        {
            return after;
        }
        else if (after == null || after.isEmpty())
        {
            // Return the 'before' map but with no values
            Map<QName, Serializable> diff = new HashMap<QName, Serializable>(before.size() * 2);
            for (QName qname : before.keySet())
            {
                diff.put(qname, null);
            }
            return diff;
        }
        
        // Make a copy of the after values
        Map<QName, Serializable> diff = new HashMap<QName, Serializable>(after);
        // Remove all entries that didn't change to leave the new or modified values
        diff.entrySet().removeAll(before.entrySet());
        // Now find all values values that were removed and put null values in
        Set<QName> beforeKeysCopy = new HashSet<QName>(before.keySet());
        beforeKeysCopy.removeAll(after.keySet());
        for (QName qname : beforeKeysCopy)
        {
            diff.put(qname, null);
        }
        // Done
        return diff;
    }
}
