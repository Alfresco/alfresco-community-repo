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

import java.io.Serializable;
import java.util.Map;

import org.alfresco.service.cmr.publishing.channels.Channel;
import org.alfresco.service.cmr.publishing.channels.ChannelType;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * @author Brian
 * @author Nick Smith
 * @since 4.0
 */
public class ChannelImpl implements Channel
{
    private final NodeRef nodeRef;
    private final ChannelType channelType;
    private final String name;
    private final ChannelHelper channelHelper;

    public ChannelImpl(ChannelType channelType, NodeRef nodeRef, String name, ChannelHelper channelHelper)
    {
        this.nodeRef = nodeRef;
        this.channelType = channelType;
        this.name = name;
        this.channelHelper = channelHelper;
    }

    /**
    * {@inheritDoc}
    */
    public String getId()
    {
        return nodeRef.toString();
    }
    
    /**
    * {@inheritDoc}
     */
    public ChannelType getChannelType()
    {
        return channelType;
    }

    /**
     * {@inheritDoc}
    */
    public String getName()
    {
        return name;
    }

    /**
     * {@inheritDoc}
    */
    public NodeRef getNodeRef()
    {
        return nodeRef;
    }

    /**
     * {@inheritDoc}
    */
    public Map<QName, Serializable> getProperties()
    {
        return channelHelper.getChannelProperties(nodeRef);
    }

    /**
    * {@inheritDoc}
    */
    public void publish(NodeRef nodeToPublish)
    {
        channelHelper.addPublishedAspect(nodeToPublish, nodeRef);
        if (channelHelper.canPublish(nodeToPublish, channelType))
        {
            channelType.publish(nodeToPublish, getProperties());
        }
    }

    /**
    * {@inheritDoc}
    */
    public void unPublish(NodeRef nodeToUnpublish)
    {
        if (channelType.canUnpublish())
        {
            channelType.unpublish(nodeToUnpublish, getProperties());
        }
    }

    /**
    * {@inheritDoc}
    */
    public void updateStatus(String status, String nodeUrl)
    {
        if (channelType.canPublishStatusUpdates())
        {
            int urlLength = nodeUrl == null ? 0 : nodeUrl.length();
            int maxLength = channelType.getMaximumStatusLength() - urlLength;
            if (maxLength > 0)
            {
                int endpoint = Math.min(maxLength, status.length());
                status = status.substring(0, endpoint );
            }
            String msg = nodeUrl == null ? status : status + nodeUrl;
            channelType.updateStatus(this, msg, getProperties());
        }
    }

    /**
    * {@inheritDoc}
    */
    public String getUrl(NodeRef publishedNode)
    {
        NodeRef mappedNode = channelHelper.mapSourceToEnvironment(publishedNode, nodeRef);
        return channelType.getNodeUrl(mappedNode);
    }

    /**
    * {@inheritDoc}
     */
    public boolean isAuthorised()
    {
        return channelHelper.isChannelAuthorised(nodeRef);
    }

    /**
    * {@inheritDoc}
    */
    public boolean canPublish()
    {
        return channelType.canPublish() &&
            isAuthorised() &&
            channelHelper.hasPublishPermissions(nodeRef);
    }

    /**
    * {@inheritDoc}
    */
    public boolean canUnpublish()
    {
        return channelType.canPublish() &&
            isAuthorised() &&
            channelHelper.hasPublishPermissions(nodeRef);
    }

    /**
    * {@inheritDoc}
    */
    public boolean canPublishStatusUpdates()
    {
        return channelType.canPublish() &&
            isAuthorised() &&
            channelHelper.hasPublishPermissions(nodeRef);
    }
}
