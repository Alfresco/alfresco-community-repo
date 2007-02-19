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
package org.alfresco.repo.cache;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;

/**
 * A cache that does nothing - always.
 * <P/>
 * There are conditions under which code that expects to be caching, should not be.  Using this
 * cache, it becomes possible to configure a valid cache in whilst still ensuring that the
 * actual caching is not performed.
 * 
 * @author Derek Hulley
 */
public class NullCache<K extends Serializable, V extends Serializable> implements SimpleCache<K, V>
{
    public NullCache()
    {
    }

    /** NO-OP */
    public boolean contains(K key)
    {
        return false;
    }

    public Collection<K> getKeys()
    {
        return Collections.<K>emptyList();
    }

    /** NO-OP */
    public V get(K key)
    {
        return null;
    }

    /** NO-OP */
    public void put(K key, V value)
    {
        return;
    }

    /** NO-OP */
    public void remove(K key)
    {
        return;
    }

    /** NO-OP */
    public void clear()
    {
        return;
    }
}
