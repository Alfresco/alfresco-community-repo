
package org.alfresco.repo.virtual.ref;

import java.util.HashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A synchronized string hash code mapping store.<br>
 * Associates a string with a given string-hash.
 */
public class HashStore
{

    private HashMap<String, String> hashStore = new HashMap<>();

    private HashMap<String, String> lookupStore = new HashMap<>();

    private ReentrantReadWriteLock configurationLock = new ReentrantReadWriteLock();

    public void put(String string, String hash)
    {
        configurationLock.writeLock().lock();
        try
        {
            hashStore.put(string,
                          hash);
            lookupStore.put(hash,
                            string);
        }
        finally
        {
            configurationLock.writeLock().unlock();
        }
    }

    public String hash(String string)
    {
        configurationLock.readLock().lock();
        try
        {
            return hashStore.get(string);
        }
        finally
        {
            configurationLock.readLock().unlock();
        }

    }

    public String lookup(String hash)
    {
        configurationLock.readLock().lock();
        try
        {
            return lookupStore.get(hash);
        }
        finally
        {
            configurationLock.readLock().unlock();
        }

    }
}
