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
import org.alfresco.repo.site.SiteServiceException;
import org.alfresco.service.cmr.publishing.channels.Channel;
import org.alfresco.service.cmr.publishing.channels.ChannelType;
import org.alfresco.service.cmr.repository.NodeRef;
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

    @Resource(name="environmentHelper")
    private EnvironmentHelper environmentHelper;

    private ChannelType mockedChannelType = mock(ChannelType.class);

    @Before
    @Override
    public void onSetUp() throws Exception
    {
        super.onSetUp();
        channelService = (ChannelServiceImpl) getApplicationContext().getBean("channelService");
        environmentHelper = (EnvironmentHelper) getApplicationContext().getBean("environmentHelper");
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
        List<Channel> channels = channelService.getChannels(siteId);
        assertTrue(channels.isEmpty());

        Channel channel = channelService.createChannel(siteId, channelTypeName, channelName, null);
        assertEquals(channelTypeName, channel.getChannelType().getId());
        assertEquals(channelName, channel.getName());
        assertTrue(nodeService.exists(channel.getNodeRef()));
        
        NodeRef environmentNode = environmentHelper.getEnvironment(siteId);
        assertNotNull(environmentNode);
        assertNotNull(nodeService.getChildByName(environmentNode, ContentModel.ASSOC_CONTAINS, channelName));
    }

    @Test
    public void testDeleteChannel() throws Exception
    {
        testCreateChannel();
        
        channelService.deleteChannel(siteId, channelName);
        
        List<Channel> channels = channelService.getChannels(siteId);
        assertTrue(channels.isEmpty());

        NodeRef environmentNode = environmentHelper.getEnvironment(siteId);
        assertNotNull(environmentNode);
        assertNull(nodeService.getChildByName(environmentNode, ContentModel.ASSOC_CONTAINS, channelName));
    }

    @Test
    public void testRenameChannel() throws Exception
    {
        String newChannelName = "New Channel Name";
        testCreateChannel();
        List<Channel> channels = channelService.getChannels(siteId);
        assertEquals(1, channels.size());
        channelService.renameChannel(siteId, channelName, newChannelName);
        
        channels = channelService.getChannels(siteId);
        assertEquals(1, channels.size());
        Channel channel = channels.get(0);
        assertEquals(newChannelName, channel.getName());
        NodeRef environmentNode = environmentHelper.getEnvironment(siteId);
        assertNotNull(environmentNode);
        assertNull(nodeService.getChildByName(environmentNode, ContentModel.ASSOC_CONTAINS, channelName));
        assertNotNull(nodeService.getChildByName(environmentNode, ContentModel.ASSOC_CONTAINS, newChannelName));
    }

    @Test
    public void testUpdateChannel() throws Exception
    {
        String newTitle = "This is my title";
        testCreateChannel();
        List<Channel> channels = channelService.getChannels(siteId);
        assertEquals(1, channels.size());
        
        Channel channel = channels.get(0);
        Map<QName,Serializable> props = channel.getProperties();
        assertNull(props.get(ContentModel.PROP_TITLE));
        
        props.put(ContentModel.PROP_TITLE, newTitle);
        channelService.updateChannel(siteId, channelName, props);
        
        channels = channelService.getChannels(siteId);
        assertEquals(1, channels.size());
        channel = channels.get(0);
        Serializable title = channel.getProperties().get(ContentModel.PROP_TITLE); 
        assertNotNull(title);
        assertEquals(newTitle, title);
    }

    @Test
    public void testGetChannels() throws Exception
    {
        List<Channel> channels = channelService.getChannels(siteId);
        assertTrue(channels.isEmpty());

        int channelCount = 7;
        Set<String> channelNames = new HashSet<String>();
        for (int i = 0; i < channelCount; ++i)
        {
            String name = GUID.generate();
            channelNames.add(name);
            channelService.createChannel(siteId, channelTypeName, name, null);

            channels = channelService.getChannels(siteId);
            assertEquals(i + 1, channels.size());
            Set<String> names = new HashSet<String>(channelNames);
            for (Channel channel : channels)
            {
                assertTrue(names.remove(channel.getName()));
            }
            assertTrue(names.isEmpty());
        }
    }
    
    @Test
    public void testGetChannel() throws Exception
    {
        try
        {
            channelService.getChannel(null, channelName);
            fail("Should throw an Exception if siteId is null!");
        }
        catch (IllegalArgumentException e) {
            // NOOP
        }
        try
        {
            channelService.getChannel(siteId, null);
            fail("Should throw an Exception if channelName is null!");
        }
        catch (IllegalArgumentException e) {
            // NOOP
        }
        Channel channel = channelService.getChannel(siteId, channelName);
        assertNull("Should return null if unknown channelName", channel);
        
        // Create channel
        Channel createdChannel = channelService.createChannel(siteId, channelTypeName, channelName, null);
        
        try
        {
            channel = channelService.getChannel("No Site", channelName);
            fail("Should throw exception if site does not exist!");
        }
        catch (SiteServiceException e)
        {
            // NOOP
        }
        
        channel = channelService.getChannel(siteId, channelName);
        assertNotNull("Should return created channel!", channel);
        assertEquals(channelName, channel.getName());
        assertEquals(createdChannel.getChannelType().getId(), channel.getChannelType().getId());
        assertEquals(createdChannel.getNodeRef(), channel.getNodeRef());
    }
}
