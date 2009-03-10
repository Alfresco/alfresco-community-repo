/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
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
package org.alfresco.repo.cache.jgroups;

import java.io.IOException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.distribution.CacheManagerPeerProvider;
import net.sf.ehcache.distribution.CacheManagerPeerProviderFactory;
import net.sf.ehcache.distribution.CachePeer;

import org.alfresco.repo.jgroups.AlfrescoJGroupsChannelFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jgroups.Channel;

/**
 * A cache peer provider that does heartbeat sending and receiving using JGroups.
 * 
 * @author Derek Hulley
 * @since 2.1.3
 */
public class JGroupsRMICacheManagerPeerProvider implements CacheManagerPeerProvider
{
    public static final int DEFAULT_HEARTBEAT_INTERVAL = 5000;
    public static final int MINIMUM_HEARTBEAT_INTERVAL = 1000;
    /**
     * the heartbeat signal time in milliseconds.<br/>
     * The default is {@link #DEFAULT_HEARTBEAT_INTERVAL}.
     */
    public static final String PROP_HEARTBEAT_INTERVAL = "heartbeatInterval";
    
    private static Log logger = LogFactory.getLog(JGroupsRMICacheManagerPeerProvider.class);
    
    private final JGroupsKeepAliveHeartbeatSender heartbeatSender;
    private final JGroupsKeepAliveHeartbeatReceiver heartbeatReceiver;
    
    /**
     * Store the entries referenced first by cache name and then by the RMI URL.
     * This looks like duplicated data, but the fastest lookup needs to be a retrieval of
     * the list of URLs for a given cache.  All other access is infrequent.
     */
    private final Map<String, Map<String, CachePeerEntry>> cachePeersByUrlByCacheName;
    
    private final long staleAge;
    private final ReadLock peersReadLock;
    private final WriteLock peersWriteLock;

    public JGroupsRMICacheManagerPeerProvider(CacheManager cacheManager, Channel heartbeatChannel, long heartbeatInterval)
    {
        this.heartbeatSender = new JGroupsKeepAliveHeartbeatSender(cacheManager, heartbeatChannel, heartbeatInterval);
        this.heartbeatReceiver = new JGroupsKeepAliveHeartbeatReceiver(this, heartbeatSender, heartbeatChannel);
        
        cachePeersByUrlByCacheName = new HashMap<String, Map<String, CachePeerEntry>>(103);
        
        ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock(true);
        peersReadLock = readWriteLock.readLock();
        peersWriteLock = readWriteLock.writeLock();
        
        // Calculate the age that a peer entry must be before we consider it stale.
        // This is the method used in the Multicast EHCache code, so I guess it must be OK...
        // Ofcourse, it's better to send to peers that are broken than to evict peers when they
        // are just having some trouble sending their heartbeats for some reason.
        staleAge = (heartbeatInterval * 2 + 100) * 1000 * 1000;
    }

    public void init()
    {
        heartbeatSender.init();
        heartbeatReceiver.init();
    }

    private String extractCacheName(String rmiUrl)
    {
        return rmiUrl.substring(rmiUrl.lastIndexOf('/') + 1);
    }

    /**
     * Lazy map creation using appropriate synchronization
     * @return      the entry if it exists otherwise <tt>null</tt>
     */
    private CachePeerEntry getCachePeerEntry(String cacheName, String rmiUrl)
    {
        Map<String, CachePeerEntry> peerEntriesByUrl = getPeerEntriesByUrl(cacheName);
        
        peersReadLock.lock();
        try
        {
            return peerEntriesByUrl.get(rmiUrl);
        }
        finally
        {
            peersReadLock.unlock();
        }
    }
    
    /**
     * Lazy map creation using appropriate synchronization
     * @return      never null
     */
    private Map<String, CachePeerEntry> getPeerEntriesByUrl(String cacheName)
    {
        Map<String, CachePeerEntry> peerEntriesByUrl;
        peersReadLock.lock();
        try
        {
            peerEntriesByUrl = cachePeersByUrlByCacheName.get(cacheName);
            if (peerEntriesByUrl != null)
            {
                return peerEntriesByUrl;
            }
        }
        finally
        {
            peersReadLock.unlock();
        }
        peersWriteLock.lock();
        try
        {
            // Double check in the write lock
            peerEntriesByUrl = cachePeersByUrlByCacheName.get(cacheName);
            if (peerEntriesByUrl != null)
            {
                return peerEntriesByUrl;
            }
            peerEntriesByUrl = new HashMap<String, CachePeerEntry>(7);
            cachePeersByUrlByCacheName.put(cacheName, peerEntriesByUrl);
            return peerEntriesByUrl;
        }
        finally
        {
            peersWriteLock.unlock();
        }
    }
    
    /**
     * Performs the actual RMI setup necessary to create a cache peer.
     * @param rmiUrl            the RMI URL of the peer
     * @return                  Returns the cache peer
     */
    /* Always called from a write block */
    private /*synchronized*/ CachePeer registerPeerImpl(String rmiUrl)
    {
        try
        {
            CachePeer cachePeer = (CachePeer) Naming.lookup(rmiUrl);
            return cachePeer;
        }
        catch (NotBoundException e)         // Pretty ordinary
        {
            if (logger.isDebugEnabled())
            {
                logger.debug(
                        "Unable to lookup remote cache peer for " + rmiUrl + ".  " +
                        "Removing from peer list.",
                        e);
            }
        }
        catch (IOException e)               // Some network issue
        {
            if (logger.isDebugEnabled())
            {
                logger.debug(
                        "Unable to lookup remote cache peer for " + rmiUrl + ".  " +
                        "Removing from peer list.",
                        e);
            }
        }
        catch (Throwable e)                 // More serious
        {
            logger.error(
                    "Unable to lookup remote cache peer for " + rmiUrl + ".  " +
                    "Removing from peer list.",
                    e);
        }
        // Only errors
        return null;
    }
    
    public void registerPeer(String rmiUrl)
    {
        String cacheName = extractCacheName(rmiUrl);
        CachePeerEntry peerEntry = getCachePeerEntry(cacheName, rmiUrl);
        if (peerEntry != null && !peerEntry.isStale(staleAge))
        {
            // It is already there and is still current
            peerEntry.updateTimestamp();
            return;
        }
        // There is no entry
        peersWriteLock.lock();
        try
        {
            // Double check
            peerEntry = getCachePeerEntry(cacheName, rmiUrl);
            if (peerEntry != null && !peerEntry.isStale(staleAge))
            {
                // It has just appeared.  Ofcourse, it will be current
                peerEntry.updateTimestamp();
                return;
            }
            // Create a new one
            CachePeer cachePeer = registerPeerImpl(rmiUrl);
            if (cachePeer == null)
            {
                // It can be null, ie. the RMI URL is not valid.
                // This is not an error and we just ignore it
                return;
            }
            // Cache it
            peerEntry = new CachePeerEntry(cachePeer, rmiUrl);
            Map<String, CachePeerEntry> peerEntriesByUrl = getPeerEntriesByUrl(cacheName);
            peerEntriesByUrl.put(rmiUrl, peerEntry);
            // Done
            if (logger.isDebugEnabled())
            {
                logger.debug("Registered new cache peer with URL: " + rmiUrl);
            }
        }
        finally
        {
            peersWriteLock.unlock();
        }
    }

    public void unregisterPeer(String rmiUrl)
    {
        String cacheName = extractCacheName(rmiUrl);
        Map<String, CachePeerEntry> peerEntriesByUrl = getPeerEntriesByUrl(cacheName);
        peersWriteLock.lock();
        try
        {
            peerEntriesByUrl.remove(rmiUrl);
            // Done
            if (logger.isDebugEnabled())
            {
                logger.debug("Unregistered cache peer with URL: " + rmiUrl);
            }
        }
        finally
        {
            peersWriteLock.unlock();
        }
    }

    public List<CachePeer> listRemoteCachePeers(Ehcache cache) throws CacheException
    {
        String cacheName = cache.getName();
        Map<String, CachePeerEntry> peerEntriesByUrl = getPeerEntriesByUrl(cacheName);
        List<CachePeer> cachePeers = new ArrayList<CachePeer>(peerEntriesByUrl.size());
        List<String> staleUrlEntries = null;
        peersReadLock.lock();
        try
        {
            for (CachePeerEntry peerEntry : peerEntriesByUrl.values())
            {
                if (peerEntry.isStale(staleAge))
                {
                    if (staleUrlEntries == null)
                    {
                        staleUrlEntries = new ArrayList<String>(4);
                    }
                    // Old
                    continue;
                }
                cachePeers.add(peerEntry.getCachePeer());
            }
        }
        finally
        {
            peersReadLock.unlock();
        }
        // Clean up stale URL entries
        if (staleUrlEntries != null)
        {
            for (String rmiUrl : staleUrlEntries)
            {
                unregisterPeer(rmiUrl);
            }
        }
        // Done
        return cachePeers;
    }
    
    protected boolean stale(Date date)
    {
        throw new UnsupportedOperationException();
    }

    public long getTimeForClusterToForm()
    {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Drops all the peer references
     */
    public void dispose() throws CacheException
    {
        peersWriteLock.lock();
        try
        {
            //TODO
        }
        finally
        {
            peersWriteLock.unlock();
        }
    }
    
    /**
     * A factory that can be given in the ehcache-default.xml or ehcache-custom.xml.
     * When using this factory, it is not necessary to provide individual cache
     * cluster configurations provided that the Alfresco bootstrap class, AlfrescoEhCacheBootstrap,
     * is used.
     * 
     * @author Derek Hulley
     * @since 2.1.3
     */
    public static class Factory extends CacheManagerPeerProviderFactory
    {
        @Override
        public CacheManagerPeerProvider createCachePeerProvider(CacheManager cacheManager, Properties properties)
        {
            if (properties == null)
            {
                properties = new Properties();
            }
            
            Channel channel = AlfrescoJGroupsChannelFactory.getChannel(AlfrescoJGroupsChannelFactory.APP_REGION_EHCACHE_HEARTBEAT);
            
            long heartbeatInterval = DEFAULT_HEARTBEAT_INTERVAL;
            try
            {
                String heartBeatIntervalStr = properties.getProperty(PROP_HEARTBEAT_INTERVAL);
                if (heartBeatIntervalStr != null)
                {
                    heartbeatInterval = Long.parseLong(heartBeatIntervalStr);
                }
            }
            catch (NumberFormatException e)
            {
                throw new RuntimeException(
                        "The property " + PROP_HEARTBEAT_INTERVAL +
                        " must be a valid integer greater than " + MINIMUM_HEARTBEAT_INTERVAL);
            }
            
            if (heartbeatInterval < MINIMUM_HEARTBEAT_INTERVAL)
            {
                throw new RuntimeException(
                        "The minimum value for property " + PROP_HEARTBEAT_INTERVAL +
                        " is " + MINIMUM_HEARTBEAT_INTERVAL + "ms");
            }
            return new JGroupsRMICacheManagerPeerProvider(cacheManager, channel, heartbeatInterval);
        }
    }
    
    /**
     * Map entry to keep references to EHCache peers along with the necessary timestamping.
     * 
     * @author Derek Hulley
     * @since 2.1.3
     */
    public static class CachePeerEntry
    {
        private final CachePeer cachePeer;
        private String rmiUrl;
        private long timestamp;
        
        /**
         * @param cachePeer         the remote cache peer
         * @param rmiUrl            the RMI URL for the peer
         */
        public CachePeerEntry(CachePeer cachePeer, String rmiUrl)
        {
            this.cachePeer = cachePeer;
            this.rmiUrl = rmiUrl;
            this.timestamp = System.nanoTime();
        }
        
        @Override
        public String toString()
        {
            return rmiUrl;
        }

        public CachePeer getCachePeer()
        {
            return cachePeer;
        }

        public String getRmiUrl()
        {
            return rmiUrl;
        }

        /**
         * Refreshes the peer's timestamp.
         */
        public void updateTimestamp()
        {
            timestamp = System.nanoTime();
        }
        
        /**
         * @param age   the maximum age (nanoseconds) before the peer is considered old
         * @return      Returns <tt>true</tt> if the cache peer is older than the given time (nanoseconds) 
         */
        public boolean isStale(long age)
        {
            return (System.nanoTime() - age) > timestamp;
        }
    }
}
