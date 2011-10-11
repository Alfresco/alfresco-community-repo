/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.cache;

import java.sql.SQLException;
import java.util.Collection;

import javax.transaction.Status;
import javax.transaction.UserTransaction;

import junit.framework.TestCase;
import net.sf.ehcache.CacheManager;

import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;

/**
 * @see org.alfresco.repo.cache.EhCacheAdapter
 * 
 * @author Derek Hulley
 */
public class CacheTest extends TestCase
{
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext(
            new String[] {
                    "classpath:cache-test/cache-test-context.xml",
                    ApplicationContextHelper.CONFIG_LOCATIONS[0]});
    
    private ServiceRegistry serviceRegistry;
    private SimpleCache<String, Object> standaloneCache;
    private SimpleCache<String, Object> backingCache;
    private TransactionalCache<String, Object> transactionalCache;
    private SimpleCache<String, Object> objectCache;
    
    @SuppressWarnings("unchecked")
    @Override
    public void setUp() throws Exception
    {
        serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        standaloneCache = (SimpleCache<String, Object>) ctx.getBean("ehCache1");
        backingCache = (SimpleCache<String, Object>) ctx.getBean("backingCache");
        transactionalCache = (TransactionalCache<String, Object>) ctx.getBean("transactionalCache");
        objectCache = (SimpleCache<String, Object>) ctx.getBean("objectCache");
        
        // Make the cache mutable (default)
        transactionalCache.setMutable(true);
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
        CacheManager cacheManager = (CacheManager) ctx.getBean("testEHCacheManager");
        assertNotNull(cacheManager);
        CacheManager cacheManagerCheck = (CacheManager) ctx.getBean("testEHCacheManager");
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
    
    private static final String NEW_GLOBAL_ONE = "new_global_one";
    private static final String NEW_GLOBAL_TWO = "new_global_two";
    private static final String NEW_GLOBAL_THREE = "new_global_three";
    private static final String UPDATE_TXN_THREE = "updated_txn_three";
    private static final String UPDATE_TXN_FOUR = "updated_txn_four";

    public void testRollbackCleanup() throws Exception
    {
        TransactionService transactionService = serviceRegistry.getTransactionService();
        RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();

        // Add items to the global cache
        backingCache.put(NEW_GLOBAL_ONE, NEW_GLOBAL_ONE);
        
        RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>()
        {
            private int throwCount = 0;
            public Object execute() throws Throwable
            {
                transactionalCache.put(NEW_GLOBAL_TWO, NEW_GLOBAL_TWO);
                transactionalCache.remove(NEW_GLOBAL_ONE);

                String key = "B";
                String value = "BBB";
                // no transaction - do a put
                transactionalCache.put(key, value);
                // Blow up
                if (throwCount < 5)
                {
                    throwCount++;
                    throw new SQLException("Dummy");
                }
                else
                {
                    throw new Exception("Fail");
                }
            }
        };
        try
        {
            txnHelper.doInTransaction(callback);
        }
        catch (Exception e)
        {
            // Expected
        }
        
        assertFalse("Remove not done after rollback", transactionalCache.contains(NEW_GLOBAL_ONE));
        assertFalse("Update happened after rollback", transactionalCache.contains(NEW_GLOBAL_TWO));
    }
    
    public void testTransactionalCacheWithSingleTxn() throws Throwable
    {
        // add item to global cache
        backingCache.put(NEW_GLOBAL_ONE, NEW_GLOBAL_ONE);
        backingCache.put(NEW_GLOBAL_TWO, NEW_GLOBAL_TWO);
        backingCache.put(NEW_GLOBAL_THREE, NEW_GLOBAL_THREE);
        
        TransactionService transactionService = serviceRegistry.getTransactionService();
        UserTransaction txn = transactionService.getUserTransaction();
        try
        {
            // begin a transaction
            txn.begin();
            
            // remove 1 from the cache
            transactionalCache.remove(NEW_GLOBAL_ONE);
            assertFalse("Item was not removed from txn cache", transactionalCache.contains(NEW_GLOBAL_ONE));
            assertNull("Get didn't return null", transactionalCache.get(NEW_GLOBAL_ONE));
            assertTrue("Item was removed from backing cache", backingCache.contains(NEW_GLOBAL_ONE));
            
            // update 3 in the cache
            transactionalCache.put(UPDATE_TXN_THREE, "XXX");
            assertEquals("Item not updated in txn cache", "XXX", transactionalCache.get(UPDATE_TXN_THREE));
            assertFalse(
                    "Item was put into backing cache",
                    backingCache.contains(UPDATE_TXN_THREE));
            
            // check that the keys collection is correct
            Collection<String> transactionalKeys = transactionalCache.getKeys();
            assertFalse("Transactionally removed item found in keys", transactionalKeys.contains(NEW_GLOBAL_ONE));
            assertTrue("Transactionally added item not found in keys", transactionalKeys.contains(UPDATE_TXN_THREE));
            
            // Register a post-commit cache reader to make sure that nothing blows up if the cache is hit in post-commit
            PostCommitCacheReader listenerReader = new PostCommitCacheReader(transactionalCache, UPDATE_TXN_THREE);
            AlfrescoTransactionSupport.bindListener(listenerReader);
            
            // Register a post-commit cache reader to make sure that nothing blows up if the cache is hit in post-commit
            PostCommitCacheWriter listenerWriter = new PostCommitCacheWriter(transactionalCache, UPDATE_TXN_FOUR, "FOUR");
            AlfrescoTransactionSupport.bindListener(listenerWriter);
            
            // commit the transaction
            txn.commit();
            
            // Check the post-commit stressers
            if (listenerReader.e != null)
            {
                throw listenerReader.e;
            }
            if (listenerWriter.e != null)
            {
                throw listenerWriter.e;
            }
            
            // check that backing cache was updated with the in-transaction changes
            assertFalse("Item was not removed from backing cache", backingCache.contains(NEW_GLOBAL_ONE));
            assertNull("Item could still be fetched from backing cache", backingCache.get(NEW_GLOBAL_ONE));
            assertEquals("Item not updated in backing cache", "XXX", backingCache.get(UPDATE_TXN_THREE));
            
            // Check that the transactional cache serves get requests
            assertEquals("Transactional cache must serve post-commit get requests", "XXX",
                    transactionalCache.get(UPDATE_TXN_THREE));
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
     * This transaction listener attempts to read from the cache in the afterCommit phase.  Technically the
     * transaction has finished, but the transaction resources are still available.
     * 
     * @author Derek Hulley
     * @since 2.1
     */
    private class PostCommitCacheReader extends TransactionListenerAdapter
    {
        private final SimpleCache<String, Object> transactionalCache;
        private final String key;
        private Throwable e;
        private PostCommitCacheReader(SimpleCache<String, Object> transactionalCache, String key)
        {
            this.transactionalCache = transactionalCache;
            this.key = key;
        }
        @Override
        public void afterCommit()
        {
            try
            {
                transactionalCache.get(key);
            }
            catch (Throwable e)
            {
                this.e = e;
                return;
            }
        }
    }
    
    /**
     * This transaction listener attempts to write to the cache in the afterCommit phase.  Technically the
     * transaction has finished, but the transaction resources are still available.
     * 
     * @author Derek Hulley
     * @since 2.1
     */
    private class PostCommitCacheWriter extends TransactionListenerAdapter
    {
        private final SimpleCache<String, Object> transactionalCache;
        private final String key;
        private final Object value;
        private Throwable e;
        private PostCommitCacheWriter(SimpleCache<String, Object> transactionalCache, String key, Object value)
        {
            this.transactionalCache = transactionalCache;
            this.key = key;
            this.value = value;
        }
        @Override
        public void afterCommit()
        {
            try
            {
                transactionalCache.put(key, value);
                transactionalCache.remove(key);
                transactionalCache.clear();
            }
            catch (Throwable e)
            {
                this.e = e;
                return;
            }
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
    public long runPerformanceTestOnCache(SimpleCache<String, Object> cache, int objectCount)
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
        for (int i = 0; i < 6; i++)
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
     * Time how long it takes to create and complete a whole lot of transactions
     */
    public void testInitializationPerformance() throws Exception
    {
        TransactionService transactionService = serviceRegistry.getTransactionService();
        long start = System.nanoTime();
        int count = 10000;
        for (int i = 0; i < count; i++)
        {
            UserTransaction txn = transactionService.getUserTransaction();
            try
            {
                txn.begin();
                transactionalCache.contains("A");
            }
            finally
            {
                try { txn.rollback(); } catch (Throwable ee) {}
            }
        }
        long end = System.nanoTime();
        
        // report
        System.out.println(
                "Cache initialization performance test: \n" +
                "   count:       " + count + "\n" +
                "   transaction: " + (end-start)/((long)count) + " ns\\count"); 
    }
    
    /**
     * @see #testPerformance()
     */
    public static void main(String ... args)
    {
        try
        {
            CacheTest test = new CacheTest();
            test.setUp();
            System.out.println("Press any key to run initialization test ...");
            System.in.read();
            test.testInitializationPerformance();
            System.out.println("Press any key to run performance test ...");
            System.in.read();
            test.testPerformance();
            System.out.println("Press any key to shutdown ...");
            System.in.read();
            test.tearDown();
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
        finally
        {
            ApplicationContextHelper.closeApplicationContext();
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
    
    /**
     * Add 50K objects into the transactional cache and checks that the first object added
     * has been discarded.
     */
    public void testMaxSizeOverrun() throws Exception
    {
        TransactionService transactionService = serviceRegistry.getTransactionService();
        UserTransaction txn = transactionService.getUserTransaction();
        try
        {
            txn.begin();
            
            Object startValue = new Integer(-1);
            String startKey = startValue.toString();
            transactionalCache.put(startKey, startValue);
            
            assertEquals("The start value isn't correct", startValue, transactionalCache.get(startKey));
            
            for (int i = 0; i < 205000; i++)
            {
                Object value = Integer.valueOf(i);
                String key = value.toString();
                transactionalCache.put(key, value);
            }
            
            // Is the start value here?
            Object checkStartValue = transactionalCache.get(startKey);
            // Now, the cache should no longer contain the first value
            assertNull("The start value didn't drop out of the cache", checkStartValue);
            
            txn.commit();
        }
        finally
        {
            try { txn.rollback(); } catch (Throwable ee) {}
        }
    }
    
    /** Execute the callback and ensure that the backing cache is left with the expected value */
    private void executeAndCheck(
            RetryingTransactionCallback<Object> callback,
            boolean readOnly,
            String key,
            Object expectedValue) throws Throwable
    {
        TransactionService transactionService = serviceRegistry.getTransactionService();
        UserTransaction txn = transactionService.getUserTransaction(readOnly);
        try
        {
            txn.begin();
            callback.execute();
            txn.commit();
        }
        finally
        {
            try { txn.rollback(); } catch (Throwable ee) {}
        }
        Object actualValue = backingCache.get(key);
        assertEquals("Backing cache value was not correct", expectedValue, actualValue);
    }
    
    private static final String COMMON_KEY = "A";
    /**
     * <ul>
     *   <li>Add to the transaction cache</li>
     *   <li>Add to the backing cache</li>
     *   <li>Commit</li>
     * </ul>
     */
    public void testConcurrentAddAgainstAdd()throws Throwable
    {
        RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                transactionalCache.put(COMMON_KEY, "AAA");
                backingCache.put(COMMON_KEY, "aaa");
                return null;
            }
        };
        transactionalCache.setMutable(true);
        executeAndCheck(callback, false, COMMON_KEY, null);
        executeAndCheck(callback, true, COMMON_KEY, "aaa");
        transactionalCache.setMutable(false);
        executeAndCheck(callback, false, COMMON_KEY, "AAA");
        executeAndCheck(callback, true, COMMON_KEY, "AAA");
    }
    /**
     * <ul>
     *   <li>Add to the transaction cache</li>
     *   <li>Add to the backing cache</li>
     *   <li>Commit</li>
     * </ul>
     */
    public void testConcurrentAddAgainstAddSame()throws Throwable
    {
        final Object commonValue = "AAA";
        RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                transactionalCache.put(COMMON_KEY, commonValue);
                backingCache.put(COMMON_KEY, commonValue);
                return null;
            }
        };
        transactionalCache.setMutable(true);
        executeAndCheck(callback, false, COMMON_KEY, "AAA");
        executeAndCheck(callback, true, COMMON_KEY, "AAA");
        transactionalCache.setMutable(false);
        executeAndCheck(callback, false, COMMON_KEY, commonValue);
        executeAndCheck(callback, true, COMMON_KEY, commonValue);
    }
    /**
     * <ul>
     *   <li>Add to the transaction cache</li>
     *   <li>Clear the backing cache</li>
     *   <li>Commit</li>
     * </ul>
     */
    public void testConcurrentAddAgainstClear()throws Throwable
    {
        RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                transactionalCache.put(COMMON_KEY, "AAA");
                backingCache.clear();
                return null;
            }
        };
        transactionalCache.setMutable(true);
        executeAndCheck(callback, false, COMMON_KEY, null);
        executeAndCheck(callback, true, COMMON_KEY, "AAA");
        transactionalCache.setMutable(false);
        executeAndCheck(callback, false, COMMON_KEY, "AAA");
        executeAndCheck(callback, true, COMMON_KEY, "AAA");
    }
    /**
     * <ul>
     *   <li>Add to the backing cache</li>
     *   <li>Update the transactional cache</li>
     *   <li>Update the backing cache</li>
     *   <li>Commit</li>
     * </ul>
     */
    public void testConcurrentUpdateAgainstUpdate()throws Throwable
    {
        RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                backingCache.put(COMMON_KEY, "aaa1");
                transactionalCache.put(COMMON_KEY, "AAA");
                backingCache.put(COMMON_KEY, "aaa2");
                return null;
            }
        };
        transactionalCache.setMutable(true);
        executeAndCheck(callback, false, COMMON_KEY, null);
        executeAndCheck(callback, true, COMMON_KEY, "aaa2");
        transactionalCache.setMutable(false);
        executeAndCheck(callback, false, COMMON_KEY, "AAA");
        executeAndCheck(callback, true, COMMON_KEY, "AAA");
    }
    /**
     * <ul>
     *   <li>Add to the backing cache</li>
     *   <li>Update the transactional cache</li>
     *   <li>Update the backing cache with a <tt>null</tt> value</li>
     *   <li>Commit</li>
     * </ul>
     */
    public void testConcurrentUpdateAgainstUpdateNull()throws Throwable
    {
        RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                backingCache.put(COMMON_KEY, "aaa1");
                transactionalCache.put(COMMON_KEY, "AAA");
                backingCache.put(COMMON_KEY, null);
                return null;
            }
        };
        transactionalCache.setMutable(true);
        executeAndCheck(callback, false, COMMON_KEY, null);
        executeAndCheck(callback, true, COMMON_KEY, null);
        transactionalCache.setMutable(false);
        executeAndCheck(callback, false, COMMON_KEY, "AAA");
        executeAndCheck(callback, true, COMMON_KEY, "AAA");
    }
    /**
     * <ul>
     *   <li>Add to the backing cache</li>
     *   <li>Update the transactional cache with a <tt>null</tt> value</li>
     *   <li>Update the backing cache</li>
     *   <li>Commit</li>
     * </ul>
     */
    public void testConcurrentUpdateNullAgainstUpdate()throws Throwable
    {
        RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                backingCache.put(COMMON_KEY, "aaa1");
                transactionalCache.put(COMMON_KEY, null);
                backingCache.put(COMMON_KEY, "aaa2");
                return null;
            }
        };
        transactionalCache.setMutable(true);
        executeAndCheck(callback, false, COMMON_KEY, null);
        executeAndCheck(callback, true, COMMON_KEY, "aaa2");
        transactionalCache.setMutable(false);
        executeAndCheck(callback, false, COMMON_KEY, null);
        executeAndCheck(callback, true, COMMON_KEY, null);
    }
    /**
     * <ul>
     *   <li>Add to the backing cache</li>
     *   <li>Update the transactional cache</li>
     *   <li>Remove from the backing cache</li>
     *   <li>Commit</li>
     * </ul>
     */
    public void testConcurrentUpdateAgainstRemove()throws Throwable
    {
        RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                backingCache.put(COMMON_KEY, "aaa1");
                transactionalCache.put(COMMON_KEY, "AAA");
                backingCache.remove(COMMON_KEY);
                return null;
            }
        };
        transactionalCache.setMutable(true);
        executeAndCheck(callback, false, COMMON_KEY, null);
        executeAndCheck(callback, true, COMMON_KEY, null);
        transactionalCache.setMutable(false);
        executeAndCheck(callback, false, COMMON_KEY, "AAA");
        executeAndCheck(callback, true, COMMON_KEY, "AAA");
    }
    /**
     * <ul>
     *   <li>Add to the backing cache</li>
     *   <li>Update the transactional cache</li>
     *   <li>Clear the backing cache</li>
     *   <li>Commit</li>
     * </ul>
     */
    public void testConcurrentUpdateAgainstClear()throws Throwable
    {
        RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                backingCache.put(COMMON_KEY, "aaa1");
                transactionalCache.put(COMMON_KEY, "AAA");
                backingCache.clear();
                return null;
            }
        };
        transactionalCache.setMutable(true);
        executeAndCheck(callback, false, COMMON_KEY, null);
        executeAndCheck(callback, true, COMMON_KEY, null);
        transactionalCache.setMutable(false);
        executeAndCheck(callback, false, COMMON_KEY, "AAA");
        executeAndCheck(callback, true, COMMON_KEY, "AAA");
    }
    /**
     * <ul>
     *   <li>Remove from the backing cache</li>
     *   <li>Remove from the transactional cache</li>
     *   <li>Add to the backing cache</li>
     *   <li>Commit</li>
     * </ul>
     */
    public void testConcurrentRemoveAgainstUpdate_NoPreExisting()throws Throwable
    {
        RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                backingCache.remove(COMMON_KEY);
                transactionalCache.remove(COMMON_KEY);
                backingCache.put(COMMON_KEY, "aaa2");
                return null;
            }
        };
        transactionalCache.setMutable(true);
        executeAndCheck(callback, false, COMMON_KEY, null);
        executeAndCheck(callback, true, COMMON_KEY, null);
        transactionalCache.setMutable(false);
        executeAndCheck(callback, false, COMMON_KEY, null);
        executeAndCheck(callback, true, COMMON_KEY, null);
    }
    /**
     * <ul>
     *   <li>Remove from the backing cache</li>
     *   <li>Add to the transactional cache</li>
     *   <li>Add to the backing cache</li>
     *   <li>Commit</li>
     * </ul>
     */
    public void testConcurrentAddAgainstAdd_NoPreExisting()throws Throwable
    {
        RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                backingCache.remove(COMMON_KEY);
                transactionalCache.put(COMMON_KEY, "aaa2-x");
                backingCache.put(COMMON_KEY, "aaa2");
                return null;
            }
        };
        transactionalCache.setMutable(true);
        executeAndCheck(callback, false, COMMON_KEY, null);
        executeAndCheck(callback, true, COMMON_KEY, "aaa2");    // Doesn't write through
        transactionalCache.setMutable(false);
        executeAndCheck(callback, false, COMMON_KEY, "aaa2-x"); // Always overwrites
        executeAndCheck(callback, true, COMMON_KEY, "aaa2-x");  // Always overwrites
    }
    /**
     * <ul>
     *   <li>Add to the backing cache</li>
     *   <li>Remove from the transactional cache</li>
     *   <li>Add to the backing cache</li>
     *   <li>Commit</li>
     * </ul>
     */
    public void testConcurrentRemoveAgainstUpdate_PreExisting()throws Throwable
    {
        RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                backingCache.put(COMMON_KEY, "aaa1");
                transactionalCache.remove(COMMON_KEY);
                backingCache.put(COMMON_KEY, "aaa2");
                return null;
            }
        };
        transactionalCache.setMutable(true);
        executeAndCheck(callback, false, COMMON_KEY, null);
        executeAndCheck(callback, true, COMMON_KEY, null);
        transactionalCache.setMutable(false);
        executeAndCheck(callback, false, COMMON_KEY, null);
        executeAndCheck(callback, true, COMMON_KEY, null);
    }
    /**
     * <ul>
     *   <li>Add to the backing cache</li>
     *   <li>Remove from the transactional cache</li>
     *   <li>Remove from the backing cache</li>
     *   <li>Commit</li>
     * </ul>
     */
    public void testConcurrentRemoveAgainstRemove()throws Throwable
    {
        RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                backingCache.put(COMMON_KEY, "aaa1");
                transactionalCache.remove(COMMON_KEY);
                backingCache.remove(COMMON_KEY);
                return null;
            }
        };
        transactionalCache.setMutable(true);
        executeAndCheck(callback, false, COMMON_KEY, null);
        executeAndCheck(callback, true, COMMON_KEY, null);
        transactionalCache.setMutable(false);
        executeAndCheck(callback, false, COMMON_KEY, null);
        executeAndCheck(callback, true, COMMON_KEY, null);
    }
    /**
     * <ul>
     *   <li>Add to the backing cache</li>
     *   <li>Remove from the transactional cache</li>
     *   <li>Clear the backing cache</li>
     *   <li>Commit</li>
     * </ul>
     */
    public void testConcurrentRemoveAgainstClear()throws Throwable
    {
        RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                backingCache.put(COMMON_KEY, "aaa1");
                transactionalCache.remove(COMMON_KEY);
                backingCache.clear();
                return null;
            }
        };
        transactionalCache.setMutable(true);
        executeAndCheck(callback, false, COMMON_KEY, null);
        executeAndCheck(callback, true, COMMON_KEY, null);
        transactionalCache.setMutable(false);
        executeAndCheck(callback, false, COMMON_KEY, null);
        executeAndCheck(callback, true, COMMON_KEY, null);
    }
}
