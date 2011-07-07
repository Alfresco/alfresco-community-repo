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

import static org.alfresco.repo.publishing.PublishingModel.ASSOC_PUBLISHING_EVENT;
import static org.alfresco.repo.publishing.PublishingModel.NAMESPACE;
import static org.alfresco.repo.publishing.PublishingModel.PROP_PUBLISHING_EVENT_CHANNEL;
import static org.alfresco.repo.publishing.PublishingModel.PROP_PUBLISHING_EVENT_COMMENT;
import static org.alfresco.repo.publishing.PublishingModel.PROP_PUBLISHING_EVENT_NODES_TO_PUBLISH;
import static org.alfresco.repo.publishing.PublishingModel.PROP_PUBLISHING_EVENT_NODES_TO_UNPUBLISH;
import static org.alfresco.repo.publishing.PublishingModel.PROP_PUBLISHING_EVENT_PAYLOAD;
import static org.alfresco.repo.publishing.PublishingModel.PROP_PUBLISHING_EVENT_STATUS;
import static org.alfresco.repo.publishing.PublishingModel.PROP_PUBLISHING_EVENT_TIME;
import static org.alfresco.repo.publishing.PublishingModel.PROP_PUBLISHING_EVENT_TIME_ZONE;
import static org.alfresco.repo.publishing.PublishingModel.PROP_PUBLISHING_EVENT_WORKFLOW_ID;
import static org.alfresco.repo.publishing.PublishingModel.PROP_STATUS_UPDATE_CHANNEL_NAMES;
import static org.alfresco.repo.publishing.PublishingModel.PROP_STATUS_UPDATE_MESSAGE;
import static org.alfresco.repo.publishing.PublishingModel.PROP_STATUS_UPDATE_NODE_REF;
import static org.alfresco.repo.publishing.PublishingModel.PROP_WF_PUBLISHING_EVENT;
import static org.alfresco.repo.publishing.PublishingModel.PROP_WF_SCHEDULED_PUBLISH_DATE;
import static org.alfresco.repo.publishing.PublishingModel.TYPE_PUBLISHING_EVENT;
import static org.alfresco.util.collections.CollectionUtils.filter;
import static org.alfresco.util.collections.CollectionUtils.isEmpty;
import static org.alfresco.util.collections.CollectionUtils.toListOfStrings;
import static org.alfresco.util.collections.CollectionUtils.transform;
import static org.alfresco.util.collections.CollectionUtils.transformFlat;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeUtils;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.publishing.PublishingEvent;
import org.alfresco.service.cmr.publishing.PublishingEvent.Status;
import org.alfresco.service.cmr.publishing.PublishingEventFilter;
import org.alfresco.service.cmr.publishing.PublishingPackage;
import org.alfresco.service.cmr.publishing.StatusUpdate;
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
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.GUID;
import org.alfresco.util.collections.Filter;
import org.alfresco.util.collections.Function;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
        String statusStr = (String) props.get(PROP_PUBLISHING_EVENT_STATUS);
        Status status = Status.valueOf(statusStr);
        PublishingPackage publishingPackage = getPayLoad(eventNode);
        Date createdTime = (Date) props.get(ContentModel.PROP_CREATED);
        String creator = (String) props.get(ContentModel.PROP_CREATOR);
        Date modifiedTime = (Date) props.get(ContentModel.PROP_MODIFIED);
        String modifier = (String) props.get(ContentModel.PROP_MODIFIER);
        String comment = (String) props.get(PROP_PUBLISHING_EVENT_COMMENT);
        Calendar scheduledTime = getScheduledTime(props);

        String channel = (String) props.get(PROP_PUBLISHING_EVENT_CHANNEL);
        StatusUpdate statusUpdate = buildStatusUpdate(props);
        return new PublishingEventImpl(eventNode.toString(),
                status, channel,
                publishingPackage, createdTime,
                creator,modifiedTime, modifier,
                scheduledTime, comment, statusUpdate);
    }

    @SuppressWarnings("unchecked")
    private StatusUpdate buildStatusUpdate(Map<QName, Serializable> props)
    {
        String message = (String) props.get(PROP_STATUS_UPDATE_MESSAGE);
        Collection<String> channelNames = (Collection<String>) props.get(PROP_STATUS_UPDATE_CHANNEL_NAMES);
        if(channelNames == null || channelNames.isEmpty())
        {
            return null;
        }
        String nodeId = (String) props.get(PROP_STATUS_UPDATE_NODE_REF);
        NodeRef nodeToLinkTo = nodeId==null ? null : new NodeRef(nodeId);
        return new StatusUpdateImpl(message, nodeToLinkTo, channelNames);
    }

    public List<PublishingEvent> getPublishingEvents(List<NodeRef> eventNodes)
    {
        return transform(eventNodes, new Function<NodeRef, PublishingEvent>()
                {
                    public PublishingEvent apply(NodeRef eventNode)
                    {
                        return getPublishingEvent(eventNode);
                    }
                });
    }
    
    public NodeRef createNode(NodeRef queueNode, PublishingPackage publishingPackage, String channelName, Calendar schedule, String comment, StatusUpdate statusUpdate)
        throws Exception
    {
        if (schedule == null)
        {
            schedule = Calendar.getInstance();
        }
        String name = GUID.generate();
        Map<QName, Serializable> props = 
            buildPublishingEventProperties(publishingPackage, channelName, schedule, comment, statusUpdate, name);
        ChildAssociationRef newAssoc = nodeService.createNode(queueNode, 
                ASSOC_PUBLISHING_EVENT,
                QName.createQName(NAMESPACE, name),
                TYPE_PUBLISHING_EVENT, props);
        NodeRef eventNode = newAssoc.getChildRef();
        setPayload(eventNode, publishingPackage);
        return eventNode;
    }

    private Map<QName, Serializable> buildPublishingEventProperties(PublishingPackage publishingPackage,
            String channelName, Calendar schedule, String comment, StatusUpdate statusUpdate, String name)
    {
        Map<QName, Serializable> props = new HashMap<QName, Serializable>();
        props.put(ContentModel.PROP_NAME, name);
        props.put(PROP_PUBLISHING_EVENT_STATUS, Status.IN_PROGRESS.name());
        props.put(PROP_PUBLISHING_EVENT_TIME, schedule.getTime());
        props.put(PublishingModel.PROP_PUBLISHING_EVENT_TIME_ZONE, schedule.getTimeZone().getID());
        props.put(PublishingModel.PROP_PUBLISHING_EVENT_CHANNEL, channelName);
        props.put(PublishingModel.PROP_PUBLISHING_EVENT_STATUS, PublishingModel.PROPVAL_PUBLISHING_EVENT_STATUS_SCHEDULED);
        if (comment != null)
        {
            props.put(PROP_PUBLISHING_EVENT_COMMENT, comment);
        }
        Collection<String> publshStrings = mapNodesToStrings(publishingPackage.getNodesToPublish());
        props.put(PROP_PUBLISHING_EVENT_NODES_TO_PUBLISH, (Serializable) publshStrings);
        Collection<String> unpublshStrings = mapNodesToStrings(publishingPackage.getNodesToUnpublish());
        props.put(PROP_PUBLISHING_EVENT_NODES_TO_UNPUBLISH, (Serializable) unpublshStrings);
        if(statusUpdate != null)
        {
            props.put(PROP_STATUS_UPDATE_MESSAGE, statusUpdate.getMessage());
            NodeRef statusNode = statusUpdate.getNodeToLinkTo();
            if(statusNode != null)
            {
                props.put(PROP_STATUS_UPDATE_NODE_REF, statusNode.toString());
            }
            props.put(PROP_STATUS_UPDATE_CHANNEL_NAMES, (Serializable) statusUpdate.getChannelNames());
        }
        return props;
    }

    private List<String> mapNodesToStrings(Collection<NodeRef> nodes)
    {
        return toListOfStrings(nodes);
    }

    public List<NodeRef> findPublishingEventNodes(final NodeRef queue, PublishingEventFilter filter)
    {
        List<NodeRef> eventNodes;
        Set<NodeRef> publishedNodes = filter.getPublishedNodes();
        if(isEmpty(publishedNodes) == false)
        {
            eventNodes= getEventNodesForPublishedNodes(queue, publishedNodes);
        }
        else
        {
            eventNodes = getAllPublishingEventNodes(queue);
        }
        Set<String> ids = filter.getIds();
        if(isEmpty(ids) == false)
        {
            eventNodes = filterEventNodesById(eventNodes, ids);
        }
        return eventNodes;
    }

    private List<NodeRef> filterEventNodesById(Collection<NodeRef> eventNodes, final Collection<String> ids)
    {
        return filter(eventNodes, new Filter<NodeRef>()
        {
            public Boolean apply(NodeRef node)
            {
                return ids.contains(node.toString());
            }
        });
    }

    private List<NodeRef> getAllPublishingEventNodes(final NodeRef queue)
    {
        List<ChildAssociationRef> assocs =
            nodeService.getChildAssocs(queue, ASSOC_PUBLISHING_EVENT, RegexQNamePattern.MATCH_ALL);
        return transform(assocs, NodeUtils.toChildRef());
    }

    /**
     * Returns a {@link List} of the {@link NodeRef}s representing PublishingEvents that were scheduled to publish at least one of the specified <code>publishedNodes</code>. 
     * @param queue
     * @param publishedNodes
     * @return
     */
    public List<NodeRef> getEventNodesForPublishedNodes(final NodeRef queue, NodeRef... publishedNodes)
    {
        return getEventNodesForPublishedNodes(queue, Arrays.asList(publishedNodes));
    }

    /**
     * Returns a {@link List} of the {@link NodeRef}s representing PublishingEvents that were scheduled to publish at least one of the specified <code>publishedNodes</code>. 
     * @param queue
     * @param publishedNodes
     * @return
     */
    public List<NodeRef> getEventNodesForPublishedNodes(final NodeRef queue, Collection<NodeRef> publishedNodes)
    {
        Function<NodeRef, Collection<NodeRef>>  transformer = new Function<NodeRef, Collection<NodeRef>>()
        {
            public Collection<NodeRef> apply(NodeRef publishedNode)
            {
                List<ChildAssociationRef> assocs = nodeService.getChildAssocsByPropertyValue(queue, PROP_PUBLISHING_EVENT_NODES_TO_PUBLISH, publishedNode.toString());
                return transform(assocs, NodeUtils.toChildRef());
            }
        };
        List<NodeRef> nodes = transformFlat(publishedNodes, transformer);
        return nodes;
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
        parameters.put(WorkflowModel.ASSOC_PACKAGE, workflowService.createPackage(null));
        parameters.put(PROP_WF_SCHEDULED_PUBLISH_DATE, scheduledTime);
        
        //Start workflow
        WorkflowPath path = workflowService.startWorkflow(getPublshingWorkflowDefinitionId(), parameters);
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
    
    private String getPublshingWorkflowDefinitionId()
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

    public Calendar getScheduledTime(NodeRef eventNode)
    {
        if(eventNode == null)
        {
            return null;
        }
        return getScheduledTime(nodeService.getProperties(eventNode));
    }

    public Calendar getScheduledTime(Map<QName, Serializable> eventProperties)
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

    public void cancelEvent(String id)
    {
        NodeRef eventNode = getPublishingEventNode(id);
        if (eventNode != null)
        {
            Map<QName,Serializable> eventProps = nodeService.getProperties(eventNode);
            String status = (String)eventProps.get(PublishingModel.PROP_PUBLISHING_EVENT_STATUS);
            //If this event has not started to be processed yet then we can stop the associated workflow and
            //delete the event...
            if (PublishingModel.PROPVAL_PUBLISHING_EVENT_STATUS_SCHEDULED.equals(status))
            {
                //Get hold of the process id
                String processId = (String)eventProps.get(PublishingModel.PROP_PUBLISHING_EVENT_WORKFLOW_ID);
                if (processId != null)
                {
                    workflowService.cancelWorkflow(processId);
                }
                nodeService.deleteNode(eventNode);
            }
            
            //Otherwise, if the current event is being processed now we just set its status to "CANCELLED REQUESTED"
            else if (PublishingModel.PROPVAL_PUBLISHING_EVENT_STATUS_IN_PROGRESS.equals(status))
            {
                nodeService.setProperty(eventNode, PublishingModel.PROP_PUBLISHING_EVENT_STATUS, PublishingModel.PROPVAL_PUBLISHING_EVENT_STATUS_CANCEL_REQUESTED);
            }
            
            //Otherwise this event has already been processed or has already been cancelled. Do nothing.
        }
    }

    public NodeRef getEnvironmentNodeForPublishingEvent(NodeRef publishingEvent)
    {
        ChildAssociationRef queueAssoc = nodeService.getPrimaryParent(publishingEvent);
        ChildAssociationRef environmentAssoc = nodeService.getPrimaryParent(queueAssoc.getParentRef());
        NodeRef environment = environmentAssoc.getParentRef();
        return environment;
    }
}
