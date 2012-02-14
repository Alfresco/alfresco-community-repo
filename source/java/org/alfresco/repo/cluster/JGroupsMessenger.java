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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jgroups.Channel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;

/**
 * JGroups implementation of the {@link Messenger} class.
 * 
 * @author Matt Ward
 */
public class JGroupsMessenger<T extends Serializable> extends ReceiverAdapter implements Messenger<T>
{
    private final Channel channel;
    private MessageReceiver<T> receiverDelegate;
    private final static Log logger = LogFactory.getLog(JGroupsMessenger.class);
    
    /**
     * Construct a messenger that wraps a JGroups Channel.
     * 
     * @param channel
     */
    public JGroupsMessenger(Channel channel)
    {
        this.channel = channel;
    }

    
    @Override
    public void send(T message)
    {
        try
        {
            // Serializing the message ourselves and passing a byte[]
            // to Channel.send() as recommended by JGroups.
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(bytes);
            out.writeObject(message);
            out.close();
            bytes.close();
            if (logger.isTraceEnabled())
            {
                logger.trace("Sending " + message);
            }
            channel.send(null, null, bytes.toByteArray());
        }
        catch (Throwable e)
        {
            throw new MessageSendingException(e);
        }
    }

    /*
     * @see org.alfresco.repo.cluster.Messenger#setReceiver(org.alfresco.repo.cluster.MessageReceiver)
     */
    @Override
    public void setReceiver(MessageReceiver<T> receiver)
    {
        // Make sure the delegate is installed, before starting to receive messages.
        receiverDelegate = receiver;
        // Start receiving messages and dispatch them to the delegate.
        channel.setReceiver(this);
    }


    /*
     * @see org.jgroups.ReceiverAdapter#receive(org.jgroups.Message)
     */
    @Override
    public void receive(Message msg)
    {
        // Deserializing the message ourselves rather than using
        // the Message's getObject() method (as recommended by JGroups).
        byte[] msgBytes = msg.getBuffer();
        ByteArrayInputStream bytes = new ByteArrayInputStream(msgBytes);
        ObjectInput in;
        try
        {
            in = new ObjectInputStream(bytes);
            @SuppressWarnings("unchecked")
            T payload = (T) in.readObject();
            in.close();
            bytes.close();
            if (logger.isTraceEnabled())
            {
                logger.trace("Received (will be delegated to receiver): " + payload);
            }
            // Pass the deserialized payload on to the receiver delegate
            receiverDelegate.onReceive(payload);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Couldn't receive object.", e);
        }
        catch (ClassNotFoundException e)
        {
            throw new RuntimeException("Couldn't receive object.", e);
        }
    }


    @Override
    public boolean isConnected()
    {
        return channel.isConnected();
    }


    @Override
    public String getAddress()
    {
        return channel.getAddress().toString();
    }
}
