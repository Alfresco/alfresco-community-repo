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
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.person.TestPersonManager;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.publishing.channels.Channel;
import org.alfresco.service.cmr.publishing.channels.ChannelType;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.alfresco.util.collections.CollectionUtils;
import org.alfresco.util.collections.Filter;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Brian
 * 
 */
public class ChannelServiceImplIntegratedTest extends AbstractPublishingIntegrationTest
{
    private static final String channelName = GUID.generate();
    private static final String channelTypeName = "MockedChannelType";
    private static boolean channelTypeRegistered = false;

    @Resource(name="channelService")
    private ChannelServiceImpl channelService;
    private PermissionService permissionService;
    private TestPersonManager personManager;
    
    private ChannelType mockedChannelType = mock(ChannelType.class);

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
    public void testGetChannelsPermissions() throws Exception
    {
        // Create Channel as Admin user.
        Channel channel = createChannel();
        NodeRef channelNode = new NodeRef(channel.getId());
        
        // Create User1 and set as FullyAuthenticatedUser.
        String user1 = GUID.generate();
        personManager.createPerson(user1);
        personManager.setUser(user1);
        
        // User1 should not have access to Channel.
        Channel channelById = channelService.getChannelById(channel.getId());
        assertNull("User1 should not have access to the channel!", channelById);
        List<Channel> channels = channelService.getChannels();
        assertFalse("Result of getChannels() should not contain the channel!", checkContainsChannel(channel.getId(), channels));
        
        // Set authentication to Admin
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        //Add Read permissions to User1.
        permissionService.setPermission(channelNode, user1, PermissionService.READ, true);
        // Set authentication to User1
        personManager.setUser(user1);
        
        // Read permissions should not allow access to the Channel.
        channelById = channelService.getChannelById(channel.getId());
        assertNull("User1 should not have access to the channel!", channelById);
        channels = channelService.getChannels();
        assertFalse("Result of getChannels() should not contain the channel!", checkContainsChannel(channel.getId(), channels));
        
        // Set authentication to Admin
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        //Add ADD_CHILD permissions to User1.
        permissionService.setPermission(channelNode, user1, PermissionService.ADD_CHILDREN, true);
        // Set authentication to User1
        personManager.setUser(user1);
        
        // Add Child permissions should allow access to the Channel.
        channelById = channelService.getChannelById(channel.getId());
        assertNotNull("User1 should have access to the channel!", channelById);
        channels = channelService.getChannels();
        assertTrue("Result of getChannels() should contain the channel!", checkContainsChannel(channel.getId(), channels));
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
    
    private boolean checkContainsChannel(final String id, List<Channel> channels)
    {
        Filter<Channel> acceptor = new Filter<Channel>()
        {
            public Boolean apply(Channel value)
            {
                return id.equals(value.getId());
            }
        };
        Channel result = CollectionUtils.findFirst(channels, acceptor);
        return result != null;
    }

    private Channel createChannel()
    {
        return channelService.createChannel(channelTypeName, channelName, null);
    }
    
    
    @Before
    @Override
    public void onSetUp() throws Exception
    {
        super.onSetUp();
        this.channelService = (ChannelServiceImpl) getApplicationContext().getBean("channelService");
        this.permissionService = (PermissionService) getApplicationContext().getBean(ServiceRegistry.PERMISSIONS_SERVICE.getLocalName());
        MutableAuthenticationService authenticationService= (MutableAuthenticationService) getApplicationContext().getBean(ServiceRegistry.AUTHENTICATION_SERVICE.getLocalName());
        PersonService personService= (PersonService) getApplicationContext().getBean(ServiceRegistry.PERSON_SERVICE.getLocalName());
        
        this.personManager = new TestPersonManager(authenticationService, personService, nodeService);
        
        when(mockedChannelType.getId()).thenReturn(channelTypeName);
        when(mockedChannelType.getChannelNodeType()).thenReturn(PublishingModel.TYPE_DELIVERY_CHANNEL);

        if (!channelTypeRegistered)
        {
            channelService.register(mockedChannelType);
            channelTypeRegistered = true;
        }

    }

    /**
    * {@inheritDoc}
    */
    @Override
    public void onTearDown() throws Exception
    {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        try
        {
            Channel channel = channelService.getChannelByName(channelName);
            if (channel != null)
            {
                channelService.deleteChannel(channel);
            }
        }
        finally
        {
            super.onTearDown();
        }
    }
}
