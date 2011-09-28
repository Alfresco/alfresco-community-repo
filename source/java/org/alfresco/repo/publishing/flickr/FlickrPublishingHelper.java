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
package org.alfresco.repo.publishing.flickr;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.repo.node.encryption.MetadataEncryptor;
import org.alfresco.repo.publishing.PublishingModel;
import org.alfresco.repo.publishing.flickr.springsocial.api.Flickr;
import org.alfresco.repo.publishing.flickr.springsocial.connect.FlickrConnectionFactory;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.springframework.social.connect.Connection;
import org.springframework.social.oauth1.OAuthToken;

/**
 * A utility class to support the {@link FlickrChannelType}.
 * @author Brian
 * @since 4.0
 */
public class FlickrPublishingHelper
{
    private NodeService nodeService;
    private FlickrConnectionFactory connectionFactory;
    private MetadataEncryptor encryptor;

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setConnectionFactory(FlickrConnectionFactory connectionFactory)
    {
        this.connectionFactory = connectionFactory;
    }

    public void setEncryptor(MetadataEncryptor encryptor)
    {
        this.encryptor = encryptor;
    }

    public FlickrConnectionFactory getConnectionFactory()
    {
        return connectionFactory;
    }

    public Connection<Flickr> getConnectionFromChannelProps(Map<QName,Serializable> channelProperties)
    {
        Connection<Flickr> connection = null;
        String tokenValue = (String) encryptor.decrypt(PublishingModel.PROP_OAUTH1_TOKEN_VALUE, channelProperties
                .get(PublishingModel.PROP_OAUTH1_TOKEN_VALUE));
        String tokenSecret = (String) encryptor.decrypt(PublishingModel.PROP_OAUTH1_TOKEN_SECRET, channelProperties
                .get(PublishingModel.PROP_OAUTH1_TOKEN_SECRET));
        Boolean danceComplete = (Boolean) channelProperties.get(PublishingModel.PROP_AUTHORISATION_COMPLETE);
        
        if (danceComplete)
        {
            OAuthToken token = new OAuthToken(tokenValue, tokenSecret);
            connection = connectionFactory.createConnection(token);
        }
        return connection;
    }

    public Connection<Flickr> getConnectionForPublishNode(NodeRef publishNode)
    {
        Connection<Flickr> connection = null;
        NodeRef channelNode = nodeService.getPrimaryParent(publishNode).getParentRef();
        if (nodeService.exists(channelNode)
                && nodeService.hasAspect(channelNode, PublishingModel.ASPECT_OAUTH1_DELIVERY_CHANNEL))
        {
            connection = getConnectionFromChannelProps(nodeService.getProperties(channelNode));
        }
        return connection;
    }

}
