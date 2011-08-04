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
package org.alfresco.repo.publishing.twitter;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.publishing.AbstractOAuth1ChannelType;
import org.alfresco.service.cmr.publishing.channels.Channel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.social.connect.Connection;
import org.springframework.social.twitter.api.Twitter;

public class TwitterChannelType extends AbstractOAuth1ChannelType<Twitter>
{
    private final static Log log = LogFactory.getLog(TwitterChannelType.class);
    public final static String ID = "twitter";

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
        return TwitterPublishingModel.TYPE_DELIVERY_CHANNEL;
    }

    @Override
    public String getId()
    {
        return ID;
    }

    @Override
    public Set<QName> getSupportedContentTypes()
    {
        return Collections.emptySet();
    }

    @Override
    public Set<String> getSupportedMimeTypes()
    {
        return Collections.emptySet();
    }

    @Override
    public void publish(NodeRef nodeToPublish, Map<QName, Serializable> properties)
    {
        //NO-OP
    }

    @Override
    public void unpublish(NodeRef nodeToUnpublish, Map<QName, Serializable> properties)
    {
        //NO-OP
    }

    @Override
    public int getMaximumStatusLength()
    {
        return 140;
    }

    @Override
    public void updateStatus(Channel channel, String status, Map<QName, Serializable> properties)
    {
        Connection<Twitter> connection = getConnectionForChannel(channel.getNodeRef());
        if (log.isInfoEnabled())
        {
            log.info("Posting update to Twitter channel " + channel.getName() + ": " + status);
        }
        connection.getApi().timelineOperations().updateStatus(status);
    }

    @Override
    public String getNodeUrl(NodeRef node)
    {
        throw new UnsupportedOperationException();
    }
    
}
