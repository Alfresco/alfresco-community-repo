/*
 * Copyright (C) 2005 Alfresco, Inc.
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
import java.util.HashSet;
import java.util.List;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.TransactionListener;
import org.alfresco.util.EqualsHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

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
 * 3 caches are maintained.
 * <ul>
 *   <li>Shared backing cache that should only be accessed by instances of this class</li>
 *   <li>Lazily created cache of updates made during the transaction</li>
 *   <li>Lazily created cache of deletions made during the transaction</li>
 * </ul>
 * <p>
 * When the cache is {@link #clear() cleared}, a flag is set on the transaction.
 * The shared cache, instead of being cleared itself, is just ignored for the remainder
 * of the tranasaction.  At the end of the transaction, if the flag is set, the
 * shared transaction is cleared <i>before</i> updates are added back to it.
 * <p>
 * Because there is a limited amount of space available to the in-transaction caches,
 * when either of these becomes full, the cleared flag is set.  This ensures that
 * the shared cache will not have stale data in the event of the transaction-local
 * caches dropping items.
 * 
 * @author Derek Hulley
 */
public class TransactionalCache<K extends Serializable, V extends Serializable>
        implements SimpleCache<K, V>, TransactionListener, InitializingBean
{
    private static final String RESOURCE_KEY_TXN_DATA = "TransactionalCache.TxnData"; 
    private static final String VALUE_DELETE = "TransactionalCache.DeleteMarker";
    
    private static Log logger = LogFactory.getLog(TransactionalCache.class);

    /** a name used to uniquely identify the transactional caches */
    private String name;
    
    /** the shared cache that will get updated after commits */
    private SimpleCache<Serializable, Serializable> sharedCache;

    /** the manager to control Ehcache caches */
    private CacheManager cacheManager;
    
    /** the maximum number of elements to be contained in the cache */
    private int maxCacheSize = 500;
    
    /** a unique string identifying this instance when binding resources */
    private String resourceKeyTxnData;

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
        if (!(obj instanceof TransactionalCache))
        {
            return false;
        }
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
     * @param sharedCache
     */
    public void setSharedCache(SimpleCache<Serializable, Serializable> sharedCache)
    {
        this.sharedCache = sharedCache;
    }

    /**
     * Set the manager to activate and control the cache instances
     * 
     * @param cacheManager
     */
    public void setCacheManager(CacheManager cacheManager)
    {
        this.cacheManager = cacheManager;
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
        Assert.notNull(name, "name property not set");
        Assert.notNull(cacheManager, "cacheManager property not set");
        // generate the resource binding key
        resourceKeyTxnData = RESOURCE_KEY_TXN_DATA + "." + name;
    }

    /**
     * To be used in a transaction only.
     */
    private TransactionData getTransactionData()
    {
        TransactionData data = (TransactionData) AlfrescoTransactionSupport.getResource(resourceKeyTxnData);
        if (data == null)
        {
            String txnId = AlfrescoTransactionSupport.getTransactionId();
            data = new TransactionData();
            // create and initialize caches
            data.updatedItemsCache = new Cache(
                    name + "_"+ txnId + "_updates",
                    maxCacheSize, false, true, 0, 0);
            data.removedItemsCache = new Cache(
                    name + "_" + txnId + "_removes",
                    maxCacheSize, false, true, 0, 0);
            try
            {
                cacheManager.addCache(data.updatedItemsCache);
                cacheManager.addCache(data.removedItemsCache);
            }
            catch (CacheException e)
            {
                throw new AlfrescoRuntimeException("Failed to add txn caches to manager", e);
            }
            finally
            {
                // ensure that we get the transaction callbacks as we have bound the unique
                // transactional caches to a common manager
                AlfrescoTransactionSupport.bindListener(this);
            }
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
            keys.addAll((Collection<K>) txnData.updatedItemsCache.getKeys());
            // remove keys
            keys.removeAll((Collection<K>) txnData.removedItemsCache.getKeys());
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
     * Checks the per-transaction caches for the object before going to the shared cache.
     * If the thread is not in a transaction, then the shared cache is accessed directly.
     */
    @SuppressWarnings("unchecked")
    public V get(K key)
    {
        boolean ignoreSharedCache = false;
        // are we in a transaction?
        if (AlfrescoTransactionSupport.getTransactionId() != null)
        {
            TransactionData txnData = getTransactionData();
            try
            {
                if (!txnData.isClearOn)   // deletions cache is still reliable
                {
                    // check to see if the key is present in the transaction's removed items
                    if (txnData.removedItemsCache.get(key) != null)
                    {
                        // it has been removed in this transaction
                        if (logger.isDebugEnabled())
                        {
                            logger.debug("get returning null - item has been removed from transactional cache: \n" +
                                    "   cache: " + this + "\n" +
                                    "   key: " + key);
                        }
                        return null;
                    }
                }
                
                // check for the item in the transaction's new/updated items
                Element element = txnData.updatedItemsCache.get(key);
                if (element != null)
                {
                    // element was found in transaction-specific updates/additions
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Found item in transactional cache: \n" +
                                "   cache: " + this + "\n" +
                                "   key: " + key + "\n" +
                                "   value: " + element.getValue());
                    }
                    return (V) element.getValue();
                }
            }
            catch (CacheException e)
            {
                throw new AlfrescoRuntimeException("Cache failure", e);
            }
            // check if the cleared flag has been set - cleared flag means ignore shared as unreliable
            ignoreSharedCache = txnData.isClearOn;
        }
        // no value found - must we ignore the shared cache?
        if (!ignoreSharedCache)
        {
            // go to the shared cache
            if (logger.isDebugEnabled())
            {
                logger.debug("No value found in transaction - fetching instance from shared cache: \n" +
                        "   cache: " + this + "\n" +
                        "   key: " + key + "\n" +
                        "   value: " + sharedCache.get(key));
            }
            return (V) sharedCache.get(key);
        }
        else        // ignore shared cache
        {
            if (logger.isDebugEnabled())
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
    public void put(K key, V value)
    {
        // are we in a transaction?
        if (AlfrescoTransactionSupport.getTransactionId() == null)  // not in transaction
        {
            // no transaction
            sharedCache.put(key, value);
            // done
            if (logger.isDebugEnabled())
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
            // we have a transaction - add the item into the updated cache for this transaction
            // are we in an overflow condition?
            if (txnData.updatedItemsCache.getMemoryStoreSize() >= maxCacheSize)
            {
                // overflow about to occur or has occured - we can only guarantee non-stale
                // data by clearing the shared cache after the transaction.  Also, the
                // shared cache needs to be ignored for the rest of the transaction.
                txnData.isClearOn = true;
            }
            Element element = new Element(key, value);
            txnData.updatedItemsCache.put(element);
            // remove the item from the removed cache, if present
            txnData.removedItemsCache.remove(key);
            // done
            if (logger.isDebugEnabled())
            {
                logger.debug("In transaction - adding item direct to transactional update cache: \n" +
                        "   cache: " + this + "\n" +
                        "   key: " + key + "\n" +
                        "   value: " + value);
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
            if (logger.isDebugEnabled())
            {
                logger.debug("No transaction - removing item from shared cache: \n" +
                        "   cache: " + this + "\n" +
                        "   key: " + key);
            }
        }
        else  // transaction present
        {
            TransactionData txnData = getTransactionData();
            // is the shared cache going to be cleared?
            if (txnData.isClearOn)
            {
                // don't store removals
            }
            else
            {
                // are we in an overflow condition?
                if (txnData.removedItemsCache.getMemoryStoreSize() >= maxCacheSize)
                {
                    // overflow about to occur or has occured - we can only guarantee non-stale
                    // data by clearing the shared cache after the transaction.  Also, the
                    // shared cache needs to be ignored for the rest of the transaction.
                    txnData.isClearOn = true;
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("In transaction - removal cache reach capacity reached: \n" +
                                "   cache: " + this + "\n" +
                                "   txn: " + AlfrescoTransactionSupport.getTransactionId());
                    }
                }
                else
                {
                    // add it from the removed cache for this txn
                    Element element = new Element(key, VALUE_DELETE);
                    txnData.removedItemsCache.put(element);
                }
            }
            // remove the item from the udpated cache, if present
            txnData.updatedItemsCache.remove(key);
            // done
            if (logger.isDebugEnabled())
            {
                logger.debug("In transaction - adding item direct to transactional removed cache: \n" +
                        "   cache: " + this + "\n" +
                        "   key: " + key);
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
            if (logger.isDebugEnabled())
            {
                logger.debug("In transaction clearing cache: \n" +
                        "   cache: " + this + "\n" +
                        "   txn: " + AlfrescoTransactionSupport.getTransactionId());
            }
            
            TransactionData txnData = getTransactionData();
            // the shared cache must be cleared at the end of the transaction
            // and also serves to ensure that the shared cache will be ignored
            // for the remainder of the transaction
            txnData.isClearOn = true;
            txnData.updatedItemsCache.removeAll();
            txnData.removedItemsCache.removeAll();
        }
        else            // no transaction
        {
            if (logger.isDebugEnabled())
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

    public void beforeCommit(boolean readOnly)
    {
    }

    public void beforeCompletion()
    {
    }

    /**
     * Merge the transactional caches into the shared cache
     */
    @SuppressWarnings("unchecked")
    public void afterCommit()
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Processing end of transaction commit");
        }
        
        TransactionData txnData = getTransactionData();
        try
        {
            if (txnData.isClearOn)
            {
                // clear shared cache
                sharedCache.clear();
                if (logger.isDebugEnabled())
                {
                    logger.debug("Clear notification recieved at end of transaction - clearing shared cache");
                }
            }
            else
            {
                // transfer any removed items
                // any removed items will have also been removed from the in-transaction updates
                // propogate the deletes to the shared cache
                List<Serializable> keys = txnData.removedItemsCache.getKeys();
                for (Serializable key : keys)
                {
                    sharedCache.remove(key);
                }
                if (logger.isDebugEnabled())
                {
                    logger.debug("Removed " + keys.size() + " values from shared cache");
                }
            }

            // transfer updates
            List<Serializable> keys = txnData.updatedItemsCache.getKeys();
            for (Serializable key : keys)
            {
                Element element = txnData.updatedItemsCache.get(key);
                sharedCache.put(key, element.getValue());
            }
            if (logger.isDebugEnabled())
            {
                logger.debug("Added " + keys.size() + " values to shared cache");
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
     * Just allow the transactional caches to be thrown away
     */
    public void afterRollback()
    {
        TransactionData txnData = getTransactionData();
        // drop caches from cachemanager
        removeCaches(txnData);
    }
    
    /**
     * Ensures that the transactional caches are removed from the common cache manager.
     * 
     * @param txnData the data with references to the the transactional caches
     */
    private void removeCaches(TransactionData txnData)
    {
        cacheManager.removeCache(txnData.updatedItemsCache.getName());
        cacheManager.removeCache(txnData.removedItemsCache.getName());
    }
    
    /** Data holder to bind data to the transaction */
    private class TransactionData
    {
        public Cache updatedItemsCache;
        public Cache removedItemsCache;
        public boolean isClearOn;
    }
}
