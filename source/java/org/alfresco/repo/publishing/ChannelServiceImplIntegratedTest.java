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

import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.publishing.channels.Channel;
import org.alfresco.service.cmr.publishing.channels.ChannelType;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Brian
 * @author Nick Smith
 * 
 */
public class ChannelServiceImplIntegratedTest extends AbstractPublishingIntegrationTest
{
    private static final String channelName = GUID.generate();

    @Resource(name="channelService")
    private ChannelServiceImpl channelService;
    
    @Test
    public void testCreateChannel() throws Exception
    {
//        personManager.setUser(username);
//        try
//        {
//            createChannel();
//            fail("Only Admin user can create channels!");
//        }
//        catch(AccessDeniedException e)
//        {
//            // NOOP
//        }
        
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        Channel channel = createChannel();
        assertEquals(channelTypeId, channel.getChannelType().getId());
        assertNotNull(channelName, channel.getName());
        NodeRef channelNode = new NodeRef(channel.getId());
        assertTrue(nodeService.exists(channelNode));
    }

    @Test
    public void testDeleteChannel() throws Exception
    {
        Channel channel = createChannel();
        assertNotNull("The channel should exist! Id: "+channel.getId(), channelService.getChannelById(channel.getId()));
        assertNotNull("The channel should exist! Name: "+channelName, channelService.getChannelByName(channelName));

        personManager.setUser(username);
        try
        {
            channelService.deleteChannel(channel);
            fail("Only Admin users should be able to delete channels.");
        }
        catch(AccessDeniedException e)
        {
            //NOOP
        }

        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        channelService.deleteChannel(channel);
        
        assertNull("The channel should have been deleed! Id: "+channel.getId(), channelService.getChannelById(channel.getId()));
        assertNull("The channel should have been deleed! Name: "+channelName, channelService.getChannelByName(channelName));
    }

    @Test
    public void testRenameChannel() throws Exception
    {
        String newChannelName = "New Channel Name";
        Channel channel = createChannel();
        
        personManager.setUser(username);
        try
        {
            channelService.renameChannel(channel, newChannelName);
            fail("Only Admin user can rename Channel.");
        }
        catch(AccessDeniedException e)
        {
            //NOOP
        }
        
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        channelService.renameChannel(channel, newChannelName);
        Channel renamedChannel = channelService.getChannelById(channel.getId());
        assertNotNull(renamedChannel);
        assertEquals(newChannelName, renamedChannel.getName());
        assertNotNull(channelService.getChannelByName(newChannelName));
        assertNotNull(channelService.getChannelById(channel.getId()));
        assertNull(channelService.getChannelByName(channelName));
    }

    @Test
    public void testUpdateChannel() throws Exception
    {
        String newTitle = "This is my title";
        Channel channel = createChannel();

        Map<QName,Serializable> props = channel.getProperties();
        assertNull(props.get(ContentModel.PROP_TITLE));
        
        props.put(ContentModel.PROP_TITLE, newTitle);
        
        
        personManager.setUser(username);
        try
        {
            channelService.updateChannel(channel, props);
            fail("Only Admin user can rename Channel.");
        }
        catch(AccessDeniedException e)
        {
            //NOOP
        }
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
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
        Set<String> ids = new HashSet<String>();
        for (int i = 0; i < channelCount; ++i)
        {
            Channel newChannel = testHelper.createChannel(channelTypeId);
            ids.add(newChannel.getId());
            
            List<Channel> channels = channelService.getChannels();
            assertEquals(i + 1 + startingSize, channels.size());
            Set<String> idsToCheck = new HashSet<String>(ids);
            for (Channel channel : channels)
            {
                idsToCheck.remove(channel.getId());
            }
            assertTrue(idsToCheck.isEmpty());
        }
    }
    
    @Test
    public void testGetChannelsPermissions() throws Exception
    {
        ChannelType publishType = testHelper.mockChannelType(GUID.generate());
        when(publishType.canPublish()).thenReturn(true);
        ChannelType statusType = testHelper.mockChannelType(GUID.generate());
        when(statusType.canPublishStatusUpdates()).thenReturn(true);
        
        Channel publishChannel = testHelper.createChannel(publishType.getId());
        Channel statusChannel = testHelper.createChannel(statusType.getId());
        
        // Create User1 and set as FullyAuthenticatedUser.
        String user1 = GUID.generate();
        personManager.createPerson(user1);
        personManager.setUser(user1);
        
        // User1 should have access to Channel with no permissions filtering.
        Channel channelById = channelService.getChannelById(publishChannel.getId());
        assertNotNull("User1 should have access to the channel!", channelById);
        List<Channel> channels = channelService.getChannels();
        assertTrue("Result of getChannels() should contain the channel!", checkContainsChannel(channels, publishChannel.getId(), statusChannel.getId()));
        channels = channelService.getPublishingChannels(false);
        assertTrue("User1 should have access to unfiltered publishing channels", checkContainsChannel(channels, publishChannel.getId()));
        channels = channelService.getStatusUpdateChannels(false);
        assertTrue("User1 should have access to unfiltered status update channels", checkContainsChannel(channels, statusChannel.getId()));
        
        // User1 should not have access if permissions are filtered.
        channels = channelService.getPublishingChannels(true);
        assertFalse("User1 should not have access to filtered publishing channels", checkContainsChannel(channels, publishChannel.getId()));
        channels = channelService.getStatusUpdateChannels(true);
        assertFalse("User1 should not have access to filtered status update channels", checkContainsChannel(channels, statusChannel.getId()));
        
        //Add Read permissions to User1.
        testHelper.setChannelPermission(user1, publishChannel.getId(), PermissionService.READ);
        testHelper.setChannelPermission(user1, statusChannel.getId(), PermissionService.READ);
        
        // Read permissions should not allow access to filtered channels.
        channels = channelService.getPublishingChannels(true);
        assertFalse("User1 should not have access to filtered publishing channels", checkContainsChannel(channels, publishChannel.getId()));
        channels = channelService.getStatusUpdateChannels(true);
        assertFalse("User1 should not have access to filtered status update channels", checkContainsChannel(channels, statusChannel.getId()));
        
        //Add ADD_CHILD permissions to User1.
        testHelper.setChannelPermission(user1, publishChannel.getId(), PermissionService.ADD_CHILDREN);
        testHelper.setChannelPermission(user1, statusChannel.getId(), PermissionService.ADD_CHILDREN);
        
        // Add Child permissions should allow access to filtered channels.
        channels = channelService.getPublishingChannels(true);
        assertTrue("User1 should have access to filtered publishing channels", checkContainsChannel(channels, publishChannel.getId()));
        channels = channelService.getStatusUpdateChannels(true);
        assertTrue("User1 should have access to filtered status update channels", checkContainsChannel(channels, statusChannel.getId()));
    }
    
    @Test
    public void testGetChannelByName() throws Exception
    {
        Channel channel = channelService.getChannelById(null);
        assertNull("Should return null if unknown channelName", channel);
        
        channel = channelService.getChannelByName(channelName);
        assertNull("Should return null if null channelName", channel);
        
        Channel createdChannel = createChannel();
        
        channel = channelService.getChannelByName(channelName);
        assertNotNull("Should return created channel!", channel);
        assertEquals(channelName, channel.getName());
        assertEquals(createdChannel.getChannelType().getId(), channel.getChannelType().getId());
        assertEquals(createdChannel.getId(), channel.getId());
    }
    
    @Test
    public void testGetChannelById() throws Exception
    {
        Channel channel = channelService.getChannelById(null);
        assertNull("Should return null if null channelId", channel);

        channel = channelService.getChannelById("test://channel/id");
        assertNull("Should return null if unknown channelId", channel);
        
        Channel createdChannel = createChannel();
        
        channel = channelService.getChannelById(createdChannel.getId());
        assertNotNull("Should return created channel!", channel);
        assertEquals(createdChannel.getId(), channel.getId());
        assertEquals(channelName, channel.getName());
        assertEquals(createdChannel.getChannelType().getId(), channel.getChannelType().getId());
    }
    
    private boolean checkContainsChannel(List<Channel> channels, final String... ids)
    {
        ArrayList<String> remainingIds = new ArrayList<String>(Arrays.asList(ids));
        for (Channel channel : channels)
        {
            remainingIds.remove(channel.getId());
            if(remainingIds.isEmpty())
            {
                return true;
            }
        }
        return false;
    }
    
    private Channel createChannel()
    {
        return testHelper.createChannel(channelTypeId, channelName);
    }
    
    @Before
    @Override
    public void onSetUp() throws Exception
    {
        super.onSetUp();
        this.channelService = (ChannelServiceImpl) getApplicationContext().getBean("channelService");
        testHelper.mockChannelType(channelTypeId);
    }
}
