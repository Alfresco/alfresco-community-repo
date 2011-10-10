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

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import net.sf.ehcache.CacheException;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.repo.transaction.TransactionListener;
import org.alfresco.util.EqualsHelper;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * A 2-level cache that mainains both a transaction-local cache and
 * wraps a non-transactional (shared) cache.
 * <p>
 * It uses the <b>Ehcache</b> <tt>Cache</tt> for it's per-transaction
 * caches as these provide automatic size limitations, etc.
 * <p>
 * Instances of this class <b>do not require a transaction</b>.  They will work
 * directly with the shared cache when no transaction is present.  There is
 * virtually no overhead when running out-of-transaction.
 * <p>
 * The first phase of the commit ensures that any values written to the cache in the
 * current transaction are not already superceded by values in the shared cache.  In
 * this case, the transaction is failed for concurrency reasons and will have to retry.
 * The second phase occurs post-commit.  We are sure that the transaction committed
 * correctly, but things may have changed in the cache between the commit and post-commit.
 * If this is the case, then the offending values are merely removed from the shared
 * cache.
 * <p>
 * When the cache is {@link #clear() cleared}, a flag is set on the transaction.
 * The shared cache, instead of being cleared itself, is just ignored for the remainder
 * of the tranasaction.  At the end of the transaction, if the flag is set, the
 * shared transaction is cleared <i>before</i> updates are added back to it.
 * <p>
 * Because there is a limited amount of space available to the in-transaction caches,
 * when either of these becomes full, the cleared flag is set.  This ensures that
 * the shared cache will not have stale data in the event of the transaction-local
 * caches dropping items.  It is therefore important to size the transactional caches
 * correctly.
 * 
 * @author Derek Hulley
 */
public class TransactionalCache<K extends Serializable, V extends Object>
        implements SimpleCache<K, V>, TransactionListener, InitializingBean
{
    private static final String RESOURCE_KEY_TXN_DATA = "TransactionalCache.TxnData"; 
    
    private Log logger;
    private boolean isDebugEnabled;

    /** a name used to uniquely identify the transactional caches */
    private String name;
    /** enable/disable write through to the shared cache */
    private boolean disableSharedCache;
    /** the shared cache that will get updated after commits */
    private SimpleCache<Serializable, Object> sharedCache;
    /** can the cached values be modified */
    private boolean isMutable;
    /** the maximum number of elements to be contained in the cache */
    private int maxCacheSize = 500;
    /** a unique string identifying this instance when binding resources */
    private String resourceKeyTxnData;

    /**
     * Public constructor.
     */
    public TransactionalCache()
    {
        logger = LogFactory.getLog(TransactionalCache.class);
        isDebugEnabled = logger.isDebugEnabled();
        disableSharedCache = false;
        isMutable = true;
    }
    
    /**
     * @see #setName(String)
     */
    public String toString()
    {
        return name;
    }
    
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (!(obj instanceof TransactionalCache<?, ?>))
        {
            return false;
        }
        @SuppressWarnings("rawtypes")
        TransactionalCache that = (TransactionalCache) obj;
        return EqualsHelper.nullSafeEquals(this.name, that.name);
    }
    
    public int hashCode()
    {
        return name.hashCode();
    }
    
    /**
     * Set the shared cache to use during transaction synchronization or when no transaction
     * is present.
     * 
     * @param sharedCache           underlying cache shared by transactions
     */
    public void setSharedCache(SimpleCache<Serializable, Object> sharedCache)
    {
        this.sharedCache = sharedCache;
    }

    /**
     * Set whether values must be written through to the shared cache or not
     * 
     * @param disableSharedCache    <tt>true</tt> to prevent values from being written to
     *                              the shared cache
     */
    public void setDisableSharedCache(boolean disableSharedCache)
    {
        this.disableSharedCache = disableSharedCache;
    }

    /**
     * @param isMutable             <tt>true</tt> if the data stored in the cache is modifiable
     */
    public void setMutable(boolean isMutable)
    {
        this.isMutable = isMutable;
    }

    /**
     * Set the maximum number of elements to store in the update and remove caches.
     * The maximum number of elements stored in the transaction will be twice the
     * value given.
     * <p>
     * The removed list will overflow to disk in order to ensure that deletions are
     * not lost.
     * 
     * @param maxCacheSize
     */
    public void setMaxCacheSize(int maxCacheSize)
    {
        this.maxCacheSize = maxCacheSize;
    }

    /**
     * Set the name that identifies this cache from other instances.  This is optional.
     * 
     * @param name
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Ensures that all properties have been set
     */
    public void afterPropertiesSet() throws Exception
    {
        PropertyCheck.mandatory(this, "name", name);
        PropertyCheck.mandatory(this, "sharedCache", sharedCache);
        
        // generate the resource binding key
        resourceKeyTxnData = RESOURCE_KEY_TXN_DATA + "." + name;
        // Refine the log category
        logger = LogFactory.getLog(TransactionalCache.class.getName() + "." + name);
        isDebugEnabled = logger.isDebugEnabled();
        
        // Assign a 'null' cache if write-through is disabled
        if (disableSharedCache)
        {
            sharedCache = new NullCache<Serializable, Object>();
        }
    }

    /**
     * To be used in a transaction only.
     */
    private TransactionData getTransactionData()
    {
        @SuppressWarnings("unchecked")
        TransactionData data = (TransactionData) AlfrescoTransactionSupport.getResource(resourceKeyTxnData);
        if (data == null)
        {
            data = new TransactionData();
            // create and initialize caches
            data.updatedItemsCache = new LRULinkedHashMap<K, CacheBucket<V>>(23);
            data.removedItemsCache = new HashSet<K>(13);
            data.isReadOnly = AlfrescoTransactionSupport.getTransactionReadState() == TxnReadState.TXN_READ_ONLY;

            // ensure that we get the transaction callbacks as we have bound the unique
            // transactional caches to a common manager
            AlfrescoTransactionSupport.bindListener(this);
            AlfrescoTransactionSupport.bindResource(resourceKeyTxnData, data);
        }
        return data;
    }
    
    /**
     * Checks the transactional removed and updated caches before checking the shared cache.
     */
    public boolean contains(K key)
    {
        Object value = get(key);
        if (value == null)
        {
            return false;
        }
        else
        {
            return true;
        }
    }
    
    /**
     * The keys returned are a union of the set of keys in the current transaction and
     * those in the backing cache.
     */
    @SuppressWarnings("unchecked")
    public Collection<K> getKeys()
    {
        Collection<K> keys = null;
        // in-txn layering
        if (AlfrescoTransactionSupport.getTransactionId() != null)
        {
            keys = new HashSet<K>(23);
            TransactionData txnData = getTransactionData();
            if (!txnData.isClearOn)
            {
                // the backing cache is not due for a clear
                Collection<K> backingKeys = (Collection<K>) sharedCache.getKeys();
                keys.addAll(backingKeys);
            }
            // add keys
            keys.addAll(txnData.updatedItemsCache.keySet());
            // remove keys
            keys.removeAll(txnData.removedItemsCache);
        }
        else
        {
            // no transaction, so just use the backing cache
            keys = (Collection<K>) sharedCache.getKeys();
        }
        // done
        return keys;
    }
    
    /**
     * Fetches a value from the shared cache.
     * 
     * @param key           the key
     * @return              Returns the value or <tt>null</tt>
     */
    @SuppressWarnings("unchecked")
    private V getSharedCacheValue(K key)
    {
        return (V) sharedCache.get(key);
    }

    /**
     * Checks the per-transaction caches for the object before going to the shared cache.
     * If the thread is not in a transaction, then the shared cache is accessed directly.
     */
    public V get(K key)
    {
        boolean ignoreSharedCache = false;
        // are we in a transaction?
        if (AlfrescoTransactionSupport.getTransactionId() != null)
        {
            TransactionData txnData = getTransactionData();
            if (txnData.isClosed)
            {
                // This check could have been done in the first if block, but that would have added another call to the
                // txn resources.
            }
            else            // The txn is still active
            {
                try
                {
                    if (!txnData.isClearOn)   // deletions cache only useful before a clear
                    {
                        // check to see if the key is present in the transaction's removed items
                        if (txnData.removedItemsCache.contains(key))
                        {
                            // it has been removed in this transaction
                            if (isDebugEnabled)
                            {
                                logger.debug("get returning null - item has been removed from transactional cache: \n" +
                                        "   cache: " + this + "\n" +
                                        "   key: " + key);
                            }
                            return null;
                        }
                    }
                    
                    // check for the item in the transaction's new/updated items
                    CacheBucket<V> bucket = (CacheBucket<V>) txnData.updatedItemsCache.get(key);
                    if (bucket != null)
                    {
                        V value = bucket.getValue();
                        // element was found in transaction-specific updates/additions
                        if (isDebugEnabled)
                        {
                            logger.debug("Found item in transactional cache: \n" +
                                    "   cache: " + this + "\n" +
                                    "   key: " + key + "\n" +
                                    "   value: " + value);
                        }
                        return value;
                    }
                }
                catch (CacheException e)
                {
                    throw new AlfrescoRuntimeException("Cache failure", e);
                }
                // check if the cleared flag has been set - cleared flag means ignore shared as unreliable
                ignoreSharedCache = txnData.isClearOn;
            }
        }
        // no value found - must we ignore the shared cache?
        if (!ignoreSharedCache)
        {
            V value = getSharedCacheValue(key);
            // go to the shared cache
            if (isDebugEnabled)
            {
                logger.debug("No value found in transaction - fetching instance from shared cache: \n" +
                        "   cache: " + this + "\n" +
                        "   key: " + key + "\n" +
                        "   value: " + value);
            }
            return value;
        }
        else        // ignore shared cache
        {
            if (isDebugEnabled)
            {
                logger.debug("No value found in transaction and ignoring shared cache: \n" +
                        "   cache: " + this + "\n" +
                        "   key: " + key);
            }
            return null;
        }
    }

    /**
     * Goes direct to the shared cache in the absence of a transaction.
     * <p>
     * Where a transaction is present, a cache of updated items is lazily added to the
     * thread and the <tt>Object</tt> put onto that. 
     */
    @SuppressWarnings("unchecked")
    public void put(K key, V value)
    {
        // are we in a transaction?
        if (AlfrescoTransactionSupport.getTransactionId() == null)  // not in transaction
        {
            // no transaction
            sharedCache.put(key, value);
            // done
            if (isDebugEnabled)
            {
                logger.debug("No transaction - adding item direct to shared cache: \n" +
                        "   cache: " + this + "\n" +
                        "   key: " + key + "\n" +
                        "   value: " + value);
            }
        }
        else  // transaction present
        {
            TransactionData txnData = getTransactionData();
            // Ensure that the cache isn't being modified
            if (txnData.isClosed)
            {
                if (isDebugEnabled)
                {
                    logger.debug(
                            "In post-commit add: \n" +
                            "   cache: " + this + "\n" +
                            "   key: " + key + "\n" +
                            "   value: " + value);
                }
            }
            else
            {
                // we have an active transaction - add the item into the updated cache for this transaction
                // are we in an overflow condition?
                if (txnData.updatedItemsCache.hasHitSize())
                {
                    // overflow about to occur or has occured - we can only guarantee non-stale
                    // data by clearing the shared cache after the transaction.  Also, the
                    // shared cache needs to be ignored for the rest of the transaction.
                    txnData.isClearOn = true;
                    if (!txnData.haveIssuedFullWarning && logger.isWarnEnabled())
                    {
                        logger.warn("Transactional update cache '" + name + "' is full (" + maxCacheSize + ").");
                        txnData.haveIssuedFullWarning = true;
                    }
                }
                Object existingValueObj = sharedCache.get(key);
                CacheBucket<V> bucket = null;
                if (existingValueObj == null)
                {
                    // ALF-5134: Performance of Alfresco cluster less than performance of single node
                    // The 'null' marker that used to be inserted also triggered an update in the afterCommit
                    // phase; the update triggered cache invalidation in the cluster.  Now, the null cannot
                    // be verified to be the same null - there is no null equivalence
                    // 
                    // The value didn't exist before
                    bucket = new NewCacheBucket<V>(value);
                }
                else
                {
                    // Record the existing value as is
                    bucket = new UpdateCacheBucket<V>((V)existingValueObj, value);
                }
                txnData.updatedItemsCache.put(key, bucket);
                // remove the item from the removed cache, if present
                txnData.removedItemsCache.remove(key);
                // done
                if (isDebugEnabled)
                {
                    logger.debug("In transaction - adding item direct to transactional update cache: \n" +
                            "   cache: " + this + "\n" +
                            "   key: " + key + "\n" +
                            "   value: " + value);
                }
            }
        }
    }

    /**
     * Goes direct to the shared cache in the absence of a transaction.
     * <p>
     * Where a transaction is present, a cache of removed items is lazily added to the
     * thread and the <tt>Object</tt> put onto that. 
     */
    public void remove(K key)
    {
        // are we in a transaction?
        if (AlfrescoTransactionSupport.getTransactionId() == null)  // not in transaction
        {
            // no transaction
            sharedCache.remove(key);
            // done
            if (isDebugEnabled)
            {
                logger.debug("No transaction - removing item from shared cache: \n" +
                        "   cache: " + this + "\n" +
                        "   key: " + key);
            }
        }
        else  // transaction present
        {
            TransactionData txnData = getTransactionData();
            // Ensure that the cache isn't being modified
            if (txnData.isClosed)
            {
                if (isDebugEnabled)
                {
                    logger.debug(
                            "In post-commit remove: \n" +
                            "   cache: " + this + "\n" +
                            "   key: " + key);
                }
            }
            else
            {
                // is the shared cache going to be cleared?
                if (txnData.isClearOn)
                {
                    // don't store removals if we're just going to clear it all out later
                }
                else
                {
                    // are we in an overflow condition?
                    if (txnData.removedItemsCache.size() >= maxCacheSize)
                    {
                        // overflow about to occur or has occured - we can only guarantee non-stale
                        // data by clearing the shared cache after the transaction.  Also, the
                        // shared cache needs to be ignored for the rest of the transaction.
                        txnData.isClearOn = true;
                        if (!txnData.haveIssuedFullWarning && logger.isWarnEnabled())
                        {
                            logger.warn("Transactional removal cache '" + name + "' is full (" + maxCacheSize + ").");
                            txnData.haveIssuedFullWarning = true;
                        }
                    }
                    else
                    {
                        // Create a bucket to remove the value from the shared cache
                        txnData.removedItemsCache.add(key);
                    }
                }
                // remove the item from the udpated cache, if present
                txnData.updatedItemsCache.remove(key);
                // done
                if (isDebugEnabled)
                {
                    logger.debug("In transaction - adding item direct to transactional removed cache: \n" +
                            "   cache: " + this + "\n" +
                            "   key: " + key);
                }
            }
        }
    }

    /**
     * Clears out all the caches.
     */
    public void clear()
    {
        // clear local caches
        if (AlfrescoTransactionSupport.getTransactionId() != null)
        {
            if (isDebugEnabled)
            {
                logger.debug("In transaction clearing cache: \n" +
                        "   cache: " + this + "\n" +
                        "   txn: " + AlfrescoTransactionSupport.getTransactionId());
            }
            
            TransactionData txnData = getTransactionData();
            // Ensure that the cache isn't being modified
            if (txnData.isClosed)
            {
                if (isDebugEnabled)
                {
                    logger.debug(
                            "In post-commit clear: \n" +
                            "   cache: " + this);
                }
            }
            else
            {
                // the shared cache must be cleared at the end of the transaction
                // and also serves to ensure that the shared cache will be ignored
                // for the remainder of the transaction
                txnData.isClearOn = true;
                txnData.updatedItemsCache.clear();
                txnData.removedItemsCache.clear();
            }
        }
        else            // no transaction
        {
            if (isDebugEnabled)
            {
                logger.debug("No transaction - clearing shared cache");
            }
            // clear shared cache
            sharedCache.clear();
        }
    }

    /**
     * NO-OP
     */
    public void flush()
    {
    }

    /**
     * NO-OP
     */
    public void beforeCompletion()
    {
    }

    /**
     * Merge the transactional caches into the shared cache
     */
    public void beforeCommit(boolean readOnly)
    {
        if (isDebugEnabled)
        {
            logger.debug("Processing before-commit");
        }
        
        TransactionData txnData = getTransactionData();
        try
        {
            if (txnData.isClearOn)
            {
                // clear shared cache
                sharedCache.clear();
                if (isDebugEnabled)
                {
                    logger.debug("Clear notification recieved in commit - clearing shared cache");
                }
            }
            else
            {
                // transfer any removed items
                for (Serializable key : txnData.removedItemsCache)
                {
                    sharedCache.remove(key);
                }
                if (isDebugEnabled)
                {
                    logger.debug("Removed " + txnData.removedItemsCache.size() + " values from shared cache in commit");
                }
            }

            // transfer updates
            Set<K> keys = (Set<K>) txnData.updatedItemsCache.keySet();
            for (Map.Entry<K, CacheBucket<V>> entry : (Set<Map.Entry<K, CacheBucket<V>>>) txnData.updatedItemsCache.entrySet())
            {
                K key = entry.getKey();
                CacheBucket<V> bucket = entry.getValue();
                bucket.doPreCommit(sharedCache, key, this.isMutable, txnData.isReadOnly);
            }
            if (isDebugEnabled)
            {
                logger.debug("Pre-commit called for " + keys.size() + " values.");
            }
        }
        catch (CacheException e)
        {
            throw new AlfrescoRuntimeException("Failed to transfer updates to shared cache", e);
        }
        finally
        {
            // Block any further updates
            txnData.isClosed = true;
        }
    }

    /**
     * Merge the transactional caches into the shared cache
     */
    public void afterCommit()
    {
        if (isDebugEnabled)
        {
            logger.debug("Processing after-commit");
        }
        
        TransactionData txnData = getTransactionData();
        try
        {
            if (txnData.isClearOn)
            {
                // clear shared cache
                sharedCache.clear();
                if (isDebugEnabled)
                {
                    logger.debug("Clear notification recieved in commit - clearing shared cache");
                }
            }
            else
            {
                // transfer any removed items
                for (Serializable key : txnData.removedItemsCache)
                {
                    sharedCache.remove(key);
                }
                if (isDebugEnabled)
                {
                    logger.debug("Removed " + txnData.removedItemsCache.size() + " values from shared cache in commit");
                }
            }

            // transfer updates
            Set<K> keys = (Set<K>) txnData.updatedItemsCache.keySet();
            for (Map.Entry<K, CacheBucket<V>> entry : (Set<Map.Entry<K, CacheBucket<V>>>) txnData.updatedItemsCache.entrySet())
            {
                K key = entry.getKey();
                CacheBucket<V> bucket = entry.getValue();
                bucket.doPostCommit(sharedCache, key, this.isMutable, txnData.isReadOnly);
            }
            if (isDebugEnabled)
            {
                logger.debug("Post-commit called for " + keys.size() + " values.");
            }
        }
        catch (CacheException e)
        {
            throw new AlfrescoRuntimeException("Failed to transfer updates to shared cache", e);
        }
        finally
        {
            removeCaches(txnData);
        }
    }

    /**
     * Transfers cache removals or clears.  This allows explicit cache cleanup to be propagated
     * to the shared cache even in the event of rollback - useful if the cause of a problem is
     * the shared cache value.
     */
    public void afterRollback()
    {
        TransactionData txnData = getTransactionData();
        try
        {
            if (txnData.isClearOn)
            {
                // clear shared cache
                sharedCache.clear();
                if (isDebugEnabled)
                {
                    logger.debug("Clear notification recieved in rollback - clearing shared cache");
                }
            }
            else
            {
                // transfer any removed items
                for (Serializable key : txnData.removedItemsCache)
                {
                    sharedCache.remove(key);
                }
                if (isDebugEnabled)
                {
                    logger.debug("Removed " + txnData.removedItemsCache.size() + " values from shared cache in rollback");
                }
            }
        }
        catch (CacheException e)
        {
            throw new AlfrescoRuntimeException("Failed to transfer updates to shared cache", e);
        }
        finally
        {
            removeCaches(txnData);
        }
    }
    
    /**
     * Ensures that the transactional caches are removed from the common cache manager.
     * 
     * @param txnData the data with references to the the transactional caches
     */
    private void removeCaches(TransactionData txnData)
    {
        txnData.isClosed = true;
    }
    
    /**
     * Interface for the transactional cache buckets.  These hold the actual values along
     * with some state and behaviour around writing from the in-transaction caches to the
     * shared.
     * 
     * @author Derek Hulley
     */
    private interface CacheBucket<BV extends Object> extends Serializable
    {
        /**
         * @return                  Returns the bucket's value
         */
        BV getValue();
        /**
         * Flush the current bucket to the shared cache as far as possible.
         * 
         * @param sharedCache       the cache to flush to
         * @param key               the key that the bucket was stored against
         */
        public void doPreCommit(
                SimpleCache<Serializable, Object> sharedCache,
                Serializable key,
                boolean mutable, boolean readOnly);
        /**
         * Flush the current bucket to the shared cache as far as possible.
         * 
         * @param sharedCache       the cache to flush to
         * @param key               the key that the bucket was stored against
         */
        public void doPostCommit(
                SimpleCache<Serializable, Object> sharedCache,
                Serializable key,
                boolean mutable, boolean readOnly);
    }
    
    /**
     * A bucket class to hold values for the caches.<br/>
     * 
     * @author Derek Hulley
     */
    private static class NewCacheBucket<BV> implements CacheBucket<BV>
    {
        private static final long serialVersionUID = -8536386687213957425L;
        
        private final BV value;
        public NewCacheBucket(BV value)
        {
            this.value = value;
        }
        public BV getValue()
        {
            return value;
        }
        public void doPreCommit(
                SimpleCache<Serializable, Object> sharedCache,
                Serializable key,
                boolean mutable, boolean readOnly)
        {
        }
        public void doPostCommit(
                SimpleCache<Serializable, Object> sharedCache,
                Serializable key,
                boolean mutable, boolean readOnly)
        {
            Object sharedObj = sharedCache.get(key);
            if (!mutable)
            {
                // Value can't change
                if (sharedObj == null)
                {
                    // Still nothing in the cache
                    sharedCache.put(key, value);
                }
            }
            else if (readOnly)
            {
                // Only add if nothing else has been added in the interim
                if (sharedObj == null)
                {
                    sharedCache.put(key, value);
                }
            }
            else
            {
                // Mutable, read-write
                if (sharedObj == null)
                {
                    sharedCache.put(key, value);                    
                }
                else
                {
                    // Remove new value in the cache
                    sharedCache.remove(key);
                }
            }
        }
    }
    
    /**
     * Data holder to keep track of a cached value's ID in order to detect stale
     * shared cache values.  This bucket assumes the presence of a pre-existing entry in
     * the shared cache.
     */
    private static class UpdateCacheBucket<BV> implements CacheBucket<BV>
    {
        private static final long serialVersionUID = 7885689778259779578L;
        
        private final BV value;
        private final BV originalValue;
        public UpdateCacheBucket(BV originalValue, BV value)
        {
            this.originalValue = originalValue;
            this.value = value;
        }
        public BV getValue()
        {
            return value;
        }
        public void doPreCommit(
                SimpleCache<Serializable, Object> sharedCache,
                Serializable key,
                boolean mutable, boolean readOnly)
        {
        }
        public void doPostCommit(
                SimpleCache<Serializable, Object> sharedCache,
                Serializable key,
                boolean mutable, boolean readOnly)
        {
            Object sharedObj = sharedCache.get(key);
            if (!mutable)
            {
                // Not normally required as mutable objects don't change,
                // but we can write it straight through as it should represent
                // unchanging values
                sharedCache.put(key, value);
            }
            else if (readOnly)
            {
                // Only add if value has not changed in the interim
                if (sharedObj == originalValue)
                {
                    sharedCache.put(key, value);
                }
            }
            else
            {
                // Mutable, read-write
                if (sharedObj == originalValue)
                {
                    sharedCache.put(key, value);
                }
                else
                {
                    // The value changed
                    sharedCache.remove(key);
                }
            }
        }
    }
    
    /** Data holder to bind data to the transaction */
    private class TransactionData
    {
        private LRULinkedHashMap<K, CacheBucket<V>> updatedItemsCache;
        private Set<K> removedItemsCache;
        private boolean haveIssuedFullWarning;
        private boolean isClearOn;
        private boolean isClosed;
        private boolean isReadOnly;
    }
    
    /**
     * Simple LRU based on {@link LinkedHashMap}
     * 
     * @author Derek Hulley
     * @since 3.4
     */
    private class LRULinkedHashMap<K1, V1> extends LinkedHashMap<K1, V1>
    {
        private static final long serialVersionUID = -4874684348174271106L;

        private LRULinkedHashMap(int initialSize)
        {
            super(initialSize);
        }
        private boolean hasHitSize()
        {
            return size() >= maxCacheSize;
        }
        /**
         * Remove the eldest entry if the size has reached the maximum cache size
         */
        @Override
        protected boolean removeEldestEntry(Map.Entry<K1, V1> eldest)
        {
            return (size() > maxCacheSize);
        }
    }
}
