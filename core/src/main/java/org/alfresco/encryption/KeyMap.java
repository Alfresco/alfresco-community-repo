/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.encryption;

import java.security.Key;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A simple map of key aliases to keys. Each key has an associated timestamp indicating
 * when it was last loaded from the keystore on disk.
 * 
 * @since 4.0
 *
 */
public class KeyMap
{
    private Map<String, CachedKey> keys;

    public KeyMap()
    {
        this.keys = new HashMap<String, CachedKey>(5);
    }

    public KeyMap(Map<String, CachedKey> keys)
    {
        super();
        this.keys = keys;
    }
    
    public int numKeys()
    {
        return keys.size();
    }

    public Set<String> getKeyAliases()
    {
        return keys.keySet();
    }

    // always returns an instance; if null will return a CachedKey.NULL
    public CachedKey getCachedKey(String keyAlias)
    {
        CachedKey cachedKey = keys.get(keyAlias);
        return (cachedKey != null ? cachedKey : CachedKey.NULL);
    }

    public Key getKey(String keyAlias)
    {
        return getCachedKey(keyAlias).getKey();
    }
    
    public void setKey(String keyAlias, Key key)
    {
        keys.put(keyAlias, new CachedKey(key));
    }
}
