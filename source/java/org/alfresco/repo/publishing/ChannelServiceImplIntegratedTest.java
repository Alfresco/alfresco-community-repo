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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.publishing.channels.Channel;
import org.alfresco.service.cmr.publishing.channels.ChannelType;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.util.GUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Brian
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:alfresco/application-context.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public class ChannelServiceImplIntegratedTest
{
    private static boolean channelTypeRegistered = false;

    @Autowired
    protected ApplicationContext applicationContext;
    protected ServiceRegistry serviceRegistry;
    protected RetryingTransactionHelper retryingTransactionHelper;
    protected NodeService nodeService;
    protected WorkflowService workflowService;
    protected FileFolderService fileFolderService;
    protected SiteService siteService;
    private ChannelServiceImpl channelService;

    protected AuthenticationComponent authenticationComponent;
    private String siteId;
    private ChannelType mockedChannelType = mock(ChannelType.class);
    private String channelTypeName;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception
    {
        serviceRegistry = (ServiceRegistry) applicationContext.getBean(ServiceRegistry.SERVICE_REGISTRY);
        serviceRegistry.getAuthenticationService().authenticate("admin", "admin".toCharArray());

        retryingTransactionHelper = serviceRegistry.getRetryingTransactionHelper();
        fileFolderService = serviceRegistry.getFileFolderService();
        workflowService = serviceRegistry.getWorkflowService();
        nodeService = serviceRegistry.getNodeService();
        siteService = serviceRegistry.getSiteService();

        channelService = (ChannelServiceImpl) applicationContext.getBean("channelService");
        siteId = GUID.generate();
        siteService.createSite("test", siteId, "Test site created by ChannelServiceImplIntegratedTest",
                "Test site created by ChannelServiceImplIntegratedTest", SiteVisibility.PUBLIC);

        channelTypeName = "MockedChannelType";
        when(mockedChannelType.getId()).thenReturn(channelTypeName);
        when(mockedChannelType.getChannelNodeType()).thenReturn(PublishingModel.TYPE_DELIVERY_CHANNEL);
        when(mockedChannelType.getContentRootNodeType()).thenReturn(ContentModel.TYPE_FOLDER);

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

        String channelName = "Test Channel - Name";
        Channel channel = channelService.createChannel(siteId, channelTypeName, channelName, null);
        assertEquals(channelTypeName, channel.getChannelType().getId());
        assertEquals(channelName, channel.getName());
        assertTrue(nodeService.exists(channel.getNodeRef()));
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
            String channelName = GUID.generate();
            channelNames.add(channelName);
            channelService.createChannel(siteId, channelTypeName, channelName, null);

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
}
