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
package org.alfresco.service.cmr.publishing;

import java.util.Calendar;
import java.util.Collection;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * A simple DTO used to gather parameters for scheduling a Publishing Event.
 * 
 * @author Brian
 * @author Nick Smith
 *
 * @since 4.0
 */
public interface PublishingDetails
{
    PublishingDetails addNodesToUnpublish(NodeRef... nodesToRemove);

    PublishingDetails addNodesToUnpublish(Collection<NodeRef> nodesToRemove);

    PublishingDetails addNodesToPublish(NodeRef... nodesToPublish);
    
    PublishingDetails addNodesToPublish(Collection<NodeRef> nodesToPublish);
    
    PublishingDetails setPublishChannel(String channelId);
    
    PublishingDetails setComment(String comment);
    
    PublishingDetails setSchedule(Calendar schedule);
    
    PublishingDetails setStatusMessage(String message);
    
    PublishingDetails setStatusNodeToLinkTo(NodeRef nodeToLinkTo);
    
    PublishingDetails addStatusUpdateChannels(Collection<String> channelIds);
    PublishingDetails addStatusUpdateChannels(String... channelIds);
    
    /**
     * @return the comment
     */
     String getComment();
    
    /**
     * @return the message
     */
     String getStatusMessage();
    
    /**
     * @return the nodeToLinkTo
     */
    NodeRef getNodeToLinkTo();
    
    /**
     * @return the publishChannelId
     */
    String getPublishChannelId();
    
    /**
     * @return the schedule
     */
    Calendar getSchedule();

    Set<String> getStatusUpdateChannels();

    /**
     * @return a {@link Set} of all the {@link NodeRef}s to be published.
     */
    Set<NodeRef> getNodesToPublish();

    /**
     * @return a {@link Set} of all the {@link NodeRef}s to be unpublished.
     */
    Set<NodeRef> getNodesToUnpublish();

}
