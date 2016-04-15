/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.util;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.alfresco.service.namespace.QName;

/**
 * Property map helper class.  
 * <p>
 * This class can be used as a short hand when a class of type
 * Map&lt;QName, Serializable&gt; is required.
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
     * Utility method to remove unchanged entries from each map.
     * 
     * @param before                the properties before (may be <tt>null</tt>)
     * @param after                 the properties after (may be <tt>null</tt>)
     * @return                      Return a map of values that <b>changed</b> from before to after.
     *                              The before value is first and the after value is second.
     * 
     * @since 3.2
     */
    public static Pair<Map<QName, Serializable>, Map<QName, Serializable>> getBeforeAndAfterMapsForChanges(
            Map<QName, Serializable> before,
            Map<QName, Serializable> after)
    {
        // Shortcuts
        if (before == null)
        {
            before = Collections.emptyMap();
        }
        if (after == null)
        {
            after = Collections.emptyMap();
        }
        
        // Get after values that changed
        Map<QName, Serializable> afterDelta = new HashMap<QName, Serializable>(after);
        afterDelta.entrySet().removeAll(before.entrySet());
        // Get before values that changed
        Map<QName, Serializable> beforeDelta = new HashMap<QName, Serializable>(before);
        beforeDelta.entrySet().removeAll(after.entrySet());
        
        // Done
        return new Pair<Map<QName, Serializable>, Map<QName, Serializable>>(beforeDelta, afterDelta);
    }
    
    /**
     * Utility method to get properties which were added as part of a change.
     * 
     * @param before the properties before (may be <code>null</code>).
     * @param after  the properties after (may be <code>null</code>).
     * @return       a map of values that were added along with their new values.
     * 
     * @since Odin
     */
    public static Map<QName, Serializable> getAddedProperties(Map<QName, Serializable> before, Map<QName, Serializable> after)
    {
        Map<QName, Serializable> result = new HashMap<QName, Serializable>();
        
        if (before != null && after != null)
        {
            result.putAll(after);
            result.keySet().removeAll(before.keySet());
        }
        
        return result;
    }
    
    /**
     * Utility method to get properties which were removed as part of a change.
     * 
     * @param before the properties before (may be <code>null</code>).
     * @param after  the properties after (may be <code>null</code>).
     * @return       a map of values that were removed along with their old values.
     * 
     * @since Odin
     */
    public static Map<QName, Serializable> getRemovedProperties(Map<QName, Serializable> before, Map<QName, Serializable> after)
    {
        Map<QName, Serializable> result = new HashMap<QName, Serializable>();
        
        if (before != null && after != null)
        {
            result.putAll(before);
            result.keySet().removeAll(after.keySet());
        }
        
        return result;
    }
    
    /**
     * Utility method to get properties which were changed (but not added or removed) as part of a change.
     * 
     * @param before the properties before (may be <code>null</code>).
     * @param after  the properties after (may be <code>null</code>).
     * @return       a map of values that were changed along with their new values.
     * 
     * @since Odin
     */
    public static Map<QName, Serializable> getChangedProperties(Map<QName, Serializable> before, Map<QName, Serializable> after)
    {
        Map<QName, Serializable> result = new HashMap<QName, Serializable>();
        
        if (before != null && after != null)
        {
            Map<QName, Serializable> intersection = new HashMap<QName, Serializable>();
            
            intersection.putAll(after);
            intersection.keySet().retainAll(before.keySet());
            
            for (Entry<QName, Serializable> entry : intersection.entrySet())
            {
                if ( ! EqualsHelper.nullSafeEquals(before.get(entry.getKey()), after.get(entry.getKey())))
                {
                    result.put(entry.getKey(), entry.getValue());
                }
            }
        }
        
        return result;
    }
    
    /**
     * Utility method to get properties which were unchanged as part of a change.
     * 
     * @param before the properties before (may be <code>null</code>).
     * @param after  the properties after (may be <code>null</code>).
     * @return       a map of values that were unchanged along with their values.
     * 
     * @since Odin
     */
    public static Map<QName, Serializable> getUnchangedProperties(Map<QName, Serializable> before, Map<QName, Serializable> after)
    {
        Map<QName, Serializable> result = new HashMap<QName, Serializable>();
        
        if (before != null && after != null)
        {
            Map<QName, Serializable> intersection = new HashMap<QName, Serializable>();
            
            intersection.putAll(before);
            intersection.keySet().retainAll(after.keySet());
            
            for (Entry<QName, Serializable> entry : intersection.entrySet())
            {
                if ( EqualsHelper.nullSafeEquals(before.get(entry.getKey()), after.get(entry.getKey())))
                {
                    result.put(entry.getKey(), entry.getValue());
                }
            }
        }
        
        return result;
    }
}
