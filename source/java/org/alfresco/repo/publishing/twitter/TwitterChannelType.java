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
import org.springframework.social.connect.Connection;
import org.springframework.social.oauth1.OAuth1Operations;
import org.springframework.social.twitter.api.Twitter;

public class TwitterChannelType extends AbstractOAuth1ChannelType
{
    public final static String ID = "twitter";
    private TwitterPublishingHelper publishingHelper;
    
    public void setPublishingHelper(TwitterPublishingHelper twitterPublishingHelper)
    {
        this.publishingHelper = twitterPublishingHelper;
    }

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
    public void updateStatus(Channel channel, String status, Map<QName, Serializable> properties)
    {
        Connection<Twitter> connection = publishingHelper.getTwitterConnectionForChannel(channel.getNodeRef());
        connection.getApi().timelineOperations().updateStatus(status);
    }

    @Override
    public String getNodeUrl(NodeRef node)
    {
        throw new UnsupportedOperationException();
    }
    
    @Override
    protected OAuth1Operations getOAuth1Operations()
    {
        return publishingHelper.getConnectionFactory().getOAuthOperations();
    }
}
