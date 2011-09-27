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
package org.alfresco.repo.publishing.linkedin;

import static org.alfresco.repo.publishing.linkedin.LinkedInPublishingModel.TYPE_DELIVERY_CHANNEL;

import org.alfresco.repo.publishing.AbstractOAuth1ChannelType;
import org.alfresco.repo.publishing.linkedin.springsocial.api.AlfrescoLinkedIn;
import org.alfresco.service.cmr.publishing.channels.Channel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.social.connect.Connection;

/**
 * @author Nick Smith
 * @since 4.0
 */
public class LinkedInChannelType extends AbstractOAuth1ChannelType<AlfrescoLinkedIn>
{
    private final static Log log = LogFactory.getLog(LinkedInChannelType.class);
    public final static String ID = "linkedin";
    
    @Override
    public boolean canPublish()
    {
        return false;
    }

    @Override
    public boolean canPublishStatusUpdates()
    {
        return true;
    }

    @Override
    public boolean canUnpublish()
    {
        return false;
    }

    @Override
    public QName getChannelNodeType()
    {
        return TYPE_DELIVERY_CHANNEL;
    }

    @Override
    public String getId()
    {
        return ID;
    }

    @Override
    public int getMaximumStatusLength()
    {
        return 700;
    }

    @Override
    public void sendStatusUpdate(Channel channel, String status)
    {
        NodeRef channelNode = new NodeRef(channel.getId());
        Connection<AlfrescoLinkedIn> connection = getConnectionForChannel(channelNode);
        if (log.isInfoEnabled())
        {
            log.info("Posting update to LinkedIn channel " + channel.getName() + ": " + status);
        }
        connection.getApi().shareComment(status);
    }

    @Override
    public String getNodeUrl(NodeRef node)
    {
        return null;
    }
}
