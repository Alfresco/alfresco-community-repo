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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.hazelcast.core.ITopic;


/**
 * Tests for the HazelcastMessengerFactory class.
 * 
 * @author Matt Ward
 */
@RunWith(MockitoJUnitRunner.class)
public class HazelcastMessengerFactoryTest
{
    private HazelcastMessengerFactory factory;
    private @Mock ITopic<Object> topic;
    
    @Before
    public void setUp()
    {
        factory = new HazelcastMessengerFactory();
        factory.setTopic(topic);
    }
    
    @Test
    public void topicWrappedInMessenger()
    {
        when(topic.getName()).thenReturn("app-region");
        HazelcastMessenger messenger = (HazelcastMessenger) factory.createMessenger();
        assertEquals("app-region", messenger.getTopicName());
    }
}
