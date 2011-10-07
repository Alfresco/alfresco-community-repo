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

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.alfresco.service.cmr.publishing.PublishingDetails;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @author Brian
 * @author Nick Smith
 * 
 * @since 4.0
 */
public class PublishingDetailsImpl implements PublishingDetails
{
    private final Set<NodeRef> nodesToPublish = new HashSet<NodeRef>();
    private final Set<NodeRef> nodesToUnpublish= new HashSet<NodeRef>();
    private final Set<String> statusChannels = new HashSet<String>();
    
    private NodeRef nodeToLinkTo = null;
    private String message = null;
    private Calendar schedule = null;
    private String comment = null;
    private String publishChannelId = null;

    /**
    * {@inheritDoc}
     */
    public PublishingDetails addNodesToPublish(NodeRef... nodesToAdd)
    {
        return addNodesToPublish(Arrays.asList(nodesToAdd));
    }

    /**
    * {@inheritDoc}
     */
    public PublishingDetails addNodesToUnpublish(NodeRef... nodesToRemove)
    {
        return addNodesToUnpublish(Arrays.asList(nodesToRemove));
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public PublishingDetails addNodesToUnpublish(Collection<NodeRef> nodesToRemove)
    {
        this.nodesToUnpublish.addAll(nodesToRemove);
        return this;
    }

    /**
    * {@inheritDoc}
    */
    public PublishingDetails addNodesToPublish(Collection<NodeRef> nodesToAdd)
    {
        this.nodesToPublish.addAll(nodesToAdd);
        return this;
    }

    /**
    * {@inheritDoc}
    */
    public PublishingDetails setPublishChannelId(String publishChannelId)
    {
        this.publishChannelId = publishChannelId;
        return this;
    }

    /**
    * {@inheritDoc}
    */
    public PublishingDetails setComment(String comment)
    {
        this.comment = comment;
        return this;
    }

    /**
    * {@inheritDoc}
    */
    public PublishingDetails setSchedule(Calendar schedule)
    {
        this.schedule = schedule;
        return this;
    }

    /**
    * {@inheritDoc}
    */
    public PublishingDetails setStatusMessage(String message)
    {
        this.message = message;
        return this;
    }

    /**
    * {@inheritDoc}
    */
    public PublishingDetails setStatusNodeToLinkTo(NodeRef nodeToLinkTo)
    {
        this.nodeToLinkTo = nodeToLinkTo;
        return this;
    }

    /**
    * {@inheritDoc}
    */
    public PublishingDetails addStatusUpdateChannels(Collection<String> channelIds)
    {
        statusChannels.addAll(channelIds);
        return this;
    }

    /**
    * {@inheritDoc}
    */
    public PublishingDetails addStatusUpdateChannels(String... channelIds)
    {
        return addStatusUpdateChannels(Arrays.asList(channelIds));
    }


    /**
    * {@inheritDoc}
    */
    public Set<NodeRef> getNodesToPublish()
    {
        return nodesToPublish;
    }

    /**
    * {@inheritDoc}
    */
    public Set<NodeRef> getNodesToUnpublish()
    {
        return nodesToUnpublish;
    }

    /**
     * @return the statusChannels
     */
    public Set<String> getStatusChannels()
    {
        return statusChannels;
    }
    
    /**
     * @return the comment
     */
    public String getComment()
    {
        return comment;
    }
    
    /**
     * @return the message
     */
    public String getStatusMessage()
    {
        return message;
    }
    
    /**
     * @return the nodeToLinkTo
     */
    public NodeRef getNodeToLinkTo()
    {
        return nodeToLinkTo;
    }
    
    /**
     * @return the publishChannelId
     */
    public String getPublishChannelId()
    {
        return publishChannelId;
    }
    
    /**
     * @return the schedule
     */
    public Calendar getSchedule()
    {
        return schedule;
    }

    public Set<String> getStatusUpdateChannels()
    {
        return statusChannels;
    }
}
