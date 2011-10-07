/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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

import static org.alfresco.model.ContentModel.PROP_VERSION_LABEL;
import static org.alfresco.repo.publishing.PublishingModel.PROP_PUBLISHING_EVENT_WORKFLOW_ID;
import static org.alfresco.repo.publishing.PublishingModel.PROP_WF_PUBLISHING_EVENT;
import static org.alfresco.repo.publishing.PublishingModel.PROP_WF_SCHEDULED_PUBLISH_DATE;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.publishing.PublishingDetails;
import org.alfresco.service.cmr.publishing.PublishingEvent;
import org.alfresco.service.cmr.publishing.PublishingPackage;
import org.alfresco.service.cmr.publishing.PublishingPackageEntry;
import org.alfresco.service.cmr.publishing.PublishingService;
import org.alfresco.service.cmr.publishing.Status;
import org.alfresco.service.cmr.publishing.StatusUpdate;
import org.alfresco.service.cmr.publishing.channels.Channel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.junit.Test;

/**
 * @author Nick Smith
 * @since 4.0
 */
public class PublishingQueueImplTest extends AbstractPublishingIntegrationTest
{
    private static final String channelId = "test://channel/node";
    private static final String comment = "The Comment";
    
    private PublishingService publishingService;
    private WorkflowService workflowService;
    
    @Test
    public void testScheduleNewPublishingEvent() throws Exception
    {
        NodeRef firstNode = createContent("First");
        NodeRef secondNode = createContent("second");
        
        assertNull(nodeService.getProperty(firstNode, PROP_VERSION_LABEL));
        assertNull(nodeService.getProperty(firstNode, PROP_VERSION_LABEL));

        Calendar schedule = Calendar.getInstance();
        schedule.add(Calendar.HOUR, 2);
        
        PublishingDetails details = publishingService.createPublishingDetails()
            .addNodesToPublish(firstNode, secondNode)
            .setPublishChannelId(channelId)
            .setSchedule(schedule)
            .setComment(comment);
        
        String eventId = testHelper.scheduleEvent(details);
        
        //Check schedule triggered versioning.
        Serializable version = nodeService.getProperty(firstNode, PROP_VERSION_LABEL);
        assertNotNull(version);
        
        PublishingEvent event = publishingService.getPublishingEvent(eventId);
        assertEquals(eventId, event.getId());
        assertEquals(comment, event.getComment());
        assertEquals(Status.SCHEDULED, event.getStatus());
        assertEquals(AuthenticationUtil.getAdminUserName(), event.getCreator());
        assertEquals(schedule, event.getScheduledTime());
        assertEquals(channelId, event.getChannelId());
        assertNull(event.getStatusUpdate());
        
        PublishingPackage pckg = event.getPackage();
        ArrayList<NodeRef> toPublish = new ArrayList<NodeRef>(2);
        ArrayList<NodeRef> toUnpublish = new ArrayList<NodeRef>(1);
        for (PublishingPackageEntry entry : pckg.getEntries())
        {
            if (entry.isPublish())
            {
                assertNotNull(entry.getSnapshot());
                toPublish.add(entry.getNodeRef());
            }
            else
            {
                toUnpublish.add(entry.getNodeRef());
            }
        }
        
        assertEquals(2, toPublish.size());
        assertTrue(toPublish.contains(firstNode));
        assertTrue(toPublish.contains(secondNode));

        assertEquals(0, toUnpublish.size());
        
        // Check the correct version is recorded in the entry.
        PublishingPackageEntry entry = pckg.getEntryMap().get(firstNode);
        assertEquals(version, entry.getSnapshot().getVersion());
        
        NodeRef eventNode = new NodeRef(eventId);
        String wfId = (String) nodeService.getProperty(eventNode, PROP_PUBLISHING_EVENT_WORKFLOW_ID);
        
        WorkflowInstance instance = workflowService.getWorkflowById(wfId);
        assertNotNull(instance);
        List<WorkflowPath> paths = workflowService.getWorkflowPaths(wfId);
        assertEquals(2, paths.size());
        Map<QName, Serializable> props = workflowService.getPathProperties(paths.get(0).getId());
        assertEquals(eventNode, props.get(PROP_WF_PUBLISHING_EVENT));
        assertEquals(schedule, props.get(PROP_WF_SCHEDULED_PUBLISH_DATE));
    }

    @Test
    public void testScheduleNewPublishingEventWithStatusUpdate() throws Exception
    {
        NodeRef firstNode = createContent("First");
        NodeRef secondNode = createContent("Second");
        
        List<String> statusChannels = Arrays.asList("test://channel/Channel1", "test://channel/Channel2", "test://channel/Channel3" );
        String message = "The message";
        PublishingDetails details = publishingService.createPublishingDetails()
            .setPublishChannelId(channelId)
            .addNodesToPublish(firstNode, secondNode)
            .setStatusMessage(message)
            .setStatusNodeToLinkTo(secondNode)
            .addStatusUpdateChannels(statusChannels);
        
        // Publish an event with the StatusUpdate
        String eventId = testHelper.scheduleEvent1Year(details);

        PublishingEvent event = publishingService.getPublishingEvent(eventId);
        StatusUpdate actualUpdate = event.getStatusUpdate();
        assertEquals(message, actualUpdate.getMessage());
        assertEquals(secondNode, actualUpdate.getNodeToLinkTo());
        Set<String> names = actualUpdate.getChannelIds();
        assertEquals(3, names.size());
        assertTrue(names.containsAll(statusChannels));
    }
    
    @Test
    public void testScheduleNewEventPermissions() throws Exception
    {
        // Create Channels as Admin
        testHelper.mockChannelType(channelTypeId);
        Channel publishChannel = testHelper.createChannel(channelTypeId);
        Channel statusChannel = testHelper.createChannel(channelTypeId);
        
        NodeRef firstNode = createContent("First");
        NodeRef secondNode = createContent("Second");
        
        // Create User1, add read permissions and set as current user.
        String user1 = GUID.generate();
        personManager.createPerson(user1);
        testHelper.setChannelPermission(user1, publishChannel.getId(), PermissionService.READ);
        testHelper.setChannelPermission(user1, statusChannel.getId(), PermissionService.READ);
        personManager.setUser(user1);

        // Publish an event
        PublishingDetails details = publishingService.createPublishingDetails();
        details.addNodesToPublish(firstNode, secondNode);
        details.setPublishChannelId(publishChannel.getId());
        try
        {
            testHelper.scheduleEvent1Year(details);
            fail("shceduleNewEvent should have thrown an AccessDeniedException!");
        }
        catch(AlfrescoRuntimeException e)
        {
            //NOOP
        }
        
        // Set Add Child permission on publish channel.
        testHelper.allowChannelAccess(user1, publishChannel.getId());
        
        // Check publish works now.
        String eventId = testHelper.scheduleEvent1Year(details);
        assertNotNull(eventId);
        
        String message = "The message";
        details.setStatusMessage(message)
            .setStatusNodeToLinkTo(secondNode)
            .addStatusUpdateChannels(statusChannel.getId());
        try
        {
            eventId = testHelper.scheduleEvent1Year(details);
            fail("shceduleNewEvent with status update should have thrown an AccessDeniedException!");
        }
        catch(AlfrescoRuntimeException e)
        {
            //NOOP
        }
        
        // Set Add Child permission on status channel.
        testHelper.allowChannelAccess(user1, statusChannel.getId());

        // Check publish works now.
        eventId = testHelper.scheduleEvent1Year(details);
        assertNotNull(eventId);
    }

    private NodeRef createContent(String name)
    {
        return testHelper.createContentNode(name);
    }
    
    /**
    * {@inheritDoc}
    */
    @Override
    public void onSetUp() throws Exception
    {
        super.onSetUp();
        this.workflowService = serviceRegistry.getWorkflowService();
        this.publishingService = (PublishingService) getApplicationContext().getBean("publishingService");
    }
}
