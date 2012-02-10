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

import org.springframework.extensions.surf.util.ParameterCheck;

import com.hazelcast.core.ITopic;
import com.hazelcast.core.MessageListener;

/**
 * Hazelcast-based implementation of the {@link Messenger} interface.
 * 
 * @see HazelcastMessengerFactory
 * @author Matt Ward
 */
public class HazelcastMessenger<T extends Serializable> implements Messenger<T>, MessageListener<T>
{
    private ITopic<T> topic;
    private MessageReceiver<T> receiverDelegate;
    
    
    /**
     * @param topic
     */
    public HazelcastMessenger(ITopic<T> topic)
    {
        this.topic = topic;
    }


    @Override
    public void send(T message)
    {
        topic.publish(message);
    }

    @Override
    public void setReceiver(MessageReceiver<T> receiver)
    {
        // Install a delegate to ready to handle incoming messages.
        receiverDelegate = receiver;
        // Start receiving messages.
        topic.addMessageListener(this);
    }

    @Override
    public void onMessage(T message)
    {
        ParameterCheck.mandatory("message", message);
        receiverDelegate.onReceive(message);
    }

    protected String getTopicName()
    {
        return topic.getName();
    }


    @Override
    public boolean isConnected()
    {
        return true;
    }
}
