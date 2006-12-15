/**
 * 
 */
package org.alfresco.repo.avm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.alfresco.repo.avm.util.SimplePath;
import org.apache.log4j.Logger;

/**
 * All lookup traffic goes through here.
 * @author britt
 */
public class LookupCache 
{
    private static Logger fgLogger = Logger.getLogger(LookupCache.class);
    
    
    /**
     * Per transaction lookup results to be added to the cache on successful
     * commit.
     */
    private ThreadLocal<Map<LookupKey, Lookup>> fToBeAdded;
    
    /**
     * Per transaction set of invalidated lookup keys.
     */
    private ThreadLocal<Set<LookupKey>> fToBePurged;
    
    /**
     * The Map of of keys to lookups.
     */
    private Map<LookupKey, Lookup> fCache;
    
    /**
     * The Map of time stamps to keys.
     */
    private SortedMap<Long, LookupKey> fTimeStamps;
    
    /**
     * The inverse map of keys to timestamps.
     */
    private Map<LookupKey, Long> fInverseTimeStamps;
    
    /**
     * The timestamp to next issue.
     */
    private long fTimeStamp;
    
    /**
     * The maximum number of lines to have in the cache.
     */
    private int fMaxSize;
    
    /**
     * Reference to the Node DAO.
     */
    private AVMNodeDAO fAVMNodeDAO;
    
    /**
     * Reference to the Store DAO.
     */
    private AVMStoreDAO fAVMStoreDAO;
    
    /**
     * Make one up.
     */
    public LookupCache()
    {
        fCache = new HashMap<LookupKey, Lookup>(); 
        fTimeStamps = new TreeMap<Long, LookupKey>();
        fInverseTimeStamps = new HashMap<LookupKey, Long>();
        fToBeAdded = new ThreadLocal<Map<LookupKey, Lookup>>();
        fToBePurged = new ThreadLocal<Set<LookupKey>>();
        fTimeStamp = 0L;
        fMaxSize = 100;
    }
    
    /**
     * Set up the node dao.
     * @param dao The dao to set.
     */
    public void setAvmNodeDAO(AVMNodeDAO dao)
    {
        fAVMNodeDAO = dao;
    }
    
    /**
     * Set the store dao.
     * @param dao The dao to set.
     */
    public void setAvmStoreDAO(AVMStoreDAO dao)
    {
        fAVMStoreDAO = dao;        
    }
    
    /**
     * Set the maximum cache size.
     * @param maxSize
     */
    public void setMaxSize(int maxSize)
    {
        fMaxSize = maxSize;
    }
    
    /**
     * Lookup a path. Try to fulfill the request from the cache.
     * @param store The AVMStore.
     * @param version The versions.
     * @param path The path we are looking up.
     * @param write Whether this is a write lookup.
     * @param includeDeleted
     * @return
     */
    public Lookup lookup(AVMStore store, int version, SimplePath path, 
                         boolean write, boolean includeDeleted)
    {
        // Create a key object.
        LookupKey key = new LookupKey(version, path, store.getName(), write, includeDeleted);
        // Is it in the cache?
        Lookup found = findInCache(key);
        if (found != null)
        {
            return found;
        }
        // Make up a Lookup to hold the results.
        if (path.size() == 0)
        {
            return null;
        }        
        Lookup result = new Lookup(store, store.getName());
        // Grab the root node to start the lookup.
        DirectoryNode dir = null;
        // Versions less than 0 mean get current.
        if (version < 0)
        {
            dir = store.getRoot();
        }
        else
        {
            dir = fAVMNodeDAO.getAVMStoreRoot(store, version);
        }
        if (dir == null)
        {
            return null;
        }
        // Add an entry for the root.
        result.add(dir, "", write);
        dir = (DirectoryNode)result.getCurrentNode();
        if (path.size() == 1 && path.get(0).equals(""))
        {
            updateCache(key, result);
            return result;
        }
        // Now look up each path element in sequence up to one
        // before the end.
        for (int i = 0; i < path.size() - 1; i++)
        {
            AVMNode child = dir.lookupChild(result, path.get(i), includeDeleted);
            if (child == null)
            {
                return null;
            }
            // Every element that is not the last needs to be a directory.
            if (child.getType() != AVMNodeType.PLAIN_DIRECTORY &&
                child.getType() != AVMNodeType.LAYERED_DIRECTORY)
            {
                return null;
            }
            result.add(child, path.get(i), write);
            dir = (DirectoryNode)result.getCurrentNode();
        }
        // Now look up the last element.
        AVMNode child = dir.lookupChild(result, path.get(path.size() - 1),
                                        includeDeleted);
        if (child == null)
        {
            return null;
        }
        result.add(child, path.get(path.size() - 1), write);
        updateCache(key, result);
        return result;        
    }
    
    /**
     * Try to find a match in the cache.
     * @param key The lookup key.
     * @return A valid for this session Lookup or null if not found.
     */
    private synchronized Lookup findInCache(LookupKey key)
    {
        // Get the current transaction's purged set.
        Set<LookupKey> purged = fToBePurged.get();
        // Get the current transaction's added map.
        Map<LookupKey, Lookup> added = fToBeAdded.get();
        // See if it's cached in the transaction.
        Lookup found = (added != null) ? added.get(key) : null;
        // It's not.
        if (found == null)
        {
            // If it's been purged in the transaction it is 
            // a miss.
            if (purged != null && purged.contains(key))
            {
                return null;
            }
            found = fCache.get(key);
        }
        // Despite the odds, we found a hit.
        if (found != null)
        {
            // Get a freshened Lookup.
            Lookup result = new Lookup(found, fAVMNodeDAO, fAVMStoreDAO);
            // Check that nothing horrible is wrong.  This should
            // be assertible, but I'll leave the check in for now.
            if (!result.isValid())
            {
                fgLogger.error("Invalid entry in cache: " + key);
                return null;
            }
            // Prepare the cache for a timestamp update on commit.
            updateCache(key, found);
            return result;
        }
        // Alternatively for a read lookup a write can match.
        if (!key.isWrite())
        {
            // Make a copy of the key and set it to 'write'
            LookupKey newKey = new LookupKey(key);
            newKey.setWrite(true);
            // Is it in the transaction's cache?
            found = (added != null) ? added.get(newKey) : null;
            // If not.
            if (found == null)
            {
                // If it's been purged it's a miss.
                if (purged != null && purged.contains(newKey))
                {
                    return null;
                }
                found = fCache.get(newKey);
            }
            if (found != null)
            {
                // We found it. Make a freshened copy of the Lookup.
                Lookup result = new Lookup(found, fAVMNodeDAO, fAVMStoreDAO);
                // Check for badness.  This should be assertible but I'll
                // leave the check in for now.
                if (!result.isValid())
                {
                    fgLogger.error("Invalid entry in cache: " + newKey);
                    return null;
                }
                // Prepare the cache to update time stamp.
                updateCache(newKey, found);
                return result;
            }
        }
        return null;
    }
    
    /**
     * Add or update an entry in the cache.
     * @param key
     * @param lookup
     */
    private void updateCache(LookupKey key, Lookup lookup)
    {
        // First, put it in the transaction scoped cache.
        Map<LookupKey, Lookup> map = fToBeAdded.get();
        if (map == null)
        {
            map = new HashMap<LookupKey, Lookup>();
        }
        map.put(key, lookup);
        // Remove any corresponding entry from the purge list.
        Set<LookupKey> purged = fToBePurged.get();
        if (purged == null)
        {
            return;
        }
        purged.remove(key);
    }     
   
    /**
     * Called when a transaction has successfully committed,
     * to make lookups from the transaction available to other transactions.
     */
    public synchronized void onCommit()
    {
        // First get rid of all entries purged by the transaction.
        Set<LookupKey> purged = fToBePurged.get();
        if (purged != null)
        {
            purgeEntries(purged);
        }
        // Null out the thread local.
        fToBePurged.set(null);
        // Get and go through the transaction's added list.
        Map<LookupKey, Lookup> added = fToBeAdded.get();
        if (added == null)
        {
            return;
        }
        for (Map.Entry<LookupKey, Lookup> entry : added.entrySet())
        {
            LookupKey key = entry.getKey();
            Lookup lookup = entry.getValue();
            // If the cache already has the key, remove it.
            if (fCache.containsKey(key))
            {
                fCache.remove(key);
                Long oldTime = fInverseTimeStamps.get(key);
                fInverseTimeStamps.remove(key);
                fTimeStamps.remove(oldTime);
            }
            // Add the entry.
            long timeStamp = fTimeStamp++;
            fTimeStamps.put(timeStamp, key);
            fInverseTimeStamps.put(key, timeStamp);
            fCache.put(key, lookup);
            // Check if we're over the limit and purge the 
            // LRU entry if we are.
            if (fCache.size() > fMaxSize)
            {
                // Get rid of the oldest entry.
                Long oldTime = fTimeStamps.firstKey();
                LookupKey old = fTimeStamps.remove(oldTime);
                fInverseTimeStamps.remove(old);
                fCache.remove(old);
            }
        }
        // Null out the ThreadLocal.
        fToBeAdded.set(null);
    }
    
    /**
     * Remove a Set of entries.
     * @param keys The Set of entries.
     */
    private void purgeEntries(Set<LookupKey> keys)
    {
        for (LookupKey key : keys)
        {
            fCache.remove(key);
            Long time = fInverseTimeStamps.remove(key);
            fTimeStamps.remove(time);
        }
    }
    
    // Following are the cache invalidation calls.
    
    /**
     * Called when a simple write operation occurs.  This
     * invalidates all read lookups and all layered lookups.
     */
    public synchronized void onWrite(String storeName)
    {
        // Get or make up the purged Set for this transaction.
        Set<LookupKey> purged = fToBePurged.get();
        if (purged == null)
        {
            purged = new HashSet<LookupKey>();
            fToBePurged.set(purged);
        }
        // Invalidate if it's a read lookup in the store, or
        // any read lookup is it's layered.
        for (Map.Entry<LookupKey, Lookup> entry : fCache.entrySet())
        {
            if ((entry.getKey().getStoreName().equals(storeName) &&
                !entry.getKey().isWrite()) || 
                (!entry.getKey().isWrite() && entry.getValue().isLayered()))
            {
                purged.add(entry.getKey());
            }
        }
        // Remove entries from the added set using the same criteria.
        Map<LookupKey, Lookup> added = fToBeAdded.get();
        if (added == null)
        {
            return;
        }
        for (Map.Entry<LookupKey, Lookup> entry : added.entrySet())
        {
            if ((entry.getKey().getStoreName().equals(storeName) &&
                 !entry.getKey().isWrite()) || 
                 (!entry.getKey().isWrite() && entry.getValue().isLayered()))
            {
                added.remove(entry.getKey());
            }
        }
    }
    
    /**
     * Called when a delete has occurred in a store.  This invalidates both
     * reads and write lookups in that store.
     */
    public synchronized void onDelete(String storeName)
    {
        // Get or make up a fresh purged Set.
        Set<LookupKey> purged = fToBePurged.get();
        if (purged == null)
        {
            purged = new HashSet<LookupKey>();
            fToBePurged.set(purged);
        }
        // Invalidate any entries that are in the store or are layered lookups.
        for (Map.Entry<LookupKey, Lookup> entry : fCache.entrySet())
        {
            if (entry.getKey().getStoreName().equals(storeName) || 
                entry.getValue().isLayered())
            {
                purged.add(entry.getKey());
            }
        }
        // Get rid of any similarly matching elements in the added list.
        Map<LookupKey, Lookup> added = fToBeAdded.get();
        if (added == null)
        {
            return;
        }
        for (Map.Entry<LookupKey, Lookup> entry : added.entrySet())
        {
            if (entry.getKey().getStoreName().equals(storeName) || 
                entry.getValue().isLayered())
            {
                added.remove(entry.getKey());
            }
        }
    }
    
    /**
     * Called when a snapshot occurs in a store.  This invalidates write 
     * lookups.  Read lookups stay untouched.
     */
    public synchronized void onSnapshot(String storeName)
    {
        // Get or make up a purged set.
        Set<LookupKey> purged = fToBePurged.get();
        if (purged == null)
        {
            purged = new HashSet<LookupKey>();
            fToBePurged.set(purged);
        }
        // Invalidate any entries that in the store and writes or
        // any layered lookups.
        for (Map.Entry<LookupKey, Lookup> entry : fCache.entrySet())
        {
            if ((entry.getKey().getStoreName().equals(storeName) &&
                 entry.getKey().isWrite()) ||
                 entry.getValue().isLayered())
            {
                purged.add(entry.getKey());
            }
        }
        // Remove from the added list by the same criteria.
        Map<LookupKey, Lookup> added = fToBeAdded.get();
        if (added == null)
        {
            return;
        }
        for (Map.Entry<LookupKey, Lookup> entry : added.entrySet())
        {
            if ((entry.getKey().getStoreName().equals(storeName) &&
                 entry.getKey().isWrite()) ||
                 entry.getValue().isLayered())
            {
                added.remove(entry.getKey());
            }
        }
    }
    
    /**
     * Called when a rollback has occurred.
     */
    public synchronized void onRollback()
    {
        // Just toss the transaction level changes.
        fToBeAdded.set(null);
        fToBePurged.set(null);
    }
}
