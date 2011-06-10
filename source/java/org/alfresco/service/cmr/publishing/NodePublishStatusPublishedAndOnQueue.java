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

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @author Brian
 *
 */
public class NodePublishStatusPublishedAndOnQueue extends BaseNodePublishStatus
{

    private final PublishingEvent queuedPublishingEvent;
    private final PublishingEvent latestPublishingEvent;

    /**
     * @param nodeRef
     * @param environment
     * @param channelName TODO
     * @param queuedPublishingEvent The next scheduled {@link PublishingEvent} on the {@link PublishingQueue}
     * @param latestPublishingEvent The last {@link PublishingEvent} to successfully publish the node.
     */
    public NodePublishStatusPublishedAndOnQueue(NodeRef nodeRef, Environment environment,
            String channelName, PublishingEvent queuedPublishingEvent, PublishingEvent latestPublishingEvent)
    {
        super(nodeRef, environment, channelName);
        this.queuedPublishingEvent = queuedPublishingEvent;
        this.latestPublishingEvent = latestPublishingEvent;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.publishing.NodePublishStatus#visit(org.alfresco.service.cmr.publishing.NodePublishStatusVisitor)
     */
    @Override
    public <T> T visit(NodePublishStatusVisitor<T> visitor)
    {
        return visitor.accept(this);
    }

    public PublishingEvent getQueuedPublishingEvent()
    {
        return queuedPublishingEvent;
    }

    /**
     * Retrieve the most recent publishing event that affected (created or updated) the node relevant to this status.
     * @return
     */
    public PublishingEvent getLatestPublishingEvent()
    {
        return latestPublishingEvent;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.publishing.NodePublishStatus#getStatus()
     */
    @Override
    public Status getStatus()
    {
        return Status.PUBLISHED_AND_ON_QUEUE;
    }
}
