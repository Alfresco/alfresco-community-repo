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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.publishing.channels.Channel;
import org.alfresco.service.cmr.publishing.channels.ChannelType;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Brian
 * 
 */
public class ChannelServiceImplIntegratedTest extends AbstractPublishingIntegrationTest
{
    private static final String channelName = "Test Channel - Name";
    private static final String channelTypeName = "MockedChannelType";
    private static boolean channelTypeRegistered = false;

    @Resource(name="channelService")
    private ChannelServiceImpl channelService;

    private ChannelType mockedChannelType = mock(ChannelType.class);

    @Before
    @Override
    public void onSetUp() throws Exception
    {
        super.onSetUp();
        channelService = (ChannelServiceImpl) getApplicationContext().getBean("channelService");
        when(mockedChannelType.getId()).thenReturn(channelTypeName);
        when(mockedChannelType.getChannelNodeType()).thenReturn(PublishingModel.TYPE_DELIVERY_CHANNEL);

        if (!channelTypeRegistered)
        {
            channelService.register(mockedChannelType);
            channelTypeRegistered = true;
        }

    }

    @Test
    public void testCreateChannel() throws Exception
    {
        Channel channel = createChannel();
        assertEquals(channelTypeName, channel.getChannelType().getId());
        assertEquals(channelName, channel.getName());
        assertTrue(nodeService.exists(channel.getNodeRef()));
    }

    @Test
    public void testDeleteChannel() throws Exception
    {
        Channel channel = createChannel();
        channelService.deleteChannel(channel);
        
        assertNull("The channel should have been deleed! Id: "+channel.getId(), channelService.getChannelById(channel.getId()));
        assertNull("The channel should have been deleed! Name: "+channelName, channelService.getChannelByName(channelName));
    }

    @Test
    public void testRenameChannel() throws Exception
    {
        String newChannelName = "New Channel Name";
        Channel channel = createChannel();
        channelService.renameChannel(channel, newChannelName);
        
        Channel renamedChannel = channelService.getChannelById(channel.getId());
        assertNotNull(renamedChannel);
        assertEquals(newChannelName, renamedChannel.getName());
    }

    @Test
    public void testUpdateChannel() throws Exception
    {
        String newTitle = "This is my title";
        Channel channel = createChannel();

        Map<QName,Serializable> props = channel.getProperties();
        assertNull(props.get(ContentModel.PROP_TITLE));
        
        props.put(ContentModel.PROP_TITLE, newTitle);
        channelService.updateChannel(channel, props);
        
        Channel updatedChannel = channelService.getChannelById(channel.getId());
        Serializable title = updatedChannel.getProperties().get(ContentModel.PROP_TITLE); 
        assertNotNull(title);
        assertEquals(newTitle, title);
    }

    @Test
    public void testGetChannels() throws Exception
    {
        int startingSize = channelService.getChannels().size();
        
        int channelCount = 7;
        Set<String> channelNames = new HashSet<String>();
        for (int i = 0; i < channelCount; ++i)
        {
            String name = GUID.generate();
            channelNames.add(name);
            channelService.createChannel(channelTypeName, name, null);

            List<Channel> channels = channelService.getChannels();
            assertEquals(i + 1 + startingSize, channels.size());
            Set<String> names = new HashSet<String>(channelNames);
            for (Channel channel : channels)
            {
                names.remove(channel.getName());
            }
            assertTrue(names.isEmpty());
        }
    }
    
    @Test
    public void testGetChannel() throws Exception
    {
        try
        {
            channelService.getChannelByName(null);
            fail("Should throw an Exception if channelName is null!");
        }
        catch (IllegalArgumentException e) {
            // NOOP
        }
        Channel channel = channelService.getChannelByName(channelName);
        assertNull("Should return null if unknown channelName", channel);
        
        Channel createdChannel = createChannel();
        
        channel = channelService.getChannelByName(channelName);
        assertNotNull("Should return created channel!", channel);
        assertEquals(channelName, channel.getName());
        assertEquals(createdChannel.getChannelType().getId(), channel.getChannelType().getId());
        assertEquals(createdChannel.getNodeRef(), channel.getNodeRef());
    }
    
    /**
     * @return
     */
    private Channel createChannel()
    {
        return channelService.createChannel(channelTypeName, channelName, null);
    }
}
