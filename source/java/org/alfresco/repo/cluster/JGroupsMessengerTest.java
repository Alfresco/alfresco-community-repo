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
import static org.mockito.Mockito.verify;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import org.jgroups.Address;
import org.jgroups.Channel;
import org.jgroups.ChannelClosedException;
import org.jgroups.ChannelNotConnectedException;
import org.jgroups.Message;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Tests for the JGroupsMessenger class.
 * 
 * @author Matt Ward
 */
public class JGroupsMessengerTest
{
    private Channel channel;
    private JGroupsMessenger<String> messenger;
    protected String receivedMsg;

    @Before
    public void setUp()
    {
        channel = Mockito.mock(Channel.class);
        messenger = new JGroupsMessenger<String>(channel);
        receivedMsg = null;
    }
    
    @Test
    public void canSendMessage() throws ChannelNotConnectedException, ChannelClosedException, IOException
    {
        String testText = "This is a test message";
        byte[] testTextSer = serialize(testText);
        // When a message is sent...
        messenger.send(testText);
        
        // the underlying channel should have been used to send it,
        // but will be called with a serialized version of the text.
        verify(channel).send(null, null, testTextSer);
    }
    
    
    @Test
    public void canReceiveMessage() throws IOException
    {
        MessageReceiver<String> receiver = new MessageReceiver<String>()
        {
            @Override
            public void onReceive(String message)
            {
                receivedMsg = message;
            }   
        };
        
        messenger.setReceiver(receiver);
        Message jgroupsMessage = new Message(null, null, serialize("JGroups message payload"));
        // JGroups will call the receive method
        messenger.receive(jgroupsMessage);
        
        // The Messenger should have installed itself as the message
        // receiver for the underlying channel.
        verify(channel).setReceiver(messenger);
        
        assertEquals("JGroups message payload", receivedMsg.toString());
    }
    
    @Test
    public void canDelegateIsConnected()
    {
        Mockito.when(channel.isConnected()).thenReturn(true);
        assertEquals(true, messenger.isConnected());

        Mockito.when(channel.isConnected()).thenReturn(false);
        assertEquals(false, messenger.isConnected());
    }

    @Test
    public void canDelegateGetAddress()
    {
        Address address = Mockito.mock(Address.class);
        Mockito.when(address.toString()).thenReturn("an-address");
        Mockito.when(channel.getAddress()).thenReturn(address);
        assertEquals("an-address", messenger.getAddress());
    }
    
    private byte[] serialize(String text) throws IOException
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ObjectOutput out = new ObjectOutputStream(bytes);
        out.writeObject(text);
        out.close();
        bytes.close();
        return bytes.toByteArray();
    }
}

