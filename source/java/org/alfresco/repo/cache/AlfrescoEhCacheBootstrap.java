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
package org.alfresco.repo.cache;

import java.io.FileNotFoundException;
import java.net.URL;

import net.sf.ehcache.CacheManager;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.cache.jgroups.JGroupsRMICacheManagerPeerProvider;
import org.alfresco.util.AbstractLifecycleBean;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jgroups.JChannel;
import org.springframework.context.ApplicationEvent;
import org.springframework.util.ResourceUtils;

/**
 * A bootstrap bean that sets up the Alfresco-specific cache manager.
 * 
 * @author Derek Hulley
 * @since 2.1.3
 */
public class AlfrescoEhCacheBootstrap extends AbstractLifecycleBean
{
    private static Log logger = LogFactory.getLog(AlfrescoEhCacheBootstrap.class);

    private JChannel ehcacheHeartbeatChannel;
    private String clusterName;
    
    public AlfrescoEhCacheBootstrap()
    {
    }
    
    public void setEhcacheHeartbeatChannel(JChannel channel)
    {
        this.ehcacheHeartbeatChannel = channel;
    }
    
    public void setClusterName(String clusterName)
    {
        this.clusterName = clusterName;
    }

    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        try
        {
            CacheManager cacheManager = InternalEhCacheManagerFactoryBean.getInstance();
            // We need only proceed if the cache-manager doesn't already have a peer mechanims
            if( cacheManager.getCacheManagerPeerProvider() != null)
            {
                logger.info("Cache cluster config enabled using ehcache-custom.xml");
                return;
            }

            setupCaches(cacheManager);
        }
        catch (Throwable e)
        {
            throw new AlfrescoRuntimeException("Failed to bootstrap the EHCache cluster", e);
        }
    }

    @Override
    protected void onShutdown(ApplicationEvent event)
    {
        // Shut the cache manager down
        CacheManager cacheManager = InternalEhCacheManagerFactoryBean.getInstance();
        cacheManager.shutdown();
        // Close the channel
        try
        {
            if (ehcacheHeartbeatChannel != null && ehcacheHeartbeatChannel.isOpen())
            {
                ehcacheHeartbeatChannel.close();
            }
        }
        catch (Throwable e)
        {
            logger.error("Error during shutdown: ", e);
        }
    }
    
    /**
     * Adds the necessary peer mechanisms to the caches and cache manager.
     * 
     * @param cacheManager          the cache manager to add the factories to
     */
    private void setupCaches(CacheManager cacheManager)
    {
        if (cacheManager.getCachePeerProvider() != null)
        {
            throw new RuntimeException("Double check for cache manager peer provider failed");
        }
        JGroupsRMICacheManagerPeerProvider peerProvider = new JGroupsRMICacheManagerPeerProvider(
                cacheManager,
                ehcacheHeartbeatChannel,
                JGroupsRMICacheManagerPeerProvider.DEFAULT_HEARTBEAT_INTERVAL);
    }
}
