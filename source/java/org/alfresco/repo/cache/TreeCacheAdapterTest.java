/*
 * Copyright (C) 2005-2006 Alfresco, Inc.
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

import org.jboss.cache.DummyTransactionManagerLookup;
import org.jboss.cache.Fqn;
import org.jboss.cache.TreeCache;

import junit.framework.TestCase;

/**
 * @see org.alfresco.repo.cache.TreeCacheAdapter
 * 
 * @author Derek Hulley
 */
public class TreeCacheAdapterTest extends TestCase
{
    private static final String KEY_A = "A";
    private static final String VALUE_A = "AAA";
    private static final String KEY_B = "B";
    private static final String VALUE_B = "BBB";
    
    private TreeCache treeCache;
    private TreeCacheAdapter<Serializable, Serializable> cache;
    
    @Override
    public void setUp() throws Exception
    {
        treeCache = new TreeCache();
        treeCache.setTransactionManagerLookupClass(DummyTransactionManagerLookup.class.getName());
        treeCache.start();
        
        cache = new TreeCacheAdapter<Serializable, Serializable>();
        cache.setCache(treeCache);
        cache.setRegionName(getName());
    }
    
    public void testSimplePutGet() throws Exception
    {
        cache.put(KEY_A, VALUE_A);
        cache.put(KEY_B, VALUE_B);
        
        // check that this is present in the underlying cache
        Serializable checkValueA = (Serializable) treeCache.get(new Fqn(getName()), KEY_A);
        assertNotNull("Value A is not present in underlying cache", checkValueA);
        assertEquals("Value A is incorrect in underlying cache", VALUE_A, checkValueA);
        
        Serializable checkValueB = cache.get(KEY_B);
        assertNotNull("Value B is not present in cache", checkValueB);
        assertEquals("Value B is incorrect in cache", VALUE_B, checkValueB);
    }
}
