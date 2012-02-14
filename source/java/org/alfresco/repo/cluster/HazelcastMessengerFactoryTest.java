/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

package org.alfresco.repo.cluster;

import static org.junit.Assert.*;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.when;

import java.net.InetSocketAddress;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.hazelcast.config.Config;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.core.Cluster;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.Member;


/**
 * Tests for the HazelcastMessengerFactory class.
 * 
 * @author Matt Ward
 */
@RunWith(MockitoJUnitRunner.class)
public class HazelcastMessengerFactoryTest
{
    private HazelcastMessengerFactory factory;
    private GroupConfig groupConfig;
    private @Mock HazelcastInstance hazelcast;
    private @Mock Member member;
    private @Mock Cluster cluster;
    private @Mock ITopic<String> topic;
    private @Mock Config config;
    
    @Before
    public void setUp()
    {
        factory = new HazelcastMessengerFactory();
        factory.setHazelcast(hazelcast);
        groupConfig = new GroupConfig();
    }
    
    @Test
    public void topicWrappedInMessenger()
    {
        when(hazelcast.<String>getTopic("app-region")).thenReturn(topic);
        when(hazelcast.getCluster()).thenReturn(cluster);
        when(cluster.getLocalMember()).thenReturn(member);
        when(member.getInetSocketAddress()).thenReturn(InetSocketAddress.createUnresolved("a-host-name", 1234));
        
        Messenger<String> messenger = factory.createMessenger("app-region");
        
        assertSame(topic, ((HazelcastMessenger<String>) messenger).getTopic());
        assertEquals("a-host-name:1234", messenger.getAddress());
    }
    
    @Test
    public void canCheckClusterIsActive()
    {
        when(hazelcast.getConfig()).thenReturn(config);
        when(config.getGroupConfig()).thenReturn(groupConfig);
        
        groupConfig.setName("my-cluster-name");
        assertEquals(true, factory.isClusterActive());
        
        groupConfig.setName("");
        assertEquals(false, factory.isClusterActive());        
    }
}
