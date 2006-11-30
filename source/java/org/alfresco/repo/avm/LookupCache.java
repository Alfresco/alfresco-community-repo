/**
 * 
 */
package org.alfresco.repo.avm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        LookupKey key = new LookupKey(version, path, store.getName(), write, includeDeleted);
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
        Lookup found = fCache.get(key);
        if (found != null)
        {
            Lookup result = new Lookup(found, fAVMNodeDAO, fAVMStoreDAO);
            if (!result.isValid())
            {
                fgLogger.error("Invalid entry in cache: " + key);
                onRollback();
                return null;
            }
            updateCache(key, found);
            return result;
        }
        // Alternatively for a read lookup a write can match.
        if (!key.isWrite())
        {
            LookupKey newKey = new LookupKey(key);
            newKey.setWrite(true);
            found = fCache.get(newKey);
            if (found != null)
            {
                Lookup result = new Lookup(found, fAVMNodeDAO, fAVMStoreDAO);
                if (!result.isValid())
                {
                    fgLogger.error("Invalid entry in cache: " + newKey);
                    onRollback();
                    return null;
                }
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
    private synchronized void updateCache(LookupKey key, Lookup lookup)
    {
        if (fCache.containsKey(key))
        {
            fCache.remove(key);
            Long oldTime = fInverseTimeStamps.get(key);
            fInverseTimeStamps.remove(key);
            fTimeStamps.remove(oldTime);
        }
        long timeStamp = fTimeStamp++;
        fTimeStamps.put(timeStamp, key);
        fInverseTimeStamps.put(key, timeStamp);
        fCache.put(key, lookup);
        if (fCache.size() > fMaxSize)
        {
            // Get rid of the oldest entry.
            Long oldTime = fTimeStamps.firstKey();
            LookupKey old = fTimeStamps.remove(oldTime);
            fInverseTimeStamps.remove(old);
            fCache.remove(old);
        }
    }
    
    /**
     * Remove a List of entries.
     * @param keys The List of entries.
     */
    private void purgeEntries(List<LookupKey> keys)
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
        List<LookupKey> toDelete = new ArrayList<LookupKey>();
        for (Map.Entry<LookupKey, Lookup> entry : fCache.entrySet())
        {
            if ((entry.getKey().getStoreName().equals(storeName) &&
                !entry.getKey().isWrite()) || 
                (!entry.getKey().isWrite() && entry.getValue().isLayered()))
            {
                toDelete.add(entry.getKey());
            }
        }
        purgeEntries(toDelete);
    }
    
    /**
     * Called when a delete has occurred in a store.  This invalidates both
     * reads and write lookups in that store.
     */
    public synchronized void onDelete(String storeName)
    {
        List<LookupKey> toDelete = new ArrayList<LookupKey>();
        for (Map.Entry<LookupKey, Lookup> entry : fCache.entrySet())
        {
            if (entry.getKey().getStoreName().equals(storeName) || 
                entry.getValue().isLayered())
            {
                toDelete.add(entry.getKey());
            }
        }
        purgeEntries(toDelete);
    }
    
    /**
     * Called when a snapshot occurs in a store.  This invalidates write 
     * lookups.  Read lookups stay untouched.
     */
    public synchronized void onSnapshot(String storeName)
    {
        List<LookupKey> toDelete = new ArrayList<LookupKey>();
        for (Map.Entry<LookupKey, Lookup> entry : fCache.entrySet())
        {
            if ((entry.getKey().getStoreName().equals(storeName) &&
                 entry.getKey().isWrite()) ||
                entry.getValue().isLayered())
            {
                toDelete.add(entry.getKey());
            }
        }
        purgeEntries(toDelete);
    }
    
    /**
     * Called when a rollback has occurred.  This invalidates the entire
     * cache.  Heavy handed but quick.
     */
    public synchronized void onRollback()
    {
        fCache.clear();
        fTimeStamps.clear();
        fInverseTimeStamps.clear();
    }
}
