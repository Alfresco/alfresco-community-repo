/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.jgroups;

import org.jgroups.Channel;
import org.jgroups.Message;

import junit.framework.TestCase;

/**
 * @see AlfrescoJGroupsChannelFactory
 * 
 * @author Derek Hulley
 * @since 2.1.3
 */
public class AlfrescoJGroupsChannelFactoryTest extends TestCase 
{
    private static byte[] bytes = new byte[65536];
    static
    {
        for (int i = 0; i < bytes.length; i++)
        {
            bytes[i] = 1;
        }
    }

    private String appRegion;
    
    @Override
    protected void setUp() throws Exception
    {
        appRegion = getName();
    }
    
    /**
     * Check that the channel is behaving
     */
    private void stressChannel(Channel channel) throws Exception
    {
        System.out.println("Test: " + getName());
        System.out.println("    Channel: " + channel);
        System.out.println("    Cluster: " + channel.getClusterName());
        channel.send(null, null, Boolean.TRUE);
        channel.send(new Message(null, null, bytes));
    }
    
    public void testNoCluster() throws Exception
    {
        Channel channel = AlfrescoJGroupsChannelFactory.getChannel(appRegion);
        stressChannel(channel);
    }
    
    public void testBasicCluster() throws Exception
    {
        AlfrescoJGroupsChannelFactory.changeClusterNamePrefix("blah");
        AlfrescoJGroupsChannelFactory.rebuildChannels();
        Channel channel = AlfrescoJGroupsChannelFactory.getChannel(appRegion);
        stressChannel(channel);
    }
    
    public void testHotSwapCluster() throws Exception
    {
        AlfrescoJGroupsChannelFactory.changeClusterNamePrefix("ONE");
        AlfrescoJGroupsChannelFactory.rebuildChannels();
        Channel channel1 = AlfrescoJGroupsChannelFactory.getChannel(appRegion);
        stressChannel(channel1);
        AlfrescoJGroupsChannelFactory.changeClusterNamePrefix("TWO");
        AlfrescoJGroupsChannelFactory.rebuildChannels();
        Channel channel2 = AlfrescoJGroupsChannelFactory.getChannel(appRegion);
        stressChannel(channel1);
        assertTrue("Channel reference must be the same", channel1 == channel2);
    }
}
