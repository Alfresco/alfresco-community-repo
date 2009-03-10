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

import java.rmi.RemoteException;
import java.util.List;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.distribution.CachePeer;

import org.alfresco.repo.jgroups.AlfrescoJGroupsChannelFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jgroups.Address;
import org.jgroups.Channel;
import org.jgroups.Message;

/**
 * Sends heartbeats to a JGroups cluster containing a list of RMI URLs.
 * 
 * @author Derek Hulley
 * @since 2.1.3
 */
public final class JGroupsKeepAliveHeartbeatSender
{
    private static Log logger = LogFactory.getLog(JGroupsKeepAliveHeartbeatSender.class);

    public static final String URL_DELIMITER = "|";

    /** Used to detect the necessary changes to the outgoing heartbeat messages */
    private static String lastHeartbeatSendUrls = "";
    public static synchronized String getLastHeartbeatSendUrls()
    {
        return lastHeartbeatSendUrls;
    }
    public static synchronized void setLastHeartbeatSendUrls(String heartbeatUrls)
    {
        lastHeartbeatSendUrls = heartbeatUrls;
    }
    
    private final CacheManager cacheManager;
    private final Channel heartbeatChannel;
    private long heartBeatInterval;
    private boolean stopped;
    private HeartbeatSenderThread serverThread;
    private Address heartbeatSourceAddress;

    /**
     * @param cacheManager          the bound CacheManager
     * @param heartbeatChannel      the cluster channel to use
     * @param heartBeatInterval     the time between heartbeats
     */
    public JGroupsKeepAliveHeartbeatSender(
            CacheManager cacheManager,
            Channel heartbeatChannel,
            long heartBeatInterval)
    {
        
        this.cacheManager = cacheManager;
        this.heartbeatChannel = heartbeatChannel;
        this.heartBeatInterval = heartBeatInterval;
    }

    /**
     * @return      Return the heartbeat interval (milliseconds)
     */
    public long getHeartBeatInterval()
    {
        return heartBeatInterval;
    }

    /**
     * @return      Returns the last heartbeat source address
     */
    /*package*/ synchronized Address getHeartbeatSourceAddress()
    {
        return heartbeatSourceAddress;
    }
    /**
     * @param heartbeatSourceAddress    the source address
     */
    private synchronized void setHeartbeatSourceAddress(Address heartbeatSourceAddress)
    {
        this.heartbeatSourceAddress = heartbeatSourceAddress;
    }
    
    /**
     * Start the heartbeat thread
     */
    public void init()
    {
        serverThread = new HeartbeatSenderThread();
        serverThread.start();
    }

    /**
     * Shutdown this heartbeat sender
     */
    public final synchronized void dispose()
    {
        stopped = true;
        notifyAll();
        serverThread.interrupt();
    }

    /**
     * A thread which sends a multicast heartbeat every second
     */
    private class HeartbeatSenderThread extends Thread
    {
        private Message heartBeatMessage;
        private int lastCachePeersHash;

        /**
         * Constructor
         */
        public HeartbeatSenderThread()
        {
            super("JGroupsKeepAliveHeartbeatSender");
            setDaemon(true);
        }

        public final void run()
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("\n" +
                        "Starting cache peer URLs heartbeat");
            }
            while (!stopped)
            {
                try
                {
                    if (AlfrescoJGroupsChannelFactory.isClusterActive())
                    {
                        Message message = getCachePeersMessage();
                        if (logger.isDebugEnabled())
                        {
                            logger.debug("\n" +
                                    "Sending cache peer URLs heartbeat: \n" +
                                    "   Message: " + message + "\n" +
                                    "   Peers:   " + new String(message.getBuffer()));
                        }
                        heartbeatChannel.send(message);
                        // Record the source address
                        setHeartbeatSourceAddress(message.getSrc());
                    }
                }
                catch (Throwable e)
                {
                    logger.debug("Heartbeat sending failed: ", e);
                }
                // Wait for the next heartbeat
                synchronized (this)
                {
                    try
                    {
                        wait(heartBeatInterval);
                    }
                    catch (InterruptedException e)
                    {
                    }
                }
            }
        }

        /**
         * Gets the message containing the peer URLs to send in the next heartbeat.
         */
        private Message getCachePeersMessage()
        {
            @SuppressWarnings("unchecked")
            List<CachePeer> localCachePeers = cacheManager.getCachePeerListener().getBoundCachePeers();
            int newCachePeersHash = localCachePeers.hashCode();
            if (heartBeatMessage == null || lastCachePeersHash != newCachePeersHash)
            {
                lastCachePeersHash = newCachePeersHash;

                String peerUrls = assembleUrlList(localCachePeers);
                JGroupsKeepAliveHeartbeatSender.setLastHeartbeatSendUrls(peerUrls);
                // Convert to message
                byte[] peerUrlsBytes = peerUrls.getBytes();
                heartBeatMessage = new Message(null, null, peerUrlsBytes);
                if (logger.isDebugEnabled())
                {
                    logger.debug("\n" +
                            "Heartbeat message updated: \n" +
                            "   URLs:    " + peerUrls + "\n" +
                            "   Message: " + heartBeatMessage);
                }
            }
            return heartBeatMessage;
        }
        
        /**
         * Builds a String of cache peer URLs of the form url1|url2|url3
         */
        public String assembleUrlList(List<CachePeer> localCachePeers)
        {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < localCachePeers.size(); i++)
            {
                CachePeer cachePeer = (CachePeer) localCachePeers.get(i);
                String rmiUrl = null;
                try
                {
                    rmiUrl = cachePeer.getUrl();
                }
                catch (RemoteException e)
                {
                    logger.error("This should never be thrown as it is called locally");
                }
                if (i != localCachePeers.size() - 1)
                {
                    sb.append(rmiUrl).append(URL_DELIMITER);
                }
                else
                {
                    sb.append(rmiUrl);
                }
            }
            return sb.toString();
        }
    }
}
