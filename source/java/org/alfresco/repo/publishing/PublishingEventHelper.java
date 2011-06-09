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

import static org.alfresco.repo.publishing.PublishingModel.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.publishing.PublishingEvent;
import org.alfresco.service.cmr.publishing.PublishingEventFilter;
import org.alfresco.service.cmr.publishing.PublishingEvent.Status;
import org.alfresco.service.cmr.publishing.PublishingPackage;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

/**
 * @author Brian
 * @author Nick Smith
 *
 */
public class PublishingEventHelper
{
    private static final Log log = LogFactory.getLog(PublishingEventHelper.class);
    public static final String WORKFLOW_DEFINITION_NAME = "publishWebContent";

    private NodeService nodeService;
    private ContentService contentService;
    private WorkflowService workflowService;
    private PublishingPackageSerializer serializer;
    
    private String workflowEngineId;
    
    /**
     * @param nodeService
     *            the nodeService to set
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @param contentService
     *            the contentService to set
     */
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    /**
     * @param workflowService the workflowService to set
     */
    public void setWorkflowService(WorkflowService workflowService)
    {
        this.workflowService = workflowService;
    }
    
    /**
     * @param workflowEngineId the workflowEngineId to set
     */
    public void setWorkflowEngineId(String workflowEngineId)
    {
        this.workflowEngineId = workflowEngineId;
    }
    
    /**
     * @param serializer the serializer to set
     */
    public void setSerializer(PublishingPackageSerializer serializer)
    {
        this.serializer = serializer;
    }

    public PublishingEvent getPublishingEvent(NodeRef eventNode) throws AlfrescoRuntimeException
    {
        if(eventNode == null)
        {
            return null;
        }
        
        Map<QName, Serializable> props = nodeService.getProperties(eventNode);
        Status status = (Status) props.get(PROP_PUBLISHING_EVENT_STATUS);
        PublishingPackage publishingPackage = getPayLoad(eventNode);
        Date createdTime = (Date) props.get(ContentModel.PROP_CREATED);
        String creator = (String) props.get(ContentModel.PROP_CREATOR);
        Date modifiedTime = (Date) props.get(ContentModel.PROP_MODIFIED);
        String modifier = (String) props.get(ContentModel.PROP_MODIFIER);
        String comment = (String) props.get(PROP_PUBLISHING_EVENT_COMMENT);
        Calendar scheduledTime = getScheduledTime(props);

        // TODO Implement PublishingEvent dependencies.
        Set<PublishingEvent> dependingEvents = Collections.emptySet();
        Set<PublishingEvent> eventsDependedOn = Collections.emptySet();
        Set<NodeRef> nodesDependedOn = Collections.emptySet();

        return new PublishingEventImpl(eventNode.toString(),
                status, publishingPackage,
                createdTime, creator,
                modifiedTime,modifier,
                dependingEvents, eventsDependedOn,
                nodesDependedOn, scheduledTime, comment);
    }

    public List<PublishingEvent> getPublishingEvents(List<NodeRef> eventNodes)
    {
        return Lists.transform(eventNodes, new Function<NodeRef, PublishingEvent>()
                {
                    public PublishingEvent apply(NodeRef eventNode)
                    {
                        return getPublishingEvent(eventNode);
                    }
                });
    }
    
    public NodeRef createNode(NodeRef queueNode, PublishingPackage publishingPackage, String channelName, Calendar schedule, String comment)
        throws Exception
    {
        if (schedule == null)
        {
            schedule = Calendar.getInstance();
        }
        Map<QName, Serializable> props = new HashMap<QName, Serializable>();
        String name = GUID.generate();
        props.put(ContentModel.PROP_NAME, name);
        props.put(PROP_PUBLISHING_EVENT_TIME, schedule.getTime());
        props.put(PublishingModel.PROP_PUBLISHING_EVENT_TIME_ZONE, schedule.getTimeZone().getID());
        props.put(PublishingModel.PROP_PUBLISHING_EVENT_CHANNEL, channelName);
        if (comment != null)
        {
            props.put(PROP_PUBLISHING_EVENT_COMMENT, comment);
        }
        ChildAssociationRef newAssoc = nodeService.createNode(queueNode, 
                ASSOC_PUBLISHING_EVENT,
                QName.createQName(NAMESPACE, name),
                TYPE_PUBLISHING_EVENT, props);
        NodeRef eventNode = newAssoc.getChildRef();
        setPayload(eventNode, publishingPackage);
        return eventNode;
    }

    public List<NodeRef> findPublishingEventNodes(NodeRef queue, PublishingEventFilter filter)
    {
        List<NodeRef> results = new ArrayList<NodeRef>();
        Set<String> ids = filter.getIds();
        if(ids != null)
        {
            for (String id : ids)
            {
                NodeRef eventNode = new NodeRef(id);
                if (nodeService.exists(eventNode))
                {
                    ChildAssociationRef parentAssoc = nodeService.getPrimaryParent(eventNode);
                    if (parentAssoc.getParentRef().equals(queue)
                            && ASSOC_PUBLISHING_EVENT.equals(parentAssoc.getTypeQName()))
                    {
                        results.add(eventNode);
                    }
                }
            }
        }
        return results;
    }

    public List<PublishingEvent> findPublishingEvents(NodeRef queue, PublishingEventFilter filter)
    {
        List<NodeRef> eventNodes = findPublishingEventNodes(queue, filter);
        return getPublishingEvents(eventNodes);
    }
    
    public PublishingEvent getPublishingEvent(String id)
    {
        NodeRef eventNode = getPublishingEventNode(id);
        return getPublishingEvent(eventNode);
    }
    
    public NodeRef getPublishingEventNode(String id)
    {
        if (id != null && NodeRef.isNodeRef(id))
        {
            NodeRef eventNode = new NodeRef(id);
            if (nodeService.exists(eventNode) && TYPE_PUBLISHING_EVENT.equals(nodeService.getType(eventNode)))
            {
                return eventNode;
            }
        }
        return null;
    }
    
    public String startPublishingWorkflow(NodeRef eventNode, Calendar scheduledTime)
    {
        //Set parameters
        Map<QName, Serializable> parameters = new HashMap<QName, Serializable>();
        parameters.put(PROP_WF_PUBLISHING_EVENT, eventNode);
        //TODO Will this handle the timezone?
        parameters.put(PROP_WF_SCHEDULED_PUBLISH_DATE, scheduledTime.getTime());
        
        //Start workflow
        WorkflowPath path = workflowService.startWorkflow(getPublshingDefinitionId(), parameters);
        String instanceId = path.getInstance().getId();
        
        //Set the Workflow Id on the event node.
        nodeService.setProperty(eventNode, PROP_PUBLISHING_EVENT_WORKFLOW_ID, instanceId);
        
        //End the start task.
        //TODO Replace with endStartTask() call after merge to HEAD.
        List<WorkflowTask> tasks = workflowService.getTasksForWorkflowPath(path.getId());
        WorkflowTask startTask = tasks.get(0);
        workflowService.endTask(startTask.getId(), null);
        return instanceId;
    }
    
    private String getPublshingDefinitionId()
    {
        String definitionName = workflowEngineId + "$" + WORKFLOW_DEFINITION_NAME;
        WorkflowDefinition definition = workflowService.getDefinitionByName(definitionName);
        if(definition == null)
        {
            String msg = "The Web publishing workflow definition does not exist! Definition name: " + definitionName;
            throw new AlfrescoRuntimeException(msg);
        }
        return definition.getId();
    }

    private Calendar getScheduledTime(Map<QName, Serializable> eventProperties)
    {
        Date time = (Date) eventProperties.get(PROP_PUBLISHING_EVENT_TIME);
        String timezone= (String) eventProperties.get(PROP_PUBLISHING_EVENT_TIME_ZONE);
        Calendar scheduledTime = Calendar.getInstance();
        scheduledTime.setTime(time);
        scheduledTime.setTimeZone(TimeZone.getTimeZone(timezone));
        return scheduledTime;
    }
    
    private void setPayload(NodeRef eventNode, PublishingPackage publishingPackage) throws Exception
    {
        try
        {
            ContentWriter contentWriter = contentService.getWriter(eventNode,
                    PROP_PUBLISHING_EVENT_PAYLOAD, true);
            contentWriter.setEncoding("UTF-8");
            OutputStream os = contentWriter.getContentOutputStream();
            serializer.serialize(publishingPackage, os);
            os.flush();
            os.close();
        }
        catch (Exception ex)
        {
            log.warn("Failed to serialize publishing package", ex);
            throw ex;
        }
    }
    
    private PublishingPackage getPayLoad(NodeRef eventNode) throws AlfrescoRuntimeException
    {
        ContentReader contentReader = contentService.getReader(eventNode, PROP_PUBLISHING_EVENT_PAYLOAD);
        InputStream input = contentReader.getContentInputStream();
        try
        {
            return serializer.deserialize(input);
        }
        catch (Exception ex)
        {
            String msg ="Failed to deserialize publishing package for PublishingEvent: " +eventNode;
            throw new AlfrescoRuntimeException(msg, ex);
        }
    }

}
