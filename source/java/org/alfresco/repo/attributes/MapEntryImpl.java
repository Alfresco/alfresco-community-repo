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

/**
 * Implementation of map entries.
 * @author britt
 */
public class MapEntryImpl implements MapEntry
{
    private long fID;
    
    private MapAttribute fMap;
    
    private String fKey;
    
    private Attribute fAttribute;
    
    public MapEntryImpl()
    {
    }
    
    public MapEntryImpl(MapAttribute map,
                        String key,
                        Attribute attribute)
    {
        fMap = map;
        fKey = key;
        fAttribute = attribute;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.MapEntry#getAttribute()
     */
    public Attribute getAttribute()
    {
        return fAttribute;
    }

    /**
     * Set the attribute.
     * @param attr The attribute.
     */
    public void setAttribute(Attribute attr)
    {
        fAttribute = attr;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.MapEntry#getId()
     */
    public long getId()
    {
        return fID;
    }

    /**
     * Setter.
     */
    public void setId(long id)
    {
        fID = id;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.MapEntry#getKey()
     */
    public String getKey()
    {
        return fKey;
    }
    
    /**
     * Setter.
     */
    public void setKey(String key)
    {
        fKey = key;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.MapEntry#getMap()
     */
    public MapAttribute getMap()
    {
        return fMap;
    }
    
    /**
     * Setter.
     */
    public void setMap(MapAttribute map)
    {
        fMap = map;
    }
}
