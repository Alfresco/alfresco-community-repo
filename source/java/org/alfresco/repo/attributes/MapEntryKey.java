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

import java.io.Serializable;

/**
 * Key class for MapEntries.
 * @author britt
 */
public class MapEntryKey implements Serializable
{
    private static final long serialVersionUID = 1637682889407656800L;

    private MapAttribute fMap;
    
    private String fKey;
    
    public MapEntryKey()
    {
    }
    
    public MapEntryKey(MapAttribute map, String key)
    {
        fMap = map;
        fKey = key;
    }

    /**
     * @return the Key
     */
    public String getKey()
    {
        return fKey;
    }

    /**
     * @param key the Key to set
     */
    public void setKey(String key)
    {
        fKey = key;
    }

    /**
     * @return the Map
     */
    public MapAttribute getMap()
    {
        return fMap;
    }

    /**
     * @param map the Map to set
     */
    public void setMap(MapAttribute map)
    {
        fMap = map;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!(obj instanceof MapEntryKey))
        {
            return false;
        }
        MapEntryKey other = (MapEntryKey)obj;
        return fKey.equals(other.getKey()) && 
               fMap.equals(other.getMap());
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return fKey.hashCode() + fMap.hashCode();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "[MapEntryKey:" + fKey + ']';
    }
}
