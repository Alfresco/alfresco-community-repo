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
package org.alfresco.repo.jgroups;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jgroups.JChannel;

/**
 * A cache peer provider that does heartbeat sending and receiving using JGroups.
 * 
 * @author Derek Hulley
 * @since 2.1.3
 */
public class AlfrescoJChannelFactory 
{
    private static final Map<String, JChannel> channels;
    
    static
    {
        channels = new ConcurrentHashMap<String, JChannel>(5);
    }

    /**
     * 
     * @param clusterName           the name of the cluster.  The effectively groups the
     *                              machines that will be interacting with each other.
     * @param applicationRegion     the application region.
     * @return
     */
    public static JChannel getChannel(String clusterName, String applicationRegion)
    {
        /*
         * Synchronization is handled by the Map
         */
        StringBuilder sb = new StringBuilder(100).append(clusterName).append("-").append(applicationRegion);
        String fullClusterName = sb.toString();
        // Get the channel
        JChannel channel = channels.get(fullClusterName);
    }
    
    private static synchronized void newChannel(String clusterName, String clusterRegion)
    {
        
    }
    
    /**
     * TODO: Make into a Spring factory bean so that it can be used as a direct bean reference
     */
    private AlfrescoJChannelFactory()
    {
    }

}
