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

import java.io.Serializable;

import org.alfresco.util.PropertyCheck;

import com.hazelcast.core.ITopic;

/**
 * Hazelcast-based implementation of the {@link MessengerFactory} interface.
 * The factory must be configured with an {@link ITopic} - which
 * should be configured with a topic name that corresponds to an application
 * region.
 * 
 * @author Matt Ward
 */
public class HazelcastMessengerFactory implements MessengerFactory
{
    private ITopic<Object> topic;
    
    
    /**
     * @param topic the topic to set
     */
    public void setTopic(ITopic<Object> topic)
    {
        this.topic = topic;
    }

    /*
     * @see org.alfresco.repo.cluster.MessengerFactory#createMessenger()
     */
    @Override
    public <T extends Serializable> Messenger<T> createMessenger()
    {
        PropertyCheck.mandatory(this, "topic", topic);
        return new HazelcastMessenger(topic);
    }
}
