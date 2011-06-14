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

import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.publishing.Environment;
import org.alfresco.service.cmr.publishing.MutablePublishingPackage;
import org.alfresco.service.cmr.publishing.PublishingEvent;
import org.alfresco.service.cmr.publishing.PublishingPackageEntry;
import org.alfresco.service.cmr.publishing.PublishingQueue;
import org.alfresco.service.cmr.publishing.PublishingService;
import org.alfresco.service.cmr.publishing.channels.Channel;
import org.alfresco.service.cmr.publishing.channels.ChannelType;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
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
public class PublishingIntegratedTest
{
    private static String channelName = "Test Channel - Name";

    @Autowired
    protected ApplicationContext applicationContext;
    protected ServiceRegistry serviceRegistry;
    protected RetryingTransactionHelper retryingTransactionHelper;
    protected NodeService nodeService;
    protected WorkflowService workflowService;
    protected FileFolderService fileFolderService;
    protected SiteService siteService;
    private ChannelServiceImpl channelService;
    private PublishServiceImpl publishingService;

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
        publishingService = (PublishServiceImpl) applicationContext.getBean("publishingService");
        siteId = GUID.generate();
        siteService.createSite("test", siteId, "Test site created by ChannelServiceImplIntegratedTest",
                "Test site created by PublishingIntegratedTest", SiteVisibility.PUBLIC);

        channelTypeName = "MockedChannelType";
        when(mockedChannelType.getId()).thenReturn(channelTypeName);
        when(mockedChannelType.getChannelNodeType()).thenReturn(PublishingModel.TYPE_DELIVERY_CHANNEL);
        when(mockedChannelType.getContentRootNodeType()).thenReturn(ContentModel.TYPE_FOLDER);

        if (channelService.getChannelType(channelTypeName)== null)
        {
            channelService.register(mockedChannelType);
        }
    }

    @Test
    public void testScheduleNewEvent() throws Exception
    {
        Channel channel = channelService.createChannel(siteId, channelTypeName, channelName, null);

        Set<NodeRef> nodes = new HashSet<NodeRef>();
        for (int i = 0; i < 4; ++i)
        {
            nodes.add(nodeService.createNode(channel.getNodeRef(), ContentModel.ASSOC_CONTAINS, QName.createQName(
                    NamespaceService.CONTENT_MODEL_1_0_URI, Integer.toString(i)), ContentModel.TYPE_CONTENT).getChildRef());
        }

        Environment liveEnvironment = publishingService.getEnvironment(siteId, PublishingService.LIVE_ENVIRONMENT_NAME);
        PublishingQueue liveQueue = liveEnvironment.getPublishingQueue();
        MutablePublishingPackage publishingPackage = liveQueue.createPublishingPackage();
        publishingPackage.addNodesToPublish(nodes);

        Calendar scheduleTime = Calendar.getInstance();
        scheduleTime.add(Calendar.HOUR, 1);
        String eventId = liveQueue.scheduleNewEvent(publishingPackage, channelName, scheduleTime, null, null);
        
        PublishingEvent event = publishingService.getPublishingEvent(eventId);
        
        Assert.assertEquals(scheduleTime, event.getScheduledTime());
        Assert.assertEquals(eventId, event.getId());
        Collection<PublishingPackageEntry> entries = event.getPackage().getEntries();
        Assert.assertEquals(4, entries.size());
        for (PublishingPackageEntry entry : entries)
        {
            Assert.assertTrue(entry.isPublish());
            Assert.assertTrue(nodes.remove(entry.getNodeRef()));
        }
        Assert.assertTrue(nodes.isEmpty());
    }
    
    @Test
    public void testCancelScheduledEvent()
    {
        Channel channel = channelService.createChannel(siteId, channelTypeName, channelName, null);

        Set<NodeRef> nodes = new HashSet<NodeRef>();
        for (int i = 0; i < 4; ++i)
        {
            nodes.add(nodeService.createNode(channel.getNodeRef(), ContentModel.ASSOC_CONTAINS, QName.createQName(
                    NamespaceService.CONTENT_MODEL_1_0_URI, Integer.toString(i)), ContentModel.TYPE_CONTENT).getChildRef());
        }

        Environment liveEnvironment = publishingService.getEnvironment(siteId, PublishingService.LIVE_ENVIRONMENT_NAME);
        PublishingQueue liveQueue = liveEnvironment.getPublishingQueue();
        MutablePublishingPackage publishingPackage = liveQueue.createPublishingPackage();
        publishingPackage.addNodesToPublish(nodes);

        Calendar scheduleTime = Calendar.getInstance();
        scheduleTime.add(Calendar.HOUR, 1);
        String eventId = liveQueue.scheduleNewEvent(publishingPackage, channelName, scheduleTime, null, null);
        PublishingEvent event = publishingService.getPublishingEvent(eventId);
        Assert.assertNotNull(event);
        publishingService.cancelPublishingEvent(eventId);
        event = publishingService.getPublishingEvent(eventId);
        Assert.assertNull(event);
    }
}
