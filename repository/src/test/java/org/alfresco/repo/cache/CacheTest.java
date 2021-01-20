/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.cache;

import java.sql.SQLException;
import java.util.Collection;

import javax.transaction.Status;
import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.repo.cache.TransactionStats.OpType;
import org.alfresco.repo.cache.TransactionalCache.ValueHolder;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.testing.category.LuceneTests;
import org.apache.commons.lang3.mutable.MutableLong;
import org.junit.experimental.categories.Category;
import org.springframework.context.ApplicationContext;

/**
 * @see org.alfresco.repo.cache.TransactionalCache
 * 
 * @author Derek Hulley 
 */
@Category({OwnJVMTestsCategory.class, LuceneTests.class})
public class CacheTest extends TestCase
{
    private  ApplicationContext ctx;
    
    private ServiceRegistry serviceRegistry;
    private SimpleCache<String, Object> objectCache;
    private SimpleCache<String, ValueHolder<Object>> backingCache;
    private SimpleCache<String, ValueHolder<Object>> backingCacheNoStats;
    private TransactionalCache<String, Object> transactionalCache;
    private TransactionalCache<String, Object> transactionalCacheNoStats;
    private CacheStatistics cacheStats;
    
    @SuppressWarnings("unchecked")
    @Override
    public void setUp() throws Exception
    {
        ctx = ApplicationContextHelper.getApplicationContext(
                new String[] { "classpath:cache-test/cache-test-context.xml", ApplicationContextHelper.CONFIG_LOCATIONS[0] });
        if (AlfrescoTransactionSupport.getTransactionReadState() != TxnReadState.TXN_NONE)
        {
            fail("A transaction is still running");
        }
        
        serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        objectCache = (SimpleCache<String, Object>) ctx.getBean("objectCache");
        backingCache = (SimpleCache<String, ValueHolder<Object>>) ctx.getBean("backingCache");
        backingCacheNoStats = (SimpleCache<String, ValueHolder<Object>>) ctx.getBean("backingCacheNoStats");
        transactionalCache = (TransactionalCache<String, Object>) ctx.getBean("transactionalCache");
        transactionalCacheNoStats = (TransactionalCache<String, Object>) ctx.getBean("transactionalCacheNoStats");
        cacheStats = (CacheStatistics) ctx.getBean("cacheStatistics");
        // Make sure that the backing cache is empty
        backingCache.clear();
        backingCacheNoStats.clear();
        
        // Make the cache mutable (default)
        transactionalCache.setMutable(true);
        transactionalCache.setAllowEqualsChecks(false);
        
        transactionalCacheNoStats.setMutable(true);
        transactionalCacheNoStats.setAllowEqualsChecks(false);
    }
    
    @Override
    public void tearDown()
    {
        serviceRegistry = null;
        objectCache = null;
        backingCache = null;
        transactionalCache = null;
        backingCacheNoStats = null;
        transactionalCacheNoStats = null;
    }
    
    public void testSetUp() throws Exception
    {
        assertNotNull(serviceRegistry);
        assertNotNull(backingCache);
        assertNotNull(backingCacheNoStats);
        assertNotNull(objectCache);
        assertNotNull(transactionalCache);
        assertNotNull(transactionalCacheNoStats);
    }
    
    public void testObjectCache() throws Exception
    {
        objectCache.clear();
        
        objectCache.put("A", this);
        Object obj = objectCache.get("A");
        assertTrue("Object not cached properly", this == obj);

        objectCache.put("A", "AAA");
        assertEquals("AAA", objectCache.get("A"));
        
        Collection<String> keys = objectCache.getKeys();
        assertEquals("Cache didn't return correct number of keys", 1, keys.size());
        
        objectCache.remove("A");
        assertNull(objectCache.get("A"));
    }
    
    public void testTransactionalCacheNoTxn() throws Exception
    {
        String key = "B";
        String value = "BBB";
        // no transaction - do a put
        transactionalCache.put(key, value);
        // check that the value appears in the backing cache, backingCache
        assertEquals("Backing cache not used for put when no transaction present", value, TransactionalCache.getSharedCacheValue(backingCache, key, null));
        
        // remove the value from the backing cache and check that it is removed from the transaction cache
        backingCache.remove(key);
        assertNull("Backing cache not used for removed when no transaction present", transactionalCache.get(key));
        
        // add value into backing cache
        TransactionalCache.putSharedCacheValue(backingCache, key, value, null);
        // remove it from the transactional cache
        transactionalCache.remove(key);
        // check that it is gone from the backing cache
        assertNull("Non-transactional remove didn't go to backing cache", TransactionalCache.getSharedCacheValue(backingCache, key, null));
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
        TransactionalCache.putSharedCacheValue(backingCache, NEW_GLOBAL_ONE, NEW_GLOBAL_ONE, null);
        
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
        TransactionalCache.putSharedCacheValue(backingCache, NEW_GLOBAL_ONE, NEW_GLOBAL_ONE, null);
        TransactionalCache.putSharedCacheValue(backingCache, NEW_GLOBAL_TWO, NEW_GLOBAL_TWO, null);
        TransactionalCache.putSharedCacheValue(backingCache, NEW_GLOBAL_THREE, NEW_GLOBAL_THREE, null);
        
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
            
            // read 2 from the cache
            assertEquals("Item not read from backing cache", NEW_GLOBAL_TWO, transactionalCache.get(NEW_GLOBAL_TWO));
            // Change the backing cache
            TransactionalCache.putSharedCacheValue(backingCache, NEW_GLOBAL_TWO, NEW_GLOBAL_TWO + "-updated", null);
            // Ensure read-committed
            assertEquals("Read-committed not preserved", NEW_GLOBAL_TWO, transactionalCache.get(NEW_GLOBAL_TWO));
            
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
            assertNull("Item could still be fetched from backing cache", TransactionalCache.getSharedCacheValue(backingCache, NEW_GLOBAL_ONE, null));
            assertEquals("Item not updated in backing cache", "XXX", TransactionalCache.getSharedCacheValue(backingCache, UPDATE_TXN_THREE, null));
            
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
    
    public void testTransactionalCacheDisableSharedCaches() throws Throwable
    {
        // add item to global cache
        TransactionalCache.putSharedCacheValue(backingCache, NEW_GLOBAL_ONE, NEW_GLOBAL_ONE, null);
        TransactionalCache.putSharedCacheValue(backingCache, NEW_GLOBAL_TWO, NEW_GLOBAL_TWO, null);
        TransactionalCache.putSharedCacheValue(backingCache, NEW_GLOBAL_THREE, NEW_GLOBAL_THREE, null);
        
        TransactionService transactionService = serviceRegistry.getTransactionService();
        UserTransaction txn = transactionService.getUserTransaction();
        try
        {
            // begin a transaction
            txn.begin();
            
            // Go directly past ALL shared caches
            transactionalCache.setDisableSharedCacheReadForTransaction(true);
            
            // Try to get results in shared caches
            assertNull("Read of mutable shared cache MUST NOT use backing cache", transactionalCache.get(NEW_GLOBAL_ONE));
            assertNull("Value should not be in any cache", transactionalCache.get(UPDATE_TXN_THREE));
            
            // Update the transactional caches
            transactionalCache.put(NEW_GLOBAL_TWO, "An update");
            transactionalCache.put(UPDATE_TXN_THREE, UPDATE_TXN_THREE);
            
            // Try to get results in shared caches
            assertNull("Read of mutable shared cache MUST NOT use backing cache", transactionalCache.get(NEW_GLOBAL_ONE));
            assertEquals("Value should be in transactional cache", "An update", transactionalCache.get(NEW_GLOBAL_TWO));
            assertEquals("Value should be in transactional cache", UPDATE_TXN_THREE, transactionalCache.get(UPDATE_TXN_THREE));
            
            txn.commit();
            
            // Now check that values were not written through for any caches
            assertEquals("Out-of-txn read must return shared value", NEW_GLOBAL_ONE, transactionalCache.get(NEW_GLOBAL_ONE));
            assertNull("Value should be removed from shared cache", transactionalCache.get(NEW_GLOBAL_TWO));
            assertEquals("New values must be written to shared cache", UPDATE_TXN_THREE, transactionalCache.get(UPDATE_TXN_THREE));
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
            long timePlain = runPerformanceTestOnCache(objectCache, count);
            
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
                try { txn.rollback(); } catch (Throwable ee) {ee.printStackTrace();}
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
    public void testNullValue() throws Throwable
    {
        TransactionService transactionService = serviceRegistry.getTransactionService();
        UserTransaction txn = transactionService.getUserTransaction();

        txn.begin();
        
        TransactionalCache.putSharedCacheValue(backingCache, "A", null, null);
        transactionalCache.put("A", "AAA");
        
        try
        {
            txn.commit();
        }
        catch (Throwable e)
        {
            try {txn.rollback();} catch (Throwable ee) {}
            throw e;
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
            Object expectedValue,
            boolean mustContainKey) throws Throwable
    {
        if (expectedValue != null && !mustContainKey)
        {
            throw new IllegalArgumentException("Why have a value when the key should not be there?");
        }
        
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
        Object actualValue = TransactionalCache.getSharedCacheValue(backingCache, key, null);
        assertEquals("Backing cache value was not correct", expectedValue, actualValue);
        assertEquals("Backing cache contains(key): ", mustContainKey, backingCache.contains(key));
        
        // Clear the backing cache to ensure that subsequent tests don't run into existing data
        backingCache.clear();
    }
    
    private static final String DEFINITIVE_ONE = "def_one";
    private static final String DEFINITIVE_TWO = "def_two";
    private static final String DEFINITIVE_THREE = "def_three";
    
    /** Lock values and ensure they don't get modified */
    public void testValueLockingInTxn() throws Exception
    {
        // add item to global cache
        TransactionalCache.putSharedCacheValue(backingCache, DEFINITIVE_TWO, "initial_two", null);
        TransactionalCache.putSharedCacheValue(backingCache, DEFINITIVE_THREE, "initial_three", null);
        
        TransactionService transactionService = serviceRegistry.getTransactionService();
        UserTransaction txn = transactionService.getUserTransaction();
        try
        {
            // begin a transaction
            txn.begin();
            
            // Add
            {
                assertEquals(null, transactionalCache.get(DEFINITIVE_ONE));
                // Add it
                transactionalCache.put(DEFINITIVE_ONE, DEFINITIVE_ONE);
                assertFalse("Key should not be locked, yet.", transactionalCache.isValueLocked(DEFINITIVE_ONE));
                // Mark it as definitive
                transactionalCache.lockValue(DEFINITIVE_ONE);
                assertTrue("Key should be locked.", transactionalCache.isValueLocked(DEFINITIVE_ONE));
                // Attempt update
                transactionalCache.put(DEFINITIVE_ONE, "update_one");
                assertEquals("Update values should be locked.", DEFINITIVE_ONE, transactionalCache.get(DEFINITIVE_ONE));
            }
            
            // Update
            {
                assertEquals("initial_two", transactionalCache.get(DEFINITIVE_TWO));
                // Update it
                transactionalCache.put(DEFINITIVE_TWO, DEFINITIVE_TWO);
                assertFalse("Key should not be locked, yet.", transactionalCache.isValueLocked(DEFINITIVE_TWO));
                // Mark it as definitive
                transactionalCache.lockValue(DEFINITIVE_TWO);
                assertTrue("Key should be locked.", transactionalCache.isValueLocked(DEFINITIVE_TWO));
                // Attempt update
                transactionalCache.put(DEFINITIVE_TWO, "update_two");
                assertEquals("Update values should be locked.", DEFINITIVE_TWO, transactionalCache.get(DEFINITIVE_TWO));
                // Attempt removal
                transactionalCache.remove(DEFINITIVE_TWO);
                assertEquals("Update values should be locked.", DEFINITIVE_TWO, transactionalCache.get(DEFINITIVE_TWO));
            }
            
            // Remove
            {
                assertEquals("initial_three", transactionalCache.get(DEFINITIVE_THREE));
                // Remove it
                transactionalCache.remove(DEFINITIVE_THREE);
                assertFalse("Key should not be locked, yet.", transactionalCache.isValueLocked(DEFINITIVE_THREE));
                // Mark it as definitive
                transactionalCache.lockValue(DEFINITIVE_THREE);
                assertTrue("Key should be locked.", transactionalCache.isValueLocked(DEFINITIVE_THREE));
                // Attempt update
                transactionalCache.put(DEFINITIVE_THREE, "add_three");
                assertEquals("Removal should be locked.", null, transactionalCache.get(DEFINITIVE_THREE));
            }
            
            txn.commit();

            // Check post-commit values
            assertEquals("Definitive change not written through.", DEFINITIVE_ONE, TransactionalCache.getSharedCacheValue(backingCache, DEFINITIVE_ONE, null));
            assertEquals("Definitive change not written through.", DEFINITIVE_TWO, TransactionalCache.getSharedCacheValue(backingCache, DEFINITIVE_TWO, null));
            assertEquals("Definitive change not written through.", null, TransactionalCache.getSharedCacheValue(backingCache, DEFINITIVE_THREE, null));
        }
        finally
        {
            try { txn.rollback(); } catch (Throwable ee) {}
        }
    }
    
    private static final String COMMON_KEY = "A";
    private static final MutableLong VALUE_ONE_A = new MutableLong(1L);
    private static final MutableLong VALUE_ONE_B = new MutableLong(1L);
    private static final MutableLong VALUE_TWO_A = new MutableLong(2L);
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
                transactionalCache.put(COMMON_KEY, VALUE_ONE_A);
                TransactionalCache.putSharedCacheValue(backingCache, COMMON_KEY, VALUE_ONE_B, null);
                return null;
            }
        };
        transactionalCache.setAllowEqualsChecks(false);
        transactionalCache.setMutable(true);
        executeAndCheck(callback, false, COMMON_KEY, null, false);  // Mutable: Pessimistic removal
        executeAndCheck(callback, true, COMMON_KEY, null, false);   // Mutable: Pessimistic removal
        transactionalCache.setMutable(false);
        executeAndCheck(callback, false, COMMON_KEY, VALUE_ONE_B, true);  // Immutable: Assume backing cache is correct
        executeAndCheck(callback, true, COMMON_KEY, VALUE_ONE_B, true);   // Immutable: Assume backing cache is correct

        transactionalCache.setAllowEqualsChecks(true);
        transactionalCache.setMutable(true);
        executeAndCheck(callback, false, COMMON_KEY, VALUE_ONE_B, true);  // Mutable: Shared cache value checked
        executeAndCheck(callback, true, COMMON_KEY, VALUE_ONE_B, true);   // Mutable: Shared cache value checked
        transactionalCache.setMutable(false);
        executeAndCheck(callback, false, COMMON_KEY, VALUE_ONE_B, true);  // Immutable: Assume backing cache is correct
        executeAndCheck(callback, true, COMMON_KEY, VALUE_ONE_B, true);   // Immutable: Assume backing cache is correct
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
        RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                transactionalCache.put(COMMON_KEY, VALUE_ONE_A);
                TransactionalCache.putSharedCacheValue(backingCache, COMMON_KEY, VALUE_ONE_A, null);
                return null;
            }
        };
        transactionalCache.setAllowEqualsChecks(false);
        transactionalCache.setMutable(true);
        executeAndCheck(callback, false, COMMON_KEY, null, false);         // Mutable: No equality check
        executeAndCheck(callback, true, COMMON_KEY, null, false);          // Mutable: No equality check
        transactionalCache.setMutable(false);
        executeAndCheck(callback, false, COMMON_KEY, VALUE_ONE_A, true);  // Immutable: Assumed to be same
        executeAndCheck(callback, true, COMMON_KEY, VALUE_ONE_A, true);   // Immutable: Assumed to be same

        transactionalCache.setAllowEqualsChecks(true);
        transactionalCache.setMutable(true);
        executeAndCheck(callback, false, COMMON_KEY, VALUE_ONE_A, true);  // Mutable: Equality check done
        executeAndCheck(callback, true, COMMON_KEY, VALUE_ONE_A, true);   // Mutable: Equality check done
        transactionalCache.setMutable(false);
        executeAndCheck(callback, false, COMMON_KEY, VALUE_ONE_A, true);  // Immutable: Assumed to be same
        executeAndCheck(callback, true, COMMON_KEY, VALUE_ONE_A, true);   // Immutable: Assumed to be same
    }
    /**
     * <ul>
     *   <li>Add to the transaction cache</li>
     *   <li>Add <tt>null</tt> to the backing cache</li>
     *   <li>Commit</li>
     * </ul>
     */
    public void testConcurrentAddAgainstAddNull()throws Throwable
    {
        RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                transactionalCache.put(COMMON_KEY, VALUE_ONE_A);
                TransactionalCache.putSharedCacheValue(backingCache, COMMON_KEY, null, null);
                return null;
            }
        };
        transactionalCache.setAllowEqualsChecks(false);
        transactionalCache.setMutable(true);
        executeAndCheck(callback, false, COMMON_KEY, null, false);          // Mutable: No equality check
        executeAndCheck(callback, true, COMMON_KEY, null, false);           // Mutable: No equality check
        transactionalCache.setMutable(false);
        executeAndCheck(callback, false, COMMON_KEY, null, true);           // Immutable: Assume backing cache is correct
        executeAndCheck(callback, true, COMMON_KEY, null, true);            // Immutable: Assume backing cache is correct
        
        transactionalCache.setAllowEqualsChecks(true);
        transactionalCache.setMutable(true);
        executeAndCheck(callback, false, COMMON_KEY, null, false);          // Mutable: Equality check done
        executeAndCheck(callback, true, COMMON_KEY, null, false);           // Mutable: Equality check done
        transactionalCache.setMutable(false);
        executeAndCheck(callback, false, COMMON_KEY, null, true);           // Immutable: Assume backing cache is correct
        executeAndCheck(callback, true, COMMON_KEY, null, true);            // Immutable: Assume backing cache is correct
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
                transactionalCache.put(COMMON_KEY, VALUE_ONE_A);
                backingCache.clear();
                return null;
            }
        };
        transactionalCache.setAllowEqualsChecks(false);
        transactionalCache.setMutable(true);
        executeAndCheck(callback, false, COMMON_KEY, VALUE_ONE_A, true);  // Mutable: Add back to backing cache
        executeAndCheck(callback, true, COMMON_KEY, VALUE_ONE_A, true);   // Mutable: Add back to backing cache
        transactionalCache.setMutable(false);
        executeAndCheck(callback, false, COMMON_KEY, VALUE_ONE_A, true);  // Immutable: Add back to backing cache
        executeAndCheck(callback, true, COMMON_KEY, VALUE_ONE_A, true);   // Immutable: Add back to backing cache

        transactionalCache.setAllowEqualsChecks(true);
        transactionalCache.setMutable(true);
        executeAndCheck(callback, false, COMMON_KEY, VALUE_ONE_A, true);  // Mutable: Add back to backing cache
        executeAndCheck(callback, true, COMMON_KEY, VALUE_ONE_A, true);   // Mutable: Add back to backing cache
        transactionalCache.setMutable(false);
        executeAndCheck(callback, false, COMMON_KEY, VALUE_ONE_A, true);  // Immutable: Add back to backing cache
        executeAndCheck(callback, true, COMMON_KEY, VALUE_ONE_A, true);   // Immutable: Add back to backing cache
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
                TransactionalCache.putSharedCacheValue(backingCache, COMMON_KEY, VALUE_ONE_A, null);
                transactionalCache.put(COMMON_KEY, VALUE_ONE_B);
                TransactionalCache.putSharedCacheValue(backingCache, COMMON_KEY, VALUE_TWO_A, null);
                return null;
            }
        };
        transactionalCache.setAllowEqualsChecks(false);
        transactionalCache.setMutable(true);
        executeAndCheck(callback, false, COMMON_KEY, null, false);         // Mutable: Pessimistic removal
        executeAndCheck(callback, true, COMMON_KEY, null, false);          // Mutable: Pessimistic removal
        transactionalCache.setMutable(false);
        executeAndCheck(callback, false, COMMON_KEY, VALUE_TWO_A, true);  // Immutable: Assume backing cache is correct
        executeAndCheck(callback, true, COMMON_KEY, VALUE_TWO_A, true);   // Immutable: Assume backing cache is correct

        transactionalCache.setAllowEqualsChecks(true);
        transactionalCache.setMutable(true);
        executeAndCheck(callback, false, COMMON_KEY, null, false);         // Mutable: Shared cache value checked failed
        executeAndCheck(callback, true, COMMON_KEY, null, false);          // Mutable: Shared cache value checked failed
        transactionalCache.setMutable(false);
        executeAndCheck(callback, false, COMMON_KEY, VALUE_TWO_A, true);  // Immutable: Assume backing cache is correct
        executeAndCheck(callback, true, COMMON_KEY, VALUE_TWO_A, true);   // Immutable: Assume backing cache is correct
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
                TransactionalCache.putSharedCacheValue(backingCache, COMMON_KEY, VALUE_ONE_A, null);
                transactionalCache.put(COMMON_KEY, VALUE_ONE_B);
                TransactionalCache.putSharedCacheValue(backingCache, COMMON_KEY, null, null);
                return null;
            }
        };
        transactionalCache.setAllowEqualsChecks(false);
        transactionalCache.setMutable(true);
        executeAndCheck(callback, false, COMMON_KEY, null, false);          // Mutable: Pessimistic removal
        executeAndCheck(callback, true, COMMON_KEY, null, false);           // Mutable: Pessimistic removal
        transactionalCache.setMutable(false);
        executeAndCheck(callback, false, COMMON_KEY, null, true);           // Immutable: Assume backing cache is correct
        executeAndCheck(callback, true, COMMON_KEY, null, true);            // Immutable: Assume backing cache is correct

        transactionalCache.setAllowEqualsChecks(true);
        transactionalCache.setMutable(true);
        executeAndCheck(callback, false, COMMON_KEY, null, false);          // Mutable: Pessimistic removal
        executeAndCheck(callback, true, COMMON_KEY, null, false);           // Mutable: Pessimistic removal
        transactionalCache.setMutable(false);
        executeAndCheck(callback, false, COMMON_KEY, null, true);           // Immutable: Assume backing cache is correct
        executeAndCheck(callback, true, COMMON_KEY, null, true);            // Immutable: Assume backing cache is correct
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
                TransactionalCache.putSharedCacheValue(backingCache, COMMON_KEY, VALUE_ONE_A, null);
                transactionalCache.put(COMMON_KEY, null);
                TransactionalCache.putSharedCacheValue(backingCache, COMMON_KEY, VALUE_ONE_B, null);
                return null;
            }
        };
        transactionalCache.setAllowEqualsChecks(false);
        transactionalCache.setMutable(true);
        executeAndCheck(callback, false, COMMON_KEY, null, false);          // Mutable: Pessimistic removal
        executeAndCheck(callback, true, COMMON_KEY, null, false);           // Mutable: Pessimistic removal
        transactionalCache.setMutable(false);
        executeAndCheck(callback, false, COMMON_KEY, VALUE_ONE_B, true);    // Immutable: Assume backing cache is correct
        executeAndCheck(callback, true, COMMON_KEY, VALUE_ONE_B, true);     // Immutable: Assume backing cache is correct

        transactionalCache.setAllowEqualsChecks(true);
        transactionalCache.setMutable(true);
        executeAndCheck(callback, false, COMMON_KEY, null, false);          // Mutable: Pessimistic removal
        executeAndCheck(callback, true, COMMON_KEY, null, false);           // Mutable: Pessimistic removal
        transactionalCache.setMutable(false);
        executeAndCheck(callback, false, COMMON_KEY, VALUE_ONE_B, true);    // Immutable: Assume backing cache is correct
        executeAndCheck(callback, true, COMMON_KEY, VALUE_ONE_B, true);     // Immutable: Assume backing cache is correct
    }
    /**
     * <ul>
     *   <li>Add to the backing cache</li>
     *   <li>Update the transactional cache with a <tt>null</tt> value</li>
     *   <li>Update the backing cache with a <tt>null</tt> value</li>
     *   <li>Commit</li>
     * </ul>
     */
    public void testConcurrentUpdateNullAgainstUpdateNull()throws Throwable
    {
        RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                TransactionalCache.putSharedCacheValue(backingCache, COMMON_KEY, VALUE_ONE_A, null);
                transactionalCache.put(COMMON_KEY, null);
                TransactionalCache.putSharedCacheValue(backingCache, COMMON_KEY, null, null);
                return null;
            }
        };
        transactionalCache.setAllowEqualsChecks(false);
        transactionalCache.setMutable(true);
        executeAndCheck(callback, false, COMMON_KEY, null, false);          // Mutable: Pessimistic removal
        executeAndCheck(callback, true, COMMON_KEY, null, false);           // Mutable: Pessimistic removal
        transactionalCache.setMutable(false);
        executeAndCheck(callback, false, COMMON_KEY, null, true);           // Immutable: Assume backing cache is correct
        executeAndCheck(callback, true, COMMON_KEY, null, true);            // Immutable: Assume backing cache is correct

        transactionalCache.setAllowEqualsChecks(true);
        transactionalCache.setMutable(true);
        executeAndCheck(callback, false, COMMON_KEY, null, true);           // Mutable: Equality check
        executeAndCheck(callback, true, COMMON_KEY, null, true);            // Mutable: Equality check
        transactionalCache.setMutable(false);
        executeAndCheck(callback, false, COMMON_KEY, null, true);           // Immutable: Equality check
        executeAndCheck(callback, true, COMMON_KEY, null, true);            // Immutable: Equality check
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
                TransactionalCache.putSharedCacheValue(backingCache, COMMON_KEY, VALUE_ONE_A, null);
                transactionalCache.put(COMMON_KEY, VALUE_ONE_B);
                backingCache.remove(COMMON_KEY);
                return null;
            }
        };
        transactionalCache.setAllowEqualsChecks(false);
        transactionalCache.setMutable(true);
        executeAndCheck(callback, false, COMMON_KEY, null, false);             // Mutable: Pessimistic removal
        executeAndCheck(callback, true, COMMON_KEY, null, false);              // Mutable: Pessimistic removal
        transactionalCache.setMutable(false);
        executeAndCheck(callback, false, COMMON_KEY, VALUE_ONE_B, true);      // Immutable: Add back to backing cache
        executeAndCheck(callback, true, COMMON_KEY, VALUE_ONE_B, true);       // Immutable: Add back to backing cache

        transactionalCache.setAllowEqualsChecks(true);
        transactionalCache.setMutable(true);
        executeAndCheck(callback, false, COMMON_KEY, null, false);             // Mutable: Pessimistic removal
        executeAndCheck(callback, true, COMMON_KEY, null, false);              // Mutable: Pessimistic removal
        transactionalCache.setMutable(false);
        executeAndCheck(callback, false, COMMON_KEY, VALUE_ONE_B, true);      // Immutable: Add back to backing cache
        executeAndCheck(callback, true, COMMON_KEY, VALUE_ONE_B, true);       // Immutable: Add back to backing cache
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
                TransactionalCache.putSharedCacheValue(backingCache, COMMON_KEY, VALUE_ONE_A, null);
                transactionalCache.put(COMMON_KEY, VALUE_ONE_B);
                backingCache.clear();
                return null;
            }
        };
        transactionalCache.setAllowEqualsChecks(false);
        transactionalCache.setMutable(true);
        executeAndCheck(callback, false, COMMON_KEY, null, false);             // Mutable: Pessimistic removal
        executeAndCheck(callback, true, COMMON_KEY, null, false);              // Mutable: Pessimistic removal
        transactionalCache.setMutable(false);
        executeAndCheck(callback, false, COMMON_KEY, VALUE_ONE_B, true);      // Immutable: Add back to backing cache
        executeAndCheck(callback, true, COMMON_KEY, VALUE_ONE_B, true);       // Immutable: Add back to backing cache

        transactionalCache.setAllowEqualsChecks(true);
        transactionalCache.setMutable(true);
        executeAndCheck(callback, false, COMMON_KEY, null, false);             // Mutable: Pessimistic removal
        executeAndCheck(callback, true, COMMON_KEY, null, false);              // Mutable: Pessimistic removal
        transactionalCache.setMutable(false);
        executeAndCheck(callback, false, COMMON_KEY, VALUE_ONE_B, true);      // Immutable: Add back to backing cache
        executeAndCheck(callback, true, COMMON_KEY, VALUE_ONE_B, true);       // Immutable: Add back to backing cache
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
                TransactionalCache.putSharedCacheValue(backingCache, COMMON_KEY, VALUE_ONE_B, null);
                return null;
            }
        };
        transactionalCache.setAllowEqualsChecks(false);
        transactionalCache.setMutable(true);
        executeAndCheck(callback, false, COMMON_KEY, null, false);             // Mutable: Pessimistic removal
        executeAndCheck(callback, true, COMMON_KEY, null, false);              // Mutable: Pessimistic removal
        transactionalCache.setMutable(false);
        executeAndCheck(callback, false, COMMON_KEY, null, false);             // Immutable: Remove from backing cache
        executeAndCheck(callback, true, COMMON_KEY, null, false);              // Immutable: Remove from backing cache

        transactionalCache.setAllowEqualsChecks(true);
        transactionalCache.setMutable(true);
        executeAndCheck(callback, false, COMMON_KEY, null, false);             // Mutable: Pessimistic removal
        executeAndCheck(callback, true, COMMON_KEY, null, false);              // Mutable: Pessimistic removal
        transactionalCache.setMutable(false);
        executeAndCheck(callback, false, COMMON_KEY, null, false);             // Immutable: Remove from backing cache
        executeAndCheck(callback, true, COMMON_KEY, null, false);              // Immutable: Remove from backing cache
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
                transactionalCache.put(COMMON_KEY, VALUE_ONE_A);
                TransactionalCache.putSharedCacheValue(backingCache, COMMON_KEY, VALUE_ONE_B, null);
                return null;
            }
        };
        transactionalCache.setAllowEqualsChecks(false);
        transactionalCache.setMutable(true);
        executeAndCheck(callback, false, COMMON_KEY, null, false);             // Mutable: Pessimistic removal
        executeAndCheck(callback, true, COMMON_KEY, null, false);              // Mutable: Pessimistic removal
        transactionalCache.setMutable(false);
        executeAndCheck(callback, false, COMMON_KEY, VALUE_ONE_B, true);      // Immutable: Assume backing cache is correct
        executeAndCheck(callback, true, COMMON_KEY, VALUE_ONE_B, true);       // Immutable: Assume backing cache is correct

        transactionalCache.setAllowEqualsChecks(true);
        transactionalCache.setMutable(true);
        executeAndCheck(callback, false, COMMON_KEY, VALUE_ONE_B, true);      // Mutable: Shared cache value checked
        executeAndCheck(callback, true, COMMON_KEY, VALUE_ONE_B, true);       // Mutable: Shared cache value checked
        transactionalCache.setMutable(false);
        executeAndCheck(callback, false, COMMON_KEY, VALUE_ONE_B, true);      // Immutable: Assume backing cache is correct
        executeAndCheck(callback, true, COMMON_KEY, VALUE_ONE_B, true);       // Immutable: Assume backing cache is correct
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
                TransactionalCache.putSharedCacheValue(backingCache, COMMON_KEY, VALUE_ONE_A, null);
                transactionalCache.remove(COMMON_KEY);
                TransactionalCache.putSharedCacheValue(backingCache, COMMON_KEY, VALUE_ONE_B, null);
                return null;
            }
        };
        transactionalCache.setAllowEqualsChecks(false);
        transactionalCache.setMutable(true);
        executeAndCheck(callback, false, COMMON_KEY, null, false);             // Mutable: Pessimistic removal
        executeAndCheck(callback, true, COMMON_KEY, null, false);              // Mutable: Pessimistic removal
        transactionalCache.setMutable(false);
        executeAndCheck(callback, false, COMMON_KEY, null, false);             // Immutable: Remove from backing cache
        executeAndCheck(callback, true, COMMON_KEY, null, false);              // Immutable: Remove from backing cache

        transactionalCache.setAllowEqualsChecks(true);
        transactionalCache.setMutable(true);
        executeAndCheck(callback, false, COMMON_KEY, null, false);             // Mutable: Pessimistic removal
        executeAndCheck(callback, true, COMMON_KEY, null, false);              // Mutable: Pessimistic removal
        transactionalCache.setMutable(false);
        executeAndCheck(callback, false, COMMON_KEY, null, false);             // Immutable: Remove from backing cache
        executeAndCheck(callback, true, COMMON_KEY, null, false);              // Immutable: Remove from backing cache
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
                TransactionalCache.putSharedCacheValue(backingCache, COMMON_KEY, VALUE_ONE_A, null);
                transactionalCache.remove(COMMON_KEY);
                backingCache.remove(COMMON_KEY);
                return null;
            }
        };
        transactionalCache.setAllowEqualsChecks(false);
        transactionalCache.setMutable(true);
        executeAndCheck(callback, false, COMMON_KEY, null, false);             // Mutable: Remove from backing cache
        executeAndCheck(callback, true, COMMON_KEY, null, false);              // Mutable: Remove from backing cache
        transactionalCache.setMutable(false);
        executeAndCheck(callback, false, COMMON_KEY, null, false);             // Immutable: Remove from backing cache
        executeAndCheck(callback, true, COMMON_KEY, null, false);              // Immutable: Remove from backing cache

        transactionalCache.setAllowEqualsChecks(true);
        transactionalCache.setMutable(true);
        executeAndCheck(callback, false, COMMON_KEY, null, false);             // Mutable: Remove from backing cache
        executeAndCheck(callback, true, COMMON_KEY, null, false);              // Mutable: Remove from backing cache
        transactionalCache.setMutable(false);
        executeAndCheck(callback, false, COMMON_KEY, null, false);             // Immutable: Remove from backing cache
        executeAndCheck(callback, true, COMMON_KEY, null, false);              // Immutable: Remove from backing cache
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
                TransactionalCache.putSharedCacheValue(backingCache, COMMON_KEY, VALUE_ONE_A, null);
                transactionalCache.remove(COMMON_KEY);
                backingCache.clear();
                return null;
            }
        };
        transactionalCache.setAllowEqualsChecks(false);
        transactionalCache.setMutable(true);
        executeAndCheck(callback, false, COMMON_KEY, null, false);             // Mutable: Nothing to do
        executeAndCheck(callback, true, COMMON_KEY, null, false);              // Mutable: Nothing to do
        transactionalCache.setMutable(false);
        executeAndCheck(callback, false, COMMON_KEY, null, false);             // Immutable: Nothing to do
        executeAndCheck(callback, true, COMMON_KEY, null, false);              // Immutable: Nothing to do

        transactionalCache.setAllowEqualsChecks(true);
        transactionalCache.setMutable(true);
        executeAndCheck(callback, false, COMMON_KEY, null, false);             // Mutable: Nothing to do
        executeAndCheck(callback, true, COMMON_KEY, null, false);              // Mutable: Nothing to do
        transactionalCache.setMutable(false);
        executeAndCheck(callback, false, COMMON_KEY, null, false);             // Immutable: Nothing to do
        executeAndCheck(callback, true, COMMON_KEY, null, false);              // Immutable: Nothing to do
    }
    
    public void testTransactionalCacheStatsOnCommit() throws Throwable
    {
        // add item to global cache
        TransactionalCache.putSharedCacheValue(backingCache, "stats-test1", "v", null);
        TransactionalCache.putSharedCacheValue(backingCache, "stats-test2", "v", null);
        TransactionalCache.putSharedCacheValue(backingCache, "stats-test3", "v", null);
        
        
        TransactionService transactionService = serviceRegistry.getTransactionService();
        UserTransaction txn = transactionService.getUserTransaction();
        
        final long hitsAtStart = cacheStats.count("transactionalCache", OpType.GET_HIT);
        final long missesAtStart = cacheStats.count("transactionalCache", OpType.GET_MISS);
        final long putsAtStart = cacheStats.count("transactionalCache", OpType.PUT);
        final long removesAtStart = cacheStats.count("transactionalCache", OpType.REMOVE);
        final long clearsAtStart = cacheStats.count("transactionalCache", OpType.CLEAR);
        
        try
        {
            // begin a transaction
            txn.begin();
            
            // Perform some puts
            transactionalCache.put("stats-test4", "v");
            transactionalCache.put("stats-test5", "v");
            transactionalCache.put("stats-test6", "v");
            transactionalCache.put("stats-test7", "v");
            transactionalCache.put("stats-test8", "v");

            // Perform some gets...
            // hits
            transactionalCache.get("stats-test3");
            transactionalCache.get("stats-test2");
            transactionalCache.get("stats-test1");
            // repeated hits won't touch the shared cache
            transactionalCache.get("stats-test2");
            transactionalCache.get("stats-test1");
            // misses - not yet committed
            transactionalCache.get("stats-miss1");
            transactionalCache.get("stats-miss2");
            transactionalCache.get("stats-miss3");
            transactionalCache.get("stats-miss4");
            // repeated misses won't touch the shared cache
            transactionalCache.get("stats-miss2");
            transactionalCache.get("stats-miss3");

            // Perform some removals
            transactionalCache.remove("stats-test1");
            transactionalCache.remove("stats-test2");
            transactionalCache.remove("stats-test3");
            transactionalCache.remove("stats-test9");
            transactionalCache.remove("stats-test10");
            transactionalCache.remove("stats-test11");
            transactionalCache.remove("stats-test12");
            transactionalCache.remove("stats-test13");
            
            // Check nothing has changed yet - changes not written through until commit or rollback
            assertEquals(hitsAtStart, cacheStats.count("transactionalCache", OpType.GET_HIT));
            assertEquals(missesAtStart, cacheStats.count("transactionalCache", OpType.GET_MISS));
            assertEquals(putsAtStart, cacheStats.count("transactionalCache", OpType.PUT));
            assertEquals(removesAtStart, cacheStats.count("transactionalCache", OpType.REMOVE));
            assertEquals(clearsAtStart, cacheStats.count("transactionalCache", OpType.CLEAR));
            
            // commit the transaction
            txn.commit();

            // TODO: remove is called twice for each remove (in beforeCommit and afterCommit) - check this is correct.
            assertEquals(removesAtStart+16, cacheStats.count("transactionalCache", OpType.REMOVE));
            assertEquals(hitsAtStart+3, cacheStats.count("transactionalCache", OpType.GET_HIT));
            assertEquals(missesAtStart+4, cacheStats.count("transactionalCache", OpType.GET_MISS));
            assertEquals(putsAtStart+5, cacheStats.count("transactionalCache", OpType.PUT));
            // Performing a clear would affect the other stats, so a separate test is required.
            assertEquals(clearsAtStart+0, cacheStats.count("transactionalCache", OpType.CLEAR));
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
    
    public void testTransactionalCacheStatsDisabled() throws Throwable
    {
        // add item to global cache
        TransactionalCache.putSharedCacheValue(backingCacheNoStats, "stats-test1", "v", null);
        TransactionalCache.putSharedCacheValue(backingCacheNoStats, "stats-test2", "v", null);
        TransactionalCache.putSharedCacheValue(backingCacheNoStats, "stats-test3", "v", null);
        
        
        TransactionService transactionService = serviceRegistry.getTransactionService();
        UserTransaction txn = transactionService.getUserTransaction();
        
        for (OpType opType : OpType.values())
        {
            try
            {
                cacheStats.count("transactionalCacheNoStats", opType);
                fail("Expected NoStatsForCache error.");
            }
            catch(NoStatsForCache e)
            {
                // Good
            }
        }
        
        try
        {
            // begin a transaction
            txn.begin();
            
            // Perform some puts
            transactionalCacheNoStats.put("stats-test4", "v");
            transactionalCache.put("stats-test5", "v");
            transactionalCache.put("stats-test6", "v");
            transactionalCache.put("stats-test7", "v");
            transactionalCache.put("stats-test8", "v");

            // Perform some gets...
            // hits
            transactionalCache.get("stats-test3");
            transactionalCache.get("stats-test2");
            transactionalCache.get("stats-test1");
            // repeated hits won't touch the shared cache
            transactionalCache.get("stats-test2");
            transactionalCache.get("stats-test1");
            // misses - not yet committed
            transactionalCache.get("stats-miss1");
            transactionalCache.get("stats-miss2");
            transactionalCache.get("stats-miss3");
            transactionalCache.get("stats-miss4");
            // repeated misses won't touch the shared cache
            transactionalCache.get("stats-miss2");
            transactionalCache.get("stats-miss3");

            // Perform some removals
            transactionalCache.remove("stats-test1");
            transactionalCache.remove("stats-test2");
            transactionalCache.remove("stats-test3");
            transactionalCache.remove("stats-test9");
            transactionalCache.remove("stats-test10");
            transactionalCache.remove("stats-test11");
            transactionalCache.remove("stats-test12");
            transactionalCache.remove("stats-test13");
            
            // Check nothing has changed - changes not written through until commit or rollback
            for (OpType opType : OpType.values())
            {
                try
                {
                    cacheStats.count("transactionalCacheNoStats", opType);
                    fail("Expected NoStatsForCache error.");
                }
                catch(NoStatsForCache e)
                {
                    // Good
                }
            }
            
            // commit the transaction
            txn.commit();

            // Post-commit, nothing should have changed.
            for (OpType opType : OpType.values())
            {
                try
                {
                    cacheStats.count("transactionalCacheNoStats", opType);
                    fail("Expected NoStatsForCache error.");
                }
                catch(NoStatsForCache e)
                {
                    // Good
                }
            }
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
    

    public void testTransactionalCacheStatsForClears() throws Throwable
    {
        // add item to global cache
        TransactionalCache.putSharedCacheValue(backingCache, "stats-test1", "v", null);
        TransactionalCache.putSharedCacheValue(backingCache, "stats-test2", "v", null);
        TransactionalCache.putSharedCacheValue(backingCache, "stats-test3", "v", null);
        
        
        TransactionService transactionService = serviceRegistry.getTransactionService();
        UserTransaction txn = transactionService.getUserTransaction();
        
        final long hitsAtStart = cacheStats.count("transactionalCache", OpType.GET_HIT);
        final long missesAtStart = cacheStats.count("transactionalCache", OpType.GET_MISS);
        final long putsAtStart = cacheStats.count("transactionalCache", OpType.PUT);
        final long removesAtStart = cacheStats.count("transactionalCache", OpType.REMOVE);
        final long clearsAtStart = cacheStats.count("transactionalCache", OpType.CLEAR);
        
        try
        {
            // begin a transaction
            txn.begin();
            
            // Perform some puts
            transactionalCache.put("stats-test4", "v");
            transactionalCache.put("stats-test5", "v");
            transactionalCache.put("stats-test6", "v");
            transactionalCache.put("stats-test7", "v");
            transactionalCache.put("stats-test8", "v");

            // Perform some gets...
            // hits
            transactionalCache.get("stats-test3");
            transactionalCache.get("stats-test2");
            transactionalCache.get("stats-test1");
            // repeated hits won't touch the shared cache
            transactionalCache.get("stats-test2");
            transactionalCache.get("stats-test1");
            // misses - not yet committed
            transactionalCache.get("stats-miss1");
            transactionalCache.get("stats-miss2");
            transactionalCache.get("stats-miss3");
            transactionalCache.get("stats-miss4");
            // repeated misses won't touch the shared cache
            transactionalCache.get("stats-miss2");
            transactionalCache.get("stats-miss3");

            // Perform some removals
            transactionalCache.remove("stats-test1");
            transactionalCache.remove("stats-test2");
            transactionalCache.remove("stats-test3");
            transactionalCache.remove("stats-test9");
            transactionalCache.remove("stats-test10");
            transactionalCache.remove("stats-test11");
            transactionalCache.remove("stats-test12");
            transactionalCache.remove("stats-test13");
            
            // Perform some clears
            transactionalCache.clear();
            transactionalCache.clear();
            
            // Check nothing has changed yet - changes not written through until commit or rollback
            assertEquals(hitsAtStart, cacheStats.count("transactionalCache", OpType.GET_HIT));
            assertEquals(missesAtStart, cacheStats.count("transactionalCache", OpType.GET_MISS));
            assertEquals(putsAtStart, cacheStats.count("transactionalCache", OpType.PUT));
            assertEquals(removesAtStart, cacheStats.count("transactionalCache", OpType.REMOVE));
            assertEquals(clearsAtStart, cacheStats.count("transactionalCache", OpType.CLEAR));
            
            // commit the transaction
            txn.commit();

            assertEquals(clearsAtStart+2, cacheStats.count("transactionalCache", OpType.CLEAR));
            // There are no removes or puts propagated to the shared cache, as a result of the clears.
            assertEquals(removesAtStart+0, cacheStats.count("transactionalCache", OpType.REMOVE));
            assertEquals(putsAtStart+0, cacheStats.count("transactionalCache", OpType.PUT));
            assertEquals(hitsAtStart+3, cacheStats.count("transactionalCache", OpType.GET_HIT));
            assertEquals(missesAtStart+4, cacheStats.count("transactionalCache", OpType.GET_MISS));
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
}
