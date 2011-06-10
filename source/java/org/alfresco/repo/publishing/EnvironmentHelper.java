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

import static org.alfresco.repo.publishing.PublishingModel.PROP_PUBLISHING_EVENT_NODES_TO_PUBLISH;
import static org.alfresco.repo.publishing.PublishingModel.PROP_PUBLISHING_EVENT_STATUS;
import static org.alfresco.repo.publishing.PublishingModel.PROP_PUBLISHING_EVENT_TIME;
import static org.alfresco.repo.publishing.PublishingModel.PROP_PUBLISHING_EVENT_TIME_ZONE;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.publishing.NodePublishStatus;
import org.alfresco.service.cmr.publishing.NodePublishStatusNotPublished;
import org.alfresco.service.cmr.publishing.NodePublishStatusOnQueue;
import org.alfresco.service.cmr.publishing.NodePublishStatusPublished;
import org.alfresco.service.cmr.publishing.NodePublishStatusPublishedAndOnQueue;
import org.alfresco.service.cmr.publishing.PublishingEvent;
import org.alfresco.service.cmr.publishing.PublishingEvent.Status;
import org.alfresco.service.cmr.publishing.PublishingService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;

/**
 * A utility class to help out with environment-related operations that are used
 * by both the channel service and the publishing service.
 * 
 * @author Brian
 * 
 */
public class EnvironmentHelper
{
    private static final String ENVIRONMENT_CONTAINER_NAME = "environments";
    private static final Set<QName> PUBLISHING_QUEUE_TYPE = new HashSet<QName>();
    private Set<QName> environmentNodeTypes;
    
    private SiteService siteService;
    private NodeService nodeService;
    private PublishingEventHelper publishingEventHelper; 
    private ChannelHelper channelHelper;
    
    static
    {
        PUBLISHING_QUEUE_TYPE.add(PublishingModel.TYPE_PUBLISHING_QUEUE);
    }

    public EnvironmentHelper()
    {
        environmentNodeTypes = new HashSet<QName>();
        environmentNodeTypes.add(PublishingModel.TYPE_ENVIRONMENT);
    }

    /**
     * @param environmentNodeTypes
     *            the environmentNodeTypes to set
     */
    public void setEnvironmentNodeTypes(Set<QName> environmentNodeTypes)
    {
        this.environmentNodeTypes = environmentNodeTypes;
    }

    /**
     * @param siteService
     *            the siteService to set
     */
    public void setSiteService(SiteService siteService)
    {
        this.siteService = siteService;
    }

    /**
     * @param nodeService
     *            the nodeService to set
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @param publishingEventHelper the publishingEventHelper to set
     */
    public void setPublishingEventHelper(PublishingEventHelper publishingEventHelper)
    {
        this.publishingEventHelper = publishingEventHelper;
    }
    
    /**
     * @param channelHelper the channelHelper to set
     */
    public void setChannelHelper(ChannelHelper channelHelper)
    {
        this.channelHelper = channelHelper;
    }
    
    public Map<String, NodeRef> getEnvironments(String siteId)
    {
        Map<String, NodeRef> results = new TreeMap<String, NodeRef>();
        NodeRef environmentContainer = getEnvironmentContainer(siteId);
        List<ChildAssociationRef> envAssocs = nodeService.getChildAssocs(environmentContainer,
                getEnvironmentNodeTypes());
        for (ChildAssociationRef envAssoc : envAssocs)
        {
            NodeRef environment = envAssoc.getChildRef();
            String name = (String) nodeService.getProperty(environment, ContentModel.PROP_NAME);
            results.put(name, environment);
        }
        return results;
    }


    public NodeRef getEnvironment(String siteId, String name)
    {
        ParameterCheck.mandatory("siteId", siteId);
        ParameterCheck.mandatory("name", name);

        NodeRef environmentContainer = getEnvironmentContainer(siteId);
        return nodeService.getChildByName(environmentContainer, ContentModel.ASSOC_CONTAINS, name);
    }

    public NodeRef getPublishingQueue(NodeRef environment)
    {
        ParameterCheck.mandatory("environment", environment);

        ChildAssociationRef queueAssoc = null;
        List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(environment, PUBLISHING_QUEUE_TYPE);
        for (ChildAssociationRef childAssoc : childAssocs)
        {
            if (childAssoc.getTypeQName().equals(PublishingModel.ASSOC_PUBLISHING_QUEUE))
            {
                queueAssoc = childAssoc;
                break;
            }
        }

        if (queueAssoc == null)
        {
            // No publishing queue
            queueAssoc = nodeService.createNode(environment,
                    PublishingModel.ASSOC_PUBLISHING_QUEUE,
                    QName.createQName(PublishingModel.NAMESPACE, "publishingQueue"),
                    PublishingModel.TYPE_PUBLISHING_QUEUE);
        }
        return queueAssoc.getChildRef();
    }

    /**
     * @return
     */
    protected Set<QName> getEnvironmentNodeTypes()
    {
        return environmentNodeTypes;
    }

    /**
     * @param siteId
     * @return
     */
    private NodeRef getEnvironmentContainer(final String siteId)
    {
        return AuthenticationUtil.runAs(new RunAsWork<NodeRef>()
        {
            public NodeRef doWork() throws Exception
            {
                NodeRef environmentContainer = siteService.getContainer(siteId, ENVIRONMENT_CONTAINER_NAME);
                if (environmentContainer == null)
                {
                    // No environment container exists for this site yet. Create
                    // it.
                    environmentContainer = siteService.createContainer(siteId, ENVIRONMENT_CONTAINER_NAME,
                            PublishingModel.TYPE_CHANNEL_CONTAINER, null);

                    // Also create the default live environment
                    Map<QName, Serializable> props = new HashMap<QName, Serializable>();
                    props.put(ContentModel.PROP_NAME, PublishingService.LIVE_ENVIRONMENT_NAME);
                    nodeService.createNode(environmentContainer, ContentModel.ASSOC_CONTAINS, QName.createQName(
                            NamespaceService.CONTENT_MODEL_1_0_URI, PublishingService.LIVE_ENVIRONMENT_NAME),
                            PublishingModel.TYPE_ENVIRONMENT, props);
                }
                return environmentContainer;
            }
        }, AuthenticationUtil.getSystemUserName());

    }

    public NodePublishStatus checkNodeStatus(NodeRef node, EnvironmentImpl environment, String channelName)
    {
        PublishingEvent queuedEvent = getQueuedPublishingEvent(node, environment, channelName);
        PublishingEvent lastEvent= getLastPublishingEvent(node, environment, channelName);
        if(queuedEvent != null)
        {
            if(lastEvent != null)
            {
                return new NodePublishStatusPublishedAndOnQueue(node, environment, channelName, queuedEvent, lastEvent);
            }
            else
            {
                return  new NodePublishStatusOnQueue(node, environment, channelName, queuedEvent);
            }
        }
        else
        {
            if(lastEvent != null)
            {
                return new NodePublishStatusPublished(node, environment, channelName, lastEvent);
            }
            else
            {
                return new NodePublishStatusNotPublished(node, environment, channelName);
            }
        }
    }

    private PublishingEvent getQueuedPublishingEvent(NodeRef node, EnvironmentImpl environment, String channelName)
    {
        NodeRef queue = getPublishingQueue(environment.getNodeRef());
        Calendar nextPublishTime = null;
        NodeRef nextEventNode = null;
        List<ChildAssociationRef> results = nodeService.getChildAssocsByPropertyValue( queue, 
                PROP_PUBLISHING_EVENT_NODES_TO_PUBLISH, node.toString());
        for (ChildAssociationRef childAssoc : results)
        {
            NodeRef child = childAssoc.getChildRef();
            if (isActiveEvent(child))
            {
                Serializable eventChannel = nodeService.getProperty(child, PublishingModel.PROP_PUBLISHING_EVENT_CHANNEL);
                if(channelName.equals(eventChannel))
                {
                    Calendar schedule = getScheduledTime(child);
                    if (nextPublishTime == null || schedule.before(nextPublishTime))
                    {
                        nextPublishTime = schedule;
                        nextEventNode = child;
                    }
                }
            }
        }
        return publishingEventHelper.getPublishingEvent(nextEventNode);
    }

    private Calendar getScheduledTime(NodeRef child)
    {
        Date time = (Date) nodeService.getProperty( child, PROP_PUBLISHING_EVENT_TIME);
        String timeZone = (String) nodeService.getProperty( child,PROP_PUBLISHING_EVENT_TIME_ZONE);
        Calendar schedule = Calendar.getInstance();
        schedule.setTimeZone(TimeZone.getTimeZone(timeZone));
        schedule.setTime(time);
        return schedule;
    }

    private boolean isActiveEvent(NodeRef eventNode)
    {
        String statusStr = (String) nodeService.getProperty( eventNode, PROP_PUBLISHING_EVENT_STATUS);
        Status status = Status.valueOf(statusStr);
        return status == Status.IN_PROGRESS || status == Status.SCHEDULED;
    }

    private PublishingEvent getLastPublishingEvent(NodeRef node, EnvironmentImpl environment, String channelName)
    {
        NodeRef mappedNode = channelHelper.mapSourceToEnvironment(node, environment.getNodeRef(), channelName);
        if(mappedNode==null || nodeService.exists(mappedNode)==false)
        {
            return null; // Node is not published.
        }
        //TODO Find the publish event.
        return null;
    }

}
