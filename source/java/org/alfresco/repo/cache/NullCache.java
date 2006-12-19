/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
