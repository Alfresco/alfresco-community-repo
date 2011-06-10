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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.alfresco.repo.publishing.PublishingModel.PROP_PUBLISHING_EVENT_WORKFLOW_ID;
import static org.alfresco.repo.publishing.PublishingModel.PROP_WF_PUBLISHING_EVENT;
import static org.alfresco.repo.publishing.PublishingModel.PROP_WF_SCHEDULED_PUBLISH_DATE;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.publishing.MutablePublishingPackage;
import org.alfresco.service.cmr.publishing.PublishingEvent;
import org.alfresco.service.cmr.publishing.PublishingEvent.Status;
import org.alfresco.service.cmr.publishing.PublishingPackage;
import org.alfresco.service.cmr.publishing.PublishingPackageEntry;
import org.alfresco.service.cmr.publishing.PublishingService;
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
    @Resource(name="publishingService")
    protected PublishingService publishingService;
    
    private WorkflowService workflowService;

    private String eventId;
    
    @Test
    public void testScheduleNewPublishingEvent() throws Exception
    {
        NodeRef firstNode = fileFolderService.create(docLib, "First", ContentModel.TYPE_CONTENT).getNodeRef();
        NodeRef secondNode = fileFolderService.create(docLib, "second", ContentModel.TYPE_CONTENT).getNodeRef();
        
        MutablePublishingPackage publishingPackage = queue.createPublishingPackage();
        publishingPackage.addNodesToPublish(firstNode, secondNode);

        //TODO Implement Unpublish
//        NodeRef thirdNode = fileFolderService.create(docLib, "third", ContentModel.TYPE_CONTENT).getNodeRef();
//        publishingPackage.addNodesToUnpublish(thirdNode);
        
        String channelName = "The channel";
        Calendar schedule = Calendar.getInstance();
        schedule.add(Calendar.HOUR, 2);
        
        String comment = "The Comment";
        
        this.eventId = queue.scheduleNewEvent(publishingPackage, channelName, schedule, comment);
        
        PublishingEvent event = publishingService.getPublishingEvent(eventId);
        assertEquals(eventId, event.getId());
        assertEquals(comment, event.getComment());
        assertEquals(Status.SCHEDULED, event.getStatus());
        assertEquals(AuthenticationUtil.getAdminUserName(), event.getCreator());
        assertEquals(schedule, event.getScheduledTime());
        
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
        
        NodeRef eventNode = new NodeRef(eventId);
        String wfId = (String) nodeService.getProperty(eventNode, PROP_PUBLISHING_EVENT_WORKFLOW_ID);
        
        WorkflowInstance instance = workflowService.getWorkflowById(wfId);
        assertNotNull(instance);
        List<WorkflowPath> paths = workflowService.getWorkflowPaths(wfId);
        assertEquals(1, paths.size());
        Map<QName, Serializable> props = workflowService.getPathProperties(paths.get(0).getId());
        assertEquals(eventNode, props.get(PROP_WF_PUBLISHING_EVENT));
        assertEquals(schedule.getTime(), props.get(PROP_WF_SCHEDULED_PUBLISH_DATE));
        
        
    }
    
    /**
    * {@inheritDoc}
    */
    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        this.workflowService = serviceRegistry.getWorkflowService();
    }
    
    /**
    * {@inheritDoc}
    */
    @Override
    public void tearDown()
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
        super.tearDown();
    }
}
