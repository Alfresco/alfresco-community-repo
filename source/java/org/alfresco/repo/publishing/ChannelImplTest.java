/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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

package org.alfresco.repo.publishing;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import junit.framework.TestCase;

import org.alfresco.service.cmr.publishing.channels.ChannelType;
import org.alfresco.service.cmr.repository.NodeRef;
/**
 * @author Nick Smith
 * @since 4.0
 *
 */
public class ChannelImplTest extends TestCase
{
    public void testUpdateStatus() throws Exception
    {
        int maxLength = 30;
        ChannelType channelType = mockChannelType(maxLength);
        
        ChannelHelper helper = mock(ChannelHelper.class);
        when(helper.getChannelProperties(any(NodeRef.class))).thenReturn(null);
        NodeRef node = new NodeRef("test://channel/node");
        
        ChannelImpl channel = new ChannelImpl(channelType, node, "Name", helper);
        
        String msg = "Here is a message";
        channel.updateStatus(msg, null);
        verify(channelType).updateStatus(channel, msg, null);
    }

    public void testUpdateStatusTruncates() throws Exception
    {
        int maxLength = 30;
        ChannelType channelType = mockChannelType(maxLength);
        
        ChannelHelper helper = mock(ChannelHelper.class);
        when(helper.getChannelProperties(any(NodeRef.class))).thenReturn(null);
        NodeRef node = new NodeRef("test://channel/node");
        
        ChannelImpl channel = new ChannelImpl(channelType, node, "Name", helper);
        
        String msg = "Here is a much longer message to truncate.";
        String expMsg = msg.substring(0, maxLength);
        channel.updateStatus(msg, null);
        verify(channelType).updateStatus(channel, expMsg, null);
    }
    
    public void testUpdateStatusTruncatesWithUrl() throws Exception
    {
        int maxLength = 30;
        ChannelType channelType = mockChannelType(maxLength);
        
        ChannelHelper helper = mock(ChannelHelper.class);
        when(helper.getChannelProperties(any(NodeRef.class))).thenReturn(null);
        NodeRef node = new NodeRef("test://channel/node");
        
        ChannelImpl channel = new ChannelImpl(channelType, node, "Name", helper);
        String nodeUrl ="http://foo/bar";
        int endpoint = maxLength - nodeUrl.length();
        
        String msg = "Here is a much longer message to truncate.";
        String expMsg = msg.substring(0, endpoint) + nodeUrl;
        channel.updateStatus(msg, nodeUrl);
        verify(channelType).updateStatus(channel, expMsg, null);
    }
    
    public void testUpdateStatusNoMaxLength() throws Exception
    {
        ChannelType channelType = mockChannelType(0);
        
        ChannelHelper helper = mock(ChannelHelper.class);
        when(helper.getChannelProperties(any(NodeRef.class))).thenReturn(null);
        NodeRef node = new NodeRef("test://channel/node");
        
        ChannelImpl channel = new ChannelImpl(channelType, node, "Name", helper);
        String nodeUrl ="http://foo/bar";
        
        String msg = "Here is a much longer message to truncate.";
        String expMsg = msg + nodeUrl;
        channel.updateStatus(msg, nodeUrl);
        verify(channelType).updateStatus(channel, expMsg, null);
    }

    private ChannelType mockChannelType(int maxLength)
    {
        ChannelType channelType = mock(ChannelType.class);
        when(channelType.canPublishStatusUpdates()).thenReturn(true);
        when(channelType.getMaximumStatusLength()).thenReturn(maxLength);
        return channelType;
    }
}
