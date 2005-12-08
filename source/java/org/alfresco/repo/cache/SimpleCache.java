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

/**
 * Basic caching interface.
 * <p>
 * All implementations <b>must</b> be thread-safe.  Additionally, the use of the
 * <tt>Serializable</tt> for both keys and values ensures that the underlying
 * cache implementations can support both clustered caches as well as persistent
 * caches.
 * 
 * @author Derek Hulley
 */
public interface SimpleCache<K extends Serializable, V extends Serializable>
{
    public boolean contains(K key);
    
    public V get(K key);
    
    public void put(K key, V value);
    
    public void remove(K key);
    
    public void clear();
}
