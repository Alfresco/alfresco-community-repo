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

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * A utility class to help out with environment-related operations that are used
 * by both the channel service and the publishing service.
 * 
 * @author Brian
 * @author Nick Smith
 * 
 */
public class EnvironmentHelper
{
    private PublishingRootObject rootObject;
    
    public NodeRef getChannelContainer()
    {
        return rootObject.getChannelContainer();
    }
    
    public PublishingQueueImpl getPublishingQueue()
    {
        return rootObject.getPublishingQueue();
    }
    
//    public NodePublishStatus checkNodeStatus(NodeRef node, String channelId)
//    {
//        PublishingEvent queuedEvent = getQueuedPublishingEvent(node, channelId);
//        PublishingEvent lastEvent= getLastPublishingEvent(node, environment, channelName);
//        if(queuedEvent != null)
//        {
//            if(lastEvent != null)
//            {
//                return new NodePublishStatusPublishedAndOnQueue(node, channelName, queuedEvent, lastEvent);
//            }
//            else
//            {
//                return  new NodePublishStatusOnQueue(node, channelName, queuedEvent);
//            }
//        }
//        else
//        {
//            if(lastEvent != null)
//            {
//                return new NodePublishStatusPublished(node, channelName, lastEvent);
//            }
//            else
//            {
//                return new NodePublishStatusNotPublished(node, channelName);
//            }
//        }
//    }
//
//    private PublishingEvent getQueuedPublishingEvent(NodeRef node, String channelId)
//    {
//        NodeRef queue = getPublishingQueue().getNodeRef();
//        Calendar nextPublishTime = null;
//        NodeRef nextEventNode = null;
//        List<NodeRef> eventNodes = publishingEventHelper.getEventNodesForPublishedNodes(queue, node);
//        for (NodeRef eventNode: eventNodes)
//        {
//            if (isActiveEvent(eventNode))
//            {
//                Map<QName, Serializable> props = nodeService.getProperties(eventNode);
//                Serializable eventChannel = props.get(PublishingModel.PROP_PUBLISHING_EVENT_CHANNEL);
//                if(channelId.equals(eventChannel))
//                {
//                    Calendar schedule = publishingEventHelper.getScheduledTime(props);
//                    if (nextPublishTime == null || schedule.before(nextPublishTime))
//                    {
//                        nextPublishTime = schedule;
//                        nextEventNode = eventNode;
//                    }
//                }
//            }
//        }
//        return publishingEventHelper.getPublishingEvent(nextEventNode);
//    }
//
//    private boolean isActiveEvent(NodeRef eventNode)
//    {
//        String statusStr = (String) nodeService.getProperty( eventNode, PROP_PUBLISHING_EVENT_STATUS);
//        Status status = Status.valueOf(statusStr);
//        return status == Status.IN_PROGRESS || status == Status.SCHEDULED;
//    }

}
