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

/**
 * 
 * Represents a loaded, cached encryption key. The key can be <tt>null</tt>.
 * 
 * @since 4.0
 *
 */
public class CachedKey
{
    public static CachedKey NULL = new CachedKey(null, null);

    private Key key;
    private long timestamp;

    CachedKey(Key key, Long timestamp)
    {
        this.key = key;
        this.timestamp = (timestamp != null ? timestamp.longValue() : -1);
    }

    public CachedKey(Key key)
    {
        super();
        this.key = key;
        this.timestamp = System.currentTimeMillis();
    }

    public Key getKey()
    {
        return key;
    }

    public long getTimestamp()
    {
        return timestamp;
    }
}
