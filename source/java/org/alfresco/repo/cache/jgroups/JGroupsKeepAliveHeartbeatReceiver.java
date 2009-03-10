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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.alfresco.util.EqualsHelper;
import org.alfresco.util.TraceableThreadFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jgroups.Address;
import org.jgroups.Channel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;

/**
 * Receives heartbeats from the {@link JGroupsKeepAliveHeartbeatSender JGroups heartbeat sender}.
 *
 * @author Derek Hulley
 * @since 2.1.3
 */
public class JGroupsKeepAliveHeartbeatReceiver extends ReceiverAdapter
{
    private static final int MAX_THREADS = 5;
    
    private static Log logger = LogFactory.getLog(JGroupsKeepAliveHeartbeatReceiver.class);
    
    private final JGroupsRMICacheManagerPeerProvider peerProvider;
    private final JGroupsKeepAliveHeartbeatSender heartbeatSender;
    private final Channel channel;
    private boolean stopped;
    private View lastView;
    private final ThreadPoolExecutor threadPool;
    private final Set<String> rmiUrlsProcessingQueue;
    
    public JGroupsKeepAliveHeartbeatReceiver(
            JGroupsRMICacheManagerPeerProvider peerProvider,
            JGroupsKeepAliveHeartbeatSender heartbeatSender,
            Channel channel)
    {
        this.peerProvider = peerProvider;
        this.heartbeatSender = heartbeatSender;
        this.channel = channel;
        
        this.rmiUrlsProcessingQueue = Collections.synchronizedSet(new HashSet<String>());

        // Build the thread pool
        TraceableThreadFactory threadFactory = new TraceableThreadFactory();
        threadFactory.setThreadDaemon(true);
        threadFactory.setThreadPriority(Thread.NORM_PRIORITY + 2);
        
        this.threadPool = new ThreadPoolExecutor(
                1,                                          // Maintain one threads
                1,                                          // We'll increase it, if necessary
                60,                                         // 1 minute until unused threads get trashed
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(),
                threadFactory);
    }
    
    /**
     * Register to receive message on the channel
     */
    public void init()
    {
        channel.setReceiver(this);
    }
    
    /**
     * Set the stop flag.
     */
    public void dispose()
    {
        stopped = true;
    }

    @Override
    public byte[] getState()
    {
        return new byte[] {};
    }

    @Override
    public void setState(byte[] state)
    {
        // Nothing to do
    }

    @Override
    public void receive(Message message)
    {
        Address localAddress = heartbeatSender.getHeartbeatSourceAddress();
        Address remoteAddress = message.getSrc();
        // Ignore messages from ourselves
        if (EqualsHelper.nullSafeEquals(localAddress, remoteAddress))
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("\n" +
                        "Ignoring cache peeer URLs heartbeat from self: " + message);
            }
            return;
        }
        
        String rmiUrls = new String(message.getBuffer());
        if (logger.isDebugEnabled())
        {
            logger.debug("\n" +
                    "Received cache peer URLs heartbeat: \n" +
                    "   Message: " + message + "\n" +
                    "   Peers:   " + rmiUrls);
        }
        // Quickly split them up
        StringTokenizer tokenizer = new StringTokenizer(rmiUrls, JGroupsKeepAliveHeartbeatSender.URL_DELIMITER, false);
        while (!stopped && tokenizer.hasMoreTokens())
        {
            String rmiUrl = tokenizer.nextToken();
            // Is it pending?
            if (rmiUrlsProcessingQueue.add(rmiUrl))
            {
                // Not pending.  Shedule it.
                ProcessingRunnable runnable = new ProcessingRunnable(rmiUrl);
                threadPool.execute(runnable);
            }
            else
            {
                // It was already waiting to be processed
                // Increase the thread pool size
                int currentThreadPoolMaxSize = threadPool.getMaximumPoolSize();
                if (currentThreadPoolMaxSize < MAX_THREADS)
                {
                    threadPool.setMaximumPoolSize(currentThreadPoolMaxSize + 1);
                }
            }
        }
    }

    /**
     * Worker class to go into thread pool
     * 
     * @author Derek Hulley
     */
    private class ProcessingRunnable implements Runnable
    {
        private String rmiUrl;
        private ProcessingRunnable(String rmiUrl)
        {
            this.rmiUrl = rmiUrl;
        }
        public void run()
        {
            rmiUrlsProcessingQueue.remove(rmiUrl);
            if (stopped)
            {
                return;
            }
            peerProvider.registerPeer(rmiUrl);
        }
    }

    @Override
    public void viewAccepted(View newView)
    {
        if (EqualsHelper.nullSafeEquals(lastView, newView))
        {
            // No change, so ignore
            return;
        }
        int lastSize = (lastView == null) ? 0 : lastView.getMembers().size();
        int newSize = newView.getMembers().size();
        // Report
        if (newSize < lastSize)
        {
            logger.warn("\n" +
            		"New cluster view with fewer members: \n" +
            		"   Last View: " + lastView + "\n" +
            		"   New View:  " + newView);
        }
        else
        {
            logger.info("\n" +
                    "New cluster view with additional members: \n" +
                    "   Last View: " + lastView + "\n" +
                    "   New View:  " + newView);
        }
        lastView = newView;
    }
}
