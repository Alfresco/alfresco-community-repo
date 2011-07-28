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

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.publishing.AbstractOAuth1ChannelType;
import org.alfresco.service.cmr.publishing.channels.Channel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.social.connect.Connection;
import org.springframework.social.linkedin.api.LinkedIn;
import org.springframework.social.oauth1.OAuth1Parameters;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * @author Nick Smith
 * @since 4.0
 */
public class LinkedInChannelType extends AbstractOAuth1ChannelType<LinkedIn>
{
    public final static String ID = "linkedIn";
    
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
        NodeRef channelNode = new NodeRef(channel.getId());
        Connection<LinkedIn> connection = getConnectionForChannel(channelNode);
        // TODO update status
    }

    @Override
    public String getNodeUrl(NodeRef node)
    {
        throw new UnsupportedOperationException();
    }
    
    @Override
    protected OAuth1Parameters getOAuth1Parameters(String callbackUrl)
    {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        params.add("perms", "delete");
        return new OAuth1Parameters(callbackUrl, params);
    }

}
