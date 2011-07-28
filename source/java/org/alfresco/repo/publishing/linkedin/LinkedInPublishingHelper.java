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

import org.alfresco.repo.publishing.PublishingModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.springframework.social.connect.Connection;
import org.springframework.social.oauth1.OAuthToken;
import org.springframework.social.twitter.api.Twitter;
import org.springframework.social.twitter.connect.TwitterConnectionFactory;

public class LinkedInPublishingHelper
{
    private NodeService nodeService;
    private TwitterConnectionFactory connectionFactory;

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setConnectionFactory(TwitterConnectionFactory connectionFactory)
    {
        this.connectionFactory = connectionFactory;
    }

    public TwitterConnectionFactory getConnectionFactory()
    {
        return connectionFactory;
    }

    public Connection<Twitter> getTwitterConnectionForChannel(NodeRef channelNode)
    {
        Connection<Twitter> connection = null;
        if (nodeService.exists(channelNode)
                && nodeService.hasAspect(channelNode, PublishingModel.ASPECT_OAUTH1_DELIVERY_CHANNEL))
        {
            String tokenValue = (String) nodeService.getProperty(channelNode, PublishingModel.PROP_OAUTH1_TOKEN_VALUE);
            String tokenSecret = (String) nodeService.getProperty(channelNode, PublishingModel.PROP_OAUTH1_TOKEN_SECRET);
            Boolean danceComplete = (Boolean) nodeService.getProperty(channelNode, PublishingModel.PROP_AUTHORISATION_COMPLETE);
            
            if (danceComplete)
            {
                OAuthToken token = new OAuthToken(tokenValue, tokenSecret);
                connection = connectionFactory.createConnection(token);
            }
        }
        return connection;
    }

}
