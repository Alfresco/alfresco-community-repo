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


import org.alfresco.util.ApplicationContextHelper;
import org.jgroups.ChannelException;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

import com.hazelcast.config.Config;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.MessageListener;

/**
 * Tests for Hazelcast implementations of {@link Messenger} and related classes.
 * These are integration tests and configured through a Spring test context file.
 * 
 * @author Matt Ward
 */
public class HazelcastTest implements MessageListener<String>
{
    private static ApplicationContext ctx;
    private MessengerTestHelper helper;
    
    @BeforeClass
    public static void setUpClass()
    {
        ctx = ApplicationContextHelper.
                getApplicationContext(new String[] { "cluster-test/hazelcast-messenger-test.xml" });
    }
    
    @AfterClass
    public static void tearDownClass()
    {
        ApplicationContextHelper.closeApplicationContext();
    }
    
    @Before
    public void setUp()
    {
        helper = new MessengerTestHelper();
    }
 
    
    @Test
    public void canSendWithHazelcastMessengerFactory() throws InterruptedException, ChannelException
    {
        Config config = new Config();
        GroupConfig groupConfig = new GroupConfig();
        groupConfig.setName("testcluster");
        groupConfig.setPassword("secret");
        config.setGroupConfig(groupConfig);
        HazelcastInstance hi = Hazelcast.newHazelcastInstance(config);
        ITopic<String> topic = hi.getTopic("testregion");
        
        topic.addMessageListener(this);
        
        MessengerFactory messengerFactory = (MessengerFactory) ctx.getBean("messengerFactory");
        Messenger<String> messenger = messengerFactory.createMessenger("testregion");
        messenger.send("Full test including spring.");
        
        helper.checkMessageReceivedWas("Full test including spring.");
    }


    @Override
    public void onMessage(String message)
    {
        helper.setReceivedMsg(message);
    }
}
