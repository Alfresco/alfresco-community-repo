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

import javax.transaction.Status;
import javax.transaction.UserTransaction;

import net.sf.ehcache.CacheManager;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import junit.framework.TestCase;

/**
 * @see org.alfresco.repo.cache.EhCacheAdapter
 * 
 * @author Derek Hulley
 */
public class CacheTest extends TestCase
{
    private static ApplicationContext ctx =new ClassPathXmlApplicationContext(
            new String[] {"classpath:cache-test-context.xml", ApplicationContextHelper.CONFIG_LOCATIONS[0]}
            );
    
    private ServiceRegistry serviceRegistry;
    private SimpleCache<String, Serializable> standaloneCache;
    private SimpleCache<String, Serializable> backingCache;
    private SimpleCache<String, Serializable> transactionalCache;
    
    @SuppressWarnings("unchecked")
    @Override
    public void setUp() throws Exception
    {
        serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        standaloneCache = (SimpleCache<String, Serializable>) ctx.getBean("ehCache1");
        backingCache = (SimpleCache<String, Serializable>) ctx.getBean("backingCache");
        transactionalCache = (SimpleCache<String, Serializable>) ctx.getBean("transactionalCache");
    }
    
    @Override
    public void tearDown()
    {
        serviceRegistry = null;
        standaloneCache = null;
        backingCache = null;
        transactionalCache = null;
    }
    
    public void testSetUp() throws Exception
    {
        CacheManager cacheManager = (CacheManager) ctx.getBean("ehCacheManager");
        assertNotNull(cacheManager);
        CacheManager cacheManagerCheck = (CacheManager) ctx.getBean("ehCacheManager");
        assertTrue(cacheManager == cacheManagerCheck);
    
        assertNotNull(serviceRegistry);
        assertNotNull(backingCache);
        assertNotNull(standaloneCache);
        assertNotNull(transactionalCache);
    }
    
    public void testEhcacheAdaptors() throws Exception
    {
        backingCache.put("A", "AAA");
        assertNull("Second cache should not have first's present", standaloneCache.get("A"));
        
        assertEquals("AAA", backingCache.get("A"));
        
        backingCache.remove("A");
        assertNull(backingCache.get("A"));
    }
    
    public void testTransactionalCacheNoTxn() throws Exception
    {
        String key = "B";
        String value = "BBB";
        // no transaction - do a put
        transactionalCache.put(key, value);
        // check that the value appears in the backing cache, backingCache
        assertEquals("Backing cache not used for put when no transaction present", value, backingCache.get(key));
        
        // remove the value from the backing cache and check that it is removed from the transaction cache
        backingCache.remove(key);
        assertNull("Backing cache not used for removed when no transaction present", transactionalCache.get(key));
        
        // add value into backing cache
        backingCache.put(key, value);
        // remove it from the transactional cache
        transactionalCache.remove(key);
        // check that it is gone from the backing cache
        assertNull("Non-transactional remove didn't go to backing cache", backingCache.get(key));
    }
    
    public void testTransactionalCacheWithTxn() throws Throwable
    {
        String newGlobalOne = "new_global_one";
        String newGlobalTwo = "new_global_two";
        String newGlobalThree = "new_global_three";
        String updatedTxnThree = "updated_txn_three";
        
        // add item to global cache
        backingCache.put(newGlobalOne, newGlobalOne);
        backingCache.put(newGlobalTwo, newGlobalTwo);
        backingCache.put(newGlobalThree, newGlobalThree);
        
        TransactionService transactionService = serviceRegistry.getTransactionService();
        UserTransaction txn = transactionService.getUserTransaction();
        try
        {
            // begin a transaction
            txn.begin();
            
            // remove 1 from the cache
            transactionalCache.remove(newGlobalOne);
            assertFalse("Item was not removed from txn cache", transactionalCache.contains(newGlobalOne));
            assertNull("Get didn't return null", transactionalCache.get(newGlobalOne));
            assertTrue("Item was removed from backing cache", backingCache.contains(newGlobalOne));
            
            // update 3 in the cache
            transactionalCache.put(updatedTxnThree, "XXX");
            assertEquals("Item not updated in txn cache", "XXX", transactionalCache.get(updatedTxnThree));
            assertFalse("Item was put into backing cache", backingCache.contains(updatedTxnThree));
            
            // commit the transaction
            txn.commit();
            
            // check that backing cache was updated with the in-transaction changes
            assertFalse("Item was not removed from backing cache", backingCache.contains(newGlobalOne));
            assertNull("Item could still be fetched from backing cache", backingCache.get(newGlobalOne));
            assertEquals("Item not updated in backing cache", "XXX", backingCache.get(updatedTxnThree));
        }
        catch (Throwable e)
        {
            if (txn.getStatus() == Status.STATUS_ACTIVE)
            {
                txn.rollback();
            }
            throw e;
        }
    }
    
    /**
     * Preloads the cache, then performs a simultaneous addition of N new values and
     * removal of the N preloaded values.
     * 
     * @param cache
     * @param objectCount
     * @return Returns the time it took in <b>nanoseconds</b>.
     */
    public long runPerformanceTestOnCache(SimpleCache<String, Serializable> cache, int objectCount)
    {
        // preload
        for (int i = 0; i < objectCount; i++)
        {
            String key = Integer.toString(i);
            Integer value = new Integer(i);
            cache.put(key, value);
        }
        
        // start timer
        long start = System.nanoTime();
        for (int i = 0; i < objectCount; i++)
        {
            String key = Integer.toString(i);
            cache.remove(key);
            // add a new value
            key = Integer.toString(i + objectCount);
            Integer value = new Integer(i + objectCount);
            cache.put(key, value);
        }
        // stop
        long stop = System.nanoTime();
        
        return (stop - start);
    }
    
    /**
     * Tests a straight Ehcache adapter against a transactional cache both in and out
     * of a transaction.  This is done repeatedly, pushing the count up.
     */
    public void testPerformance() throws Exception
    {
        for (int i = 0; i < 5; i++)
        {
            int count = (int) Math.pow(10D, (double)i);
            
            // test standalone
            long timePlain = runPerformanceTestOnCache(standaloneCache, count);
            
            // do transactional cache in a transaction
            TransactionService transactionService = serviceRegistry.getTransactionService();
            UserTransaction txn = transactionService.getUserTransaction();
            txn.begin();
            long timeTxn = runPerformanceTestOnCache(transactionalCache, count);
            long commitStart = System.nanoTime();
            txn.commit();
            long commitEnd = System.nanoTime();
            long commitTime = (commitEnd - commitStart);
            // add this to the cache's performance overhead
            timeTxn += commitTime;
            
            // report
            System.out.println("Cache performance test: \n" +
                    "   count: " + count + "\n" +
                    "   direct: " + timePlain/((long)count) + " ns\\count \n" + 
                    "   transaction: " + timeTxn/((long)count) + " ns\\count"); 
        }
    }
}
