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

import javax.transaction.Status;
import javax.transaction.UserTransaction;

import junit.framework.TestCase;
import net.sf.ehcache.CacheManager;

import org.alfresco.error.ExceptionStackUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.dao.ConcurrencyFailureException;

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
    private SimpleCache<String, Object> objectCache;
    
    @SuppressWarnings("unchecked")
    @Override
    public void setUp() throws Exception
    {
        serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        standaloneCache = (SimpleCache<String, Serializable>) ctx.getBean("ehCache1");
        backingCache = (SimpleCache<String, Serializable>) ctx.getBean("backingCache");
        transactionalCache = (SimpleCache<String, Serializable>) ctx.getBean("transactionalCache");
        objectCache = (SimpleCache<String, Object>) ctx.getBean("objectCache");
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
        assertNotNull(objectCache);
    }
    
    public void testObjectCache() throws Exception
    {
        objectCache.put("A", this);
        Object obj = objectCache.get("A");
        assertTrue("Object not cached properly", this == obj);
    }
    
    public void testEhcacheAdaptors() throws Exception
    {
        backingCache.put("A", "AAA");
        assertNull("Second cache should not have first's present", standaloneCache.get("A"));
        
        assertEquals("AAA", backingCache.get("A"));
        
        Collection<String> keys = backingCache.getKeys();
        assertEquals("Backing cache didn't return correct number of keys", 1, keys.size());
        
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
    
    public void testTransactionalCacheWithSingleTxn() throws Throwable
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
            
            // check that the keys collection is correct
            Collection<String> transactionalKeys = transactionalCache.getKeys();
            assertFalse("Transactionally removed item found in keys", transactionalKeys.contains(newGlobalOne));
            assertTrue("Transactionally added item not found in keys", transactionalKeys.contains(updatedTxnThree));
            
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
    
    /**
     * Starts off with a <tt>null</tt> in the backing cache and adds a value to the
     * transactional cache.  There should be no problem with this.
     */
    public void testNullValue() throws Exception
    {
        TransactionService transactionService = serviceRegistry.getTransactionService();
        UserTransaction txn = transactionService.getUserTransaction();
        try
        {
            txn.begin();
            
            backingCache.put("A", null);
            transactionalCache.put("A", "AAA");
            
            txn.commit();
        }
        finally
        {
            try { txn.rollback(); } catch (Throwable ee) {}
        }
    }
    
    private static final Class[] CONCURRENCY_EXCEPTIONS = {ConcurrencyFailureException.class};
    /** Execute the callback and ensure that the concurrent condition is detected */
    private void executeAndCheck(RetryingTransactionCallback callback) throws Exception
    {
        TransactionService transactionService = serviceRegistry.getTransactionService();
        UserTransaction txn = transactionService.getUserTransaction();
        try
        {
            txn.begin();
            callback.execute();
            txn.commit();
            fail("Failed to detect concurrent modification");
        }
        catch (Throwable e)
        {
            assertNotNull(
                    "Expected a concurrency failure",
                    ExceptionStackUtil.getCause(e, CONCURRENCY_EXCEPTIONS));
        }
        finally
        {
            try { txn.rollback(); } catch (Throwable ee) {}
        }
    }
    
    /**
     * <ul>
     *   <li>Add to the transaction cache</li>
     *   <li>Add to the backing cache</li>
     *   <li>Commit</li>
     * </ul>
     */
    public void testConcurrentAddAgainstAdd() throws Exception
    {
        RetryingTransactionCallback callback = new RetryingTransactionCallback()
        {
            public Object execute() throws Throwable
            {
                transactionalCache.put("A", "AAA");
                backingCache.put("A", "aaa");
                return null;
            }
        };
        executeAndCheck(callback);
    }
    /**
     * <ul>
     *   <li>Add to the transaction cache</li>
     *   <li>Clear the backing cache</li>
     *   <li>Commit</li>
     * </ul>
     */
    public void testConcurrentAddAgainstClear() throws Exception
    {
        RetryingTransactionCallback callback = new RetryingTransactionCallback()
        {
            public Object execute() throws Throwable
            {
                transactionalCache.put("A", "AAA");
                backingCache.clear();
                return null;
            }
        };
        executeAndCheck(callback);
    }
    /**
     * <ul>
     *   <li>Add to the backing cache</li>
     *   <li>Update the transactional cache</li>
     *   <li>Update the backing cache</li>
     *   <li>Commit</li>
     * </ul>
     */
    public void testConcurrentUpdateAgainstUpdate() throws Exception
    {
        RetryingTransactionCallback callback = new RetryingTransactionCallback()
        {
            public Object execute() throws Throwable
            {
                backingCache.put("A", "aaa1");
                transactionalCache.put("A", "AAA");
                backingCache.put("A", "aaa2");
                return null;
            }
        };
        executeAndCheck(callback);
    }
    /**
     * <ul>
     *   <li>Add to the backing cache</li>
     *   <li>Update the transactional cache</li>
     *   <li>Update the backing cache with a <tt>null</tt> value</li>
     *   <li>Commit</li>
     * </ul>
     */
    public void testConcurrentUpdateAgainstUpdateNull() throws Exception
    {
        RetryingTransactionCallback callback = new RetryingTransactionCallback()
        {
            public Object execute() throws Throwable
            {
                backingCache.put("A", "aaa1");
                transactionalCache.put("A", "AAA");
                backingCache.put("A", null);
                return null;
            }
        };
        executeAndCheck(callback);
    }
    /**
     * <ul>
     *   <li>Add to the backing cache</li>
     *   <li>Update the transactional cache with a <tt>null</tt> value</li>
     *   <li>Update the backing cache</li>
     *   <li>Commit</li>
     * </ul>
     */
    public void testConcurrentUpdateNullAgainstUpdate() throws Exception
    {
        RetryingTransactionCallback callback = new RetryingTransactionCallback()
        {
            public Object execute() throws Throwable
            {
                backingCache.put("A", "aaa1");
                transactionalCache.put("A", null);
                backingCache.put("A", "aaa2");
                return null;
            }
        };
        executeAndCheck(callback);
    }
    /**
     * <ul>
     *   <li>Add to the backing cache</li>
     *   <li>Update the transactional cache</li>
     *   <li>Remove from the backing cache</li>
     *   <li>Commit</li>
     * </ul>
     */
    public void testConcurrentUpdateAgainstRemove() throws Exception
    {
        RetryingTransactionCallback callback = new RetryingTransactionCallback()
        {
            public Object execute() throws Throwable
            {
                backingCache.put("A", "aaa1");
                transactionalCache.put("A", "AAA");
                backingCache.remove("A");
                return null;
            }
        };
        executeAndCheck(callback);
    }
    /**
     * <ul>
     *   <li>Add to the backing cache</li>
     *   <li>Update the transactional cache</li>
     *   <li>Clear the backing cache</li>
     *   <li>Commit</li>
     * </ul>
     */
    public void testConcurrentUpdateAgainstClear() throws Exception
    {
        RetryingTransactionCallback callback = new RetryingTransactionCallback()
        {
            public Object execute() throws Throwable
            {
                backingCache.put("A", "aaa1");
                transactionalCache.put("A", "AAA");
                backingCache.clear();
                return null;
            }
        };
        executeAndCheck(callback);
    }
    /**
     * <ul>
     *   <li>Add to the backing cache</li>
     *   <li>Remove from the transactional cache</li>
     *   <li>Add to the backing cache</li>
     *   <li>Commit</li>
     * </ul>
     */
    public void testConcurrentRemoveAgainstUpdate() throws Exception
    {
        RetryingTransactionCallback callback = new RetryingTransactionCallback()
        {
            public Object execute() throws Throwable
            {
                backingCache.put("A", "aaa1");
                transactionalCache.remove("A");
                backingCache.put("A", "aaa2");
                return null;
            }
        };
        executeAndCheck(callback);
    }
    /**
     * <ul>
     *   <li>Add to the backing cache</li>
     *   <li>Remove from the transactional cache</li>
     *   <li>Remove from the backing cache</li>
     *   <li>Commit</li>
     * </ul>
     */
    public void testConcurrentRemoveAgainstRemove() throws Exception
    {
        RetryingTransactionCallback callback = new RetryingTransactionCallback()
        {
            public Object execute() throws Throwable
            {
                backingCache.put("A", "aaa1");
                transactionalCache.remove("A");
                backingCache.remove("A");
                return null;
            }
        };
        executeAndCheck(callback);
    }
    /**
     * <ul>
     *   <li>Add to the backing cache</li>
     *   <li>Remove from the transactional cache</li>
     *   <li>Clear the backing cache</li>
     *   <li>Commit</li>
     * </ul>
     */
    public void testConcurrentRemoveAgainstClear() throws Exception
    {
        RetryingTransactionCallback callback = new RetryingTransactionCallback()
        {
            public Object execute() throws Throwable
            {
                backingCache.put("A", "aaa1");
                transactionalCache.remove("A");
                backingCache.clear();
                return null;
            }
        };
        executeAndCheck(callback);
    }
}
