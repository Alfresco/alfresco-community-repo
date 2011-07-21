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

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.publishing.MutablePublishingPackage;
import org.alfresco.service.cmr.publishing.PublishingEvent;
import org.alfresco.service.cmr.publishing.PublishingEvent.Status;
import org.alfresco.service.cmr.publishing.PublishingPackage;
import org.alfresco.service.cmr.publishing.PublishingPackageEntry;
import org.alfresco.service.cmr.publishing.PublishingService;
import org.alfresco.service.cmr.publishing.StatusUpdate;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.namespace.QName;
import org.junit.Test;

/**
 * @author Nick Smith
 * @since 4.0
 *
 */
public class PublishingQueueImplTest extends AbstractPublishingIntegrationTest
{
    private static final String channelId = "test://channel/node";
    private static final String comment = "The Comment";
    
    protected PublishingService publishingService;
    
    private WorkflowService workflowService;

    private String eventId;
    
    @Test
    public void testScheduleNewPublishingEvent() throws Exception
    {
        NodeRef firstNode = createContent("First");
        NodeRef secondNode = createContent("second");
        
        assertNull(nodeService.getProperty(firstNode, PROP_VERSION_LABEL));
        assertNull(nodeService.getProperty(firstNode, PROP_VERSION_LABEL));
        MutablePublishingPackage publishingPackage = queue.createPublishingPackage();
        publishingPackage.addNodesToPublish(firstNode, secondNode);

        //TODO Implement Unpublish
//        NodeRef thirdNode = fileFolderService.create(docLib, "third", ContentModel.TYPE_CONTENT).getNodeRef();
//        publishingPackage.addNodesToUnpublish(thirdNode);
        
        Calendar schedule = Calendar.getInstance();
        schedule.add(Calendar.HOUR, 2);
        
        this.eventId = queue.scheduleNewEvent(publishingPackage, channelId, schedule, comment, null);
        
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
            assertNotNull(entry.getSnapshot());
            if(entry.isPublish())
            {
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

//        assertEquals(1, toUnpublish.size());
//        assertTrue(toUnpublish.contains(thirdNode));
        
        // Check the correct version is recorded in the entry.
        PublishingPackageEntry entry = publishingPackage.getEntryMap().get(firstNode);
        assertEquals(version, entry.getSnapshot().getVersion());
        
        NodeRef eventNode = new NodeRef(eventId);
        String wfId = (String) nodeService.getProperty(eventNode, PROP_PUBLISHING_EVENT_WORKFLOW_ID);
        
        WorkflowInstance instance = workflowService.getWorkflowById(wfId);
        assertNotNull(instance);
        List<WorkflowPath> paths = workflowService.getWorkflowPaths(wfId);
        assertEquals(1, paths.size());
        Map<QName, Serializable> props = workflowService.getPathProperties(paths.get(0).getId());
        assertEquals(eventNode, props.get(PROP_WF_PUBLISHING_EVENT));
        assertEquals(schedule, props.get(PROP_WF_SCHEDULED_PUBLISH_DATE));
    }

    @Test
    public void testScheduleNewPublishingEventWithStatusUpdate() throws Exception
    {
        NodeRef firstNode = createContent("First");
        NodeRef secondNode = createContent("Second");
        
        List<String> channelNames = Arrays.asList("Channel1", "Channel2", "Channel3" );
        String message = "The message";
        StatusUpdate update = queue.createStatusUpdate(message, secondNode, channelNames);
        
        // Publish an event with the StatusUpdate
        MutablePublishingPackage publishingPackage = queue.createPublishingPackage();
        publishingPackage.addNodesToPublish(firstNode, secondNode);
        Calendar schedule = Calendar.getInstance();
        schedule.add(Calendar.HOUR, 2);
        this.eventId = queue.scheduleNewEvent(publishingPackage, channelId, schedule, comment, update);

        PublishingEvent event = publishingService.getPublishingEvent(eventId);
        StatusUpdate actualUpdate = event.getStatusUpdate();
        assertEquals(message, actualUpdate.getMessage());
        assertEquals(secondNode, actualUpdate.getNodeToLinkTo());
        Set<String> names = actualUpdate.getChannelIds();
        assertEquals(3, names.size());
        assertTrue(names.containsAll(channelNames));
    }
    
    private NodeRef createContent(String name)
    {
        return fileFolderService.create(docLib, name, ContentModel.TYPE_CONTENT).getNodeRef();
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
    
    /**
    * {@inheritDoc}
     * @throws Exception 
    */
    @Override
    public void onTearDown() throws Exception
    {
        if(eventId!=null)
        {
            try
            {
                publishingService.cancelPublishingEvent(eventId);
            }
            catch(Exception e)
            {
                //NOOP
            }
        }
        super.onTearDown();
    }
}
