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
package org.alfresco.repo.cache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.tenant.TenantUtil;
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
 * It uses the <b>shared</b> <tt>SimpleCache</tt> for it's per-transaction
 * caches as these can provide automatic size limitations, etc.
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
        implements LockingCache<K, V>, TransactionListener, InitializingBean
{
    private static final String RESOURCE_KEY_TXN_DATA = "TransactionalCache.TxnData";
    
    private Log logger;
    private boolean isDebugEnabled;
    
    /** a name used to uniquely identify the transactional caches */
    private String name;
    /** enable/disable write through to the shared cache */
    private boolean disableSharedCache;
    /** the shared cache that will get updated after commits */
    private SimpleCache<Serializable, ValueHolder<V>> sharedCache;
    /** can the cached values be modified */
    private boolean isMutable;
    /** can values be compared using full equality checking */
    private boolean allowEqualsChecks;
    /** the maximum number of elements to be contained in the cache */
    private int maxCacheSize = 500;
    /** a unique string identifying this instance when binding resources */
    private String resourceKeyTxnData;
    
    private boolean isTenantAware = true; // true if tenant-aware (default), false if system-wide
    
    /**
     * Public constructor.
     */
    public TransactionalCache()
    {
        logger = LogFactory.getLog(TransactionalCache.class);
        isDebugEnabled = logger.isDebugEnabled();
        disableSharedCache = false;
        isMutable = true;
        allowEqualsChecks = false;
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
    public void setSharedCache(SimpleCache<Serializable, ValueHolder<V>> sharedCache)
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
     * Allow equality checking of values before they are written to the shared cache on
     * commit.  This allows some caches to bypass unnecessary cache updates when the
     * values remain unchanged.  Typically, this setting should be applied only to mutable
     * caches and only where the values being stored have a fast and reliable equality check.
     * 
     * @param allowEqualsChecks     <tt>true</tt> if value comparisons can be made between values
     *                              stored in the transactional cache and those stored in the
     *                              shared cache
     */
    public void setAllowEqualsChecks(boolean allowEqualsChecks)
    {
        this.allowEqualsChecks = allowEqualsChecks;
    }

    /**
     * Set the maximum number of elements to store in the update and remove caches.
     * The maximum number of elements stored in the transaction will be twice the
     * value given.
     * <p>
     * The removed list will overflow to disk in order to ensure that deletions are
     * not lost.
     * 
     * @param maxCacheSize          maximum number of items to be held in-transaction
     */
    public void setMaxCacheSize(int maxCacheSize)
    {
        this.maxCacheSize = maxCacheSize;
    }

    /**
     * Set the name that identifies this cache from other instances.
     */
    public void setName(String name)
    {
        this.name = name;
    }
    
    public void setTenantAware(boolean isTenantAware)
    {
        this.isTenantAware = isTenantAware;
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
            sharedCache = NullCache.getInstance();
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
            data.updatedItemsCache = new LRULinkedHashMap<Serializable, CacheBucket<V>>(23);
            data.removedItemsCache = new HashSet<Serializable>(13);
            data.lockedItemsCache = new HashSet<Serializable>(13);
            data.isReadOnly = AlfrescoTransactionSupport.getTransactionReadState() == TxnReadState.TXN_READ_ONLY;

            // ensure that we get the transaction callbacks as we have bound the unique
            // transactional caches to a common manager
            AlfrescoTransactionSupport.bindListener(this);
            AlfrescoTransactionSupport.bindResource(resourceKeyTxnData, data);
        }
        return data;
    }
    
    /**
     * @see #setDisableSharedCacheReadForTransaction(boolean)
     */
    public boolean getDisableSharedCacheReadForTransaction()
    {
        if (AlfrescoTransactionSupport.getTransactionId() != null)
        {
            TransactionData txnData = getTransactionData();
            return txnData.noSharedCacheRead;
        }
        else
        {
            return false;
        }
    }
    
    /**
     * Transaction-long setting to force all the share cache to be bypassed for the current transaction.
     * <p/>
     * This setting is like having a {@link NullCache null} {@link #setSharedCache(SimpleCache) shared cache},
     * but only lasts for the transaction.
     * <p/>
     * Use this when a read transaction <b>must</b> see consistent and current data i.e. go to the database.
     * While this is active, write operations will also not be committed to the shared cache.
     * 
     * @param noSharedCacheRead         <tt>true</tt> to avoid reading from the shared cache for the transaction
     */
    @SuppressWarnings("unchecked")
    public void setDisableSharedCacheReadForTransaction(boolean noSharedCacheRead)
    {
        TransactionData txnData = getTransactionData();

        // If we are switching on noSharedCacheRead mode, convert all existing reads and updates to avoid 'consistent
        // read' behaviour giving us a potentially out of date node already accessed
        if (noSharedCacheRead && !txnData.noSharedCacheRead)
        {
            txnData.noSharedCacheRead = noSharedCacheRead;
            String currentCacheRegion = TenantUtil.getCurrentDomain();
            for (Map.Entry<Serializable, CacheBucket<V>> entry : new ArrayList<Map.Entry<Serializable, CacheBucket<V>>>(
                    txnData.updatedItemsCache.entrySet()))
            {
                Serializable cacheKey = entry.getKey();
                K key = null;
                if (cacheKey instanceof CacheRegionKey)
                {
                    CacheRegionKey cacheRegionKey = (CacheRegionKey) cacheKey;
                    if (currentCacheRegion.equals(cacheRegionKey.getCacheRegion()))
                    {
                        key = (K) cacheRegionKey.getCacheKey();
                    }
                }
                else
                {
                    key = (K) cacheKey;
                }

                if (key != null)
                {
                    CacheBucket<V> bucket = entry.getValue();
                    // Simply 'forget' reads
                    if (bucket instanceof ReadCacheBucket)
                    {
                        txnData.updatedItemsCache.remove(cacheKey);
                    }
                    // Convert updates to removes
                    else if (bucket instanceof UpdateCacheBucket)
                    {
                        remove(key);
                    }
                    // Leave new entries alone - they can't have come from the shared cache
                }
            }
        }
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
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Collection<K> getKeys()
    {
        Collection<Serializable> keys = null;
        // in-txn layering
        if (AlfrescoTransactionSupport.getTransactionId() != null)
        {
            keys = new HashSet<Serializable>(23);
            TransactionData txnData = getTransactionData();
            if (!txnData.isClearOn)
            {
                // the backing cache is not due for a clear
                Collection<K> backingKeys = (Collection<K>)sharedCache.getKeys();
                Collection<Serializable> backingCacheKeys = new HashSet<Serializable>(backingKeys.size());
                for (K backingKey : backingKeys)
                {
                    backingCacheKeys.add(getTenantAwareCacheKey(backingKey));
                }
                keys.addAll(backingCacheKeys);
            }
            // add keys
            keys.addAll(txnData.updatedItemsCache.keySet());
            // remove keys
            keys.removeAll(txnData.removedItemsCache);
        }
        else
        {
            // no transaction, so just use the backing cache
            keys = (Collection) sharedCache.getKeys();
        }
        
        Collection<K> cacheKeys = new HashSet<K>(keys.size());
        String currentCacheRegion = TenantUtil.getCurrentDomain();
        
        for (Serializable key : keys)
        {
            if (key instanceof CacheRegionKey)
            {
                CacheRegionKey cacheRegionKey = (CacheRegionKey)key;
                if (currentCacheRegion.equals(cacheRegionKey.getCacheRegion()))
                {
                    cacheKeys.add((K)cacheRegionKey.getCacheKey());
                }
            }
            else
            {
                cacheKeys.add((K)key);
            }
        }
        // done
        return cacheKeys;
    }
    
    /**
     * Fetches a value from the shared cache.  If values were wrapped,
     * then they will be unwrapped before being returned.  If code requires
     * direct access to the wrapper object as well, then this call should not
     * be used.
     * 
     * @param key           the key
     * @return              Returns the value or <tt>null</tt>
     */
    @SuppressWarnings("unchecked")
    public static <KEY extends Serializable, VAL> VAL getSharedCacheValue(SimpleCache<KEY, ValueHolder<VAL>> sharedCache, KEY key)
    {
        Object possibleWrapper = sharedCache.get(key);
        if (possibleWrapper == null)
        {
            return null;
        }
        else if (possibleWrapper instanceof ValueHolder)
        {
            ValueHolder<VAL> wrapper = (ValueHolder<VAL>) possibleWrapper;
            return wrapper.getValue();
        }
        else
        {
            throw new IllegalStateException("All entries for TransactionalCache must be put using TransactionalCache.putSharedCacheValue.");
        }
    }
    
    /**
     * Values written to the backing cache need proper wrapping and unwrapping
     * 
     * @param sharedCache           the cache to operate on
     * @param key                   the key
     * @param value                 the value to wrap
     * 
     * @since 4.2.3
     */
    public static <KEY extends Serializable, VAL> void putSharedCacheValue(SimpleCache<KEY, ValueHolder<VAL>> sharedCache, KEY key, VAL value)
    {
        ValueHolder<VAL> wrapper = new ValueHolder<VAL>(value);
        sharedCache.put(key, wrapper);
    }
    
    /**
     * @param txnData       the existing data associated with the transaction
     * @param key           a tenant-aware key
     * @return              <tt>true</tt> if the key is locked
     * 
     * @see HashSet#contains(Object)
     * @see HashSet#size()
     */
    private final boolean isValueLocked(TransactionData txnData, Serializable key)
    {
        /*
         * Locking will be very infrequent.  Calculating the hashcode of the key
         * and using it to determine whether the lockedItemsCache contains the key is an
         * unnecessary overhead; use the size() method for a slightly faster answer
         * in the bulk of cases.
         */
        return (txnData.lockedItemsCache.size() > 0 && txnData.lockedItemsCache.contains(key));
    }
    
    @Override
    public boolean isValueLocked(K keyIn)
    {
        if (AlfrescoTransactionSupport.getTransactionId() != null)
        {
            final Serializable key = getTenantAwareCacheKey(keyIn);
            TransactionData txnData = getTransactionData();
            return txnData.lockedItemsCache.contains(key);
        }
        else
        {
            // No transaction; we can't have locked it
            return false;
        }
    }

    @Override
    public void lockValue(K keyIn)
    {
        if (AlfrescoTransactionSupport.getTransactionId() != null)
        {
            final Serializable key = getTenantAwareCacheKey(keyIn);
            TransactionData txnData = getTransactionData();
            txnData.lockedItemsCache.add(key);
            return;
        }
        else
        {
            // No transaction; we can't lock it
            return;
        }
    }

    @Override
    public void unlockValue(K keyIn)
    {
        if (AlfrescoTransactionSupport.getTransactionId() != null)
        {
            final Serializable key = getTenantAwareCacheKey(keyIn);
            TransactionData txnData = getTransactionData();
            txnData.lockedItemsCache.remove(key);
            return;
        }
        else
        {
            // No transaction; we can't unlock it
            return;
        }
    }

    /**
     * Checks the per-transaction caches for the object before going to the shared cache.
     * If the thread is not in a transaction, then the shared cache is accessed directly.
     */
    public V get(K keyIn)
    {
        final Serializable key = getTenantAwareCacheKey(keyIn);
        
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
                else if (txnData.isClearOn)
                {
                    // Can't store values in the current txn any more
                    ignoreSharedCache = true;
                }
                else if (txnData.noSharedCacheRead)
                {
                    // Explicitly told to ignore shared cache
                    ignoreSharedCache = true;
                }
                else
                {
                    // There is no in-txn entry for the key
                    // Use the value direct from the shared cache
                    V value = TransactionalCache.getSharedCacheValue(sharedCache, key);
                    bucket = new ReadCacheBucket<V>(value);
                    txnData.updatedItemsCache.put(key, bucket);
                    return value;
                }
            }
        }
        // no value found - must we ignore the shared cache?
        if (!ignoreSharedCache)
        {
            V value = TransactionalCache.getSharedCacheValue(sharedCache, key);
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
    public void put(K keyIn, V value)
    {
        final Serializable key = getTenantAwareCacheKey(keyIn);
        
        // are we in a transaction?
        if (AlfrescoTransactionSupport.getTransactionId() == null)  // not in transaction
        {
            // no transaction
            TransactionalCache.putSharedCacheValue(sharedCache, key, value);
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
            else if (isValueLocked(txnData, key))
            {
                // The key has been locked
                if (isDebugEnabled)
                {
                    logger.debug(
                            "Ignoring put after detecting locked key: \n" +
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
                ValueHolder<V> existingValueHolder = txnData.noSharedCacheRead ? null : sharedCache.get(key);
                CacheBucket<V> bucket = null;
                if (existingValueHolder == null)
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
                    bucket = new UpdateCacheBucket<V>(existingValueHolder, value);
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
    public void remove(K keyIn)
    {
        final Serializable key = getTenantAwareCacheKey(keyIn);
        
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
            else if (isValueLocked(txnData, key))
            {
                // The key has been locked
                if (isDebugEnabled)
                {
                    logger.debug(
                            "Ignoring remove after detecting locked key: \n" +
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
                // The shared cache must be cleared at the end of the transaction
                // and also serves to ensure that the shared cache will be ignored
                // for the remainder of the transaction.
                // We do, however, keep all locked values locked.
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
            Set<Serializable> keys = (Set<Serializable>) txnData.updatedItemsCache.keySet();
            for (Map.Entry<Serializable, CacheBucket<V>> entry : (Set<Map.Entry<Serializable, CacheBucket<V>>>) txnData.updatedItemsCache.entrySet())
            {
                Serializable key = entry.getKey();
                CacheBucket<V> bucket = entry.getValue();
                bucket.doPreCommit(
                        sharedCache,
                        key, this.isMutable, this.allowEqualsChecks, txnData.isReadOnly);
            }
            if (isDebugEnabled)
            {
                logger.debug("Pre-commit called for " + keys.size() + " values.");
            }
        }
        catch (Throwable e)
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
            Set<Serializable> keys = (Set<Serializable>) txnData.updatedItemsCache.keySet();
            for (Map.Entry<Serializable, CacheBucket<V>> entry : (Set<Map.Entry<Serializable, CacheBucket<V>>>) txnData.updatedItemsCache.entrySet())
            {
                Serializable key = entry.getKey();
                CacheBucket<V> bucket = entry.getValue();
                try
                {
                    bucket.doPostCommit(
                            sharedCache,
                            key, this.isMutable, this.allowEqualsChecks, txnData.isReadOnly);
                }
                catch (Exception e)
                {
                    // MNT-10486: NPE in NodeEntity during post-commit write through to shared cache
                    //              This try-catch is diagnostic in nature.  We need to know the names of the caches
                    //              and details of the values involved.
                    //              The causal exception will be rethrown.
                    throw new AlfrescoRuntimeException(
                            "CacheBucket postCommit transfer to shared cache failed: \n" +
                            "   Cache:      " + sharedCache + "\n" +
                            "   Key:        " + key + "\n" +
                            "   New Value:  " + bucket.getValue() + "\n" +
                            "   Cache Value:" + sharedCache.get(key),
                            e);
                }
            }
            if (isDebugEnabled)
            {
                logger.debug("Post-commit called for " + keys.size() + " values.");
            }
        }
        catch (Throwable e)
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
        catch (Throwable e)
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
                SimpleCache<Serializable, ValueHolder<BV>> sharedCache,
                Serializable key,
                boolean mutable, boolean allowEqualsCheck, boolean readOnly);
        /**
         * Flush the current bucket to the shared cache as far as possible.
         * 
         * @param sharedCache       the cache to flush to
         * @param key               the key that the bucket was stored against
         */
        public void doPostCommit(
                SimpleCache<Serializable, ValueHolder<BV>> sharedCache,
                Serializable key,
                boolean mutable, boolean allowEqualsCheck, boolean readOnly);
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
                SimpleCache<Serializable, ValueHolder<BV>> sharedCache,
                Serializable key,
                boolean mutable, boolean allowEqualsCheck, boolean readOnly)
        {
        }
        public void doPostCommit(
                SimpleCache<Serializable, ValueHolder<BV>> sharedCache,
                Serializable key,
                boolean mutable, boolean allowEqualsCheck, boolean readOnly)
        {
            ValueHolder<BV> sharedObjValueHolder = sharedCache.get(key);
            if (sharedObjValueHolder == null)
            {
                // Nothing has changed, write it through
                TransactionalCache.putSharedCacheValue(sharedCache, key, value);
            }
            else if (!mutable)
            {
                // Someone else put the object there
                // The assumption is that the value will be correct because the values are immutable
                // Don't write it unnecessarily.
            }
            else if (allowEqualsCheck && EqualsHelper.nullSafeEquals(value, sharedObjValueHolder.getValue()))
            {
                // The value we want to write is the same as the one in the shared cache.
                // Don't write it unnecessarily.
            }
            else
            {
                // The shared value moved on in a way that was not possible to
                // validate.  We pessimistically remove the entry.
                sharedCache.remove(key);
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
        private final ValueHolder<BV> originalValueHolder;
        public UpdateCacheBucket(ValueHolder<BV> originalValueHolder, BV value)
        {
            this.originalValueHolder = originalValueHolder;
            this.value = value;
        }
        public BV getValue()
        {
            return value;
        }
        public void doPreCommit(
                SimpleCache<Serializable, ValueHolder<BV>> sharedCache,
                Serializable key,
                boolean mutable, boolean allowEqualsCheck, boolean readOnly)
        {
        }
        public void doPostCommit(
                SimpleCache<Serializable, ValueHolder<BV>> sharedCache,
                Serializable key,
                boolean mutable, boolean allowEqualsCheck, boolean readOnly)
        {
            ValueHolder<BV> sharedObjValueHolder = sharedCache.get(key);
            if (sharedObjValueHolder == null)
            {
                // Someone removed the value
                if (!mutable)
                {
                    // We can assume that our value is correct because it's immutable
                    TransactionalCache.putSharedCacheValue(sharedCache, key, value);
                }
                else
                {
                    // The value is mutable, so we must behave pessimistically i.e. leave the shared cache empty
                }
            }
            else if (!mutable)
            {
                // We assume the configuration is correct and therefore, that we do not need to compare
                // the cached value with the updated value.  This applies to null as well.
            }
            else if (allowEqualsCheck && EqualsHelper.nullSafeEquals(value, sharedObjValueHolder.getValue()))
            {
                // The value we want to write is the same as the one in the shared cache.
                // Don't write it unnecessarily.
            }
            else if (EqualsHelper.nullSafeEquals(originalValueHolder, sharedObjValueHolder))
            {
                // The value in the cache did not change from what we observed before.
                // Update the value.
                TransactionalCache.putSharedCacheValue(sharedCache, key, value);
            }
            else
            {
                // The shared value moved on in a way that was not possible to
                // validate.  We pessimistically remove the entry.
                sharedCache.remove(key);
            }
        }
    }
    
    /**
     * Data holder to represent data read from the shared cache.  It will not attempt to
     * update the shared cache.
     */
    private static class ReadCacheBucket<BV> implements CacheBucket<BV>
    {
        private static final long serialVersionUID = 7885689778259779578L;
        
        private final BV value;
        public ReadCacheBucket(BV value)
        {
            this.value = value;
        }
        public BV getValue()
        {
            return value;
        }
        public void doPreCommit(
                SimpleCache<Serializable, ValueHolder<BV>> sharedCache,
                Serializable key,
                boolean mutable, boolean allowEqualsCheck, boolean readOnly)
        {
        }
        public void doPostCommit(
                SimpleCache<Serializable, ValueHolder<BV>> sharedCache,
                Serializable key,
                boolean mutable, boolean allowEqualsCheck, boolean readOnly)
        {
        }
    }
    
    /** Data holder to bind data to the transaction */
    private class TransactionData
    {
        private LRULinkedHashMap<Serializable, CacheBucket<V>> updatedItemsCache;
        private Set<Serializable> removedItemsCache;
        private Set<Serializable> lockedItemsCache;
        private boolean haveIssuedFullWarning;
        private boolean isClearOn;
        private boolean isClosed;
        private boolean isReadOnly;
        private boolean noSharedCacheRead;
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
    
    /**
     * Convert the key to a tenant-specific key if the cache is tenant-aware and
     * the current thread is running in the context of a tenant.
     * 
     * @param key           the key to convert
     * @return              a key that separates tenant-specific values
     */
    private Serializable getTenantAwareCacheKey(final K key)
    {
        if (isTenantAware)
        {
            final String tenantDomain = TenantUtil.getCurrentDomain();
            if (! tenantDomain.equals(TenantService.DEFAULT_DOMAIN))
            {
                return new CacheRegionKey(tenantDomain, key);
            }
            // drop through
        }
        return key;
    }
    
    public static class CacheRegionKey implements Serializable
    {
        private static final long serialVersionUID = -213050301938804468L;
        
        private final String cacheRegion;
        private final Serializable cacheKey;
        private final int hashCode;
        
        public CacheRegionKey(String cacheRegion, Serializable cacheKey)
        {
            this.cacheRegion = cacheRegion;
            this.cacheKey = cacheKey;
            this.hashCode = cacheRegion.hashCode() + cacheKey.hashCode();
        }
        
        public Serializable getCacheKey()
        {
            return cacheKey;
        }
        
        public String getCacheRegion()
        {
            return cacheRegion;
        }
        
        @Override
        public String toString()
        {
            return cacheRegion + (cacheRegion != "" ? "." : "") + cacheKey.toString();
        }
        
        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            else if (!(obj instanceof CacheRegionKey))
            {
                return false;
            }
            CacheRegionKey that = (CacheRegionKey) obj;
            return this.cacheRegion.equals(that.cacheRegion) && this.cacheKey.equals(that.cacheKey);
        }
        
        @Override
        public int hashCode()
        {
            return hashCode;
        }
    }

    /**
     * A wrapper object to carry object values, but forcing a straight equality check
     * based on a random integer only.  This is used in cases where cache values do NOT
     * have an adequate equals method and we expect serialization of objects.
     * 
     * @author Derek Hulley
     * @since 4.2.4
     */
    public static final class ValueHolder<V2> implements Serializable
    {
        private static final long serialVersionUID = -3462098329153772713L;
        
        /**
         * A random, positive integer since we only have to
         * prevent short-term duplication between values with the same keys.
         */
        private final int rand;
        private final V2 value;
        private ValueHolder(V2 value)
        {
            
            this.rand = (int) (Math.random() * Integer.MAX_VALUE);
            this.value = value;
        }
        public final V2 getValue()
        {
            return value;
        }
        @Override
        public final int hashCode()
        {
            return rand;
        }
        @SuppressWarnings("rawtypes")
        @Override
        public final boolean equals(Object obj)
        {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            ValueHolder other = (ValueHolder) obj;
            return this.rand == other.rand;
        }
        @Override
        public final String toString()
        {
            return "ValueHolder [value=" + value + "]";
        }
    }
}
