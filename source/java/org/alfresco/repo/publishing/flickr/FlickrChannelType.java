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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.publishing.AbstractChannelType;
import org.alfresco.repo.publishing.PublishingModel;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.publishing.channels.Channel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import org.springframework.social.oauth1.AuthorizedRequestToken;
import org.springframework.social.oauth1.OAuth1Operations;
import org.springframework.social.oauth1.OAuth1Parameters;
import org.springframework.social.oauth1.OAuthToken;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class FlickrChannelType extends AbstractChannelType
{
    public final static String ID = "flickr";
    private NodeService nodeService;
    private FlickrPublishingHelper publishingHelper;
    private ActionService actionService;
    
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setPublishingHelper(FlickrPublishingHelper flickrPublishingHelper)
    {
        this.publishingHelper = flickrPublishingHelper;
    }

    public void setActionService(ActionService actionService)
    {
        this.actionService = actionService;
    }

    @Override
    public boolean canPublish()
    {
        return true;
    }

    @Override
    public boolean canPublishStatusUpdates()
    {
        return false;
    }

    @Override
    public boolean canUnpublish()
    {
        return true;
    }

    @Override
    public QName getChannelNodeType()
    {
        return FlickrPublishingModel.TYPE_DELIVERY_CHANNEL;
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
    public Set<String> getSupportedMimetypes()
    {
        return Collections.emptySet();
    }

    @Override
    public void publish(NodeRef nodeToPublish, Map<QName, Serializable> properties)
    {
        Action publishAction = actionService.createAction(FlickrPublishAction.NAME);
        actionService.executeAction(publishAction, nodeToPublish);
    }

    @Override
    public void unpublish(NodeRef nodeToUnpublish, Map<QName, Serializable> properties)
    {
    }

    @Override
    public void updateStatus(Channel channel, String status, Map<QName, Serializable> properties)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getNodeUrl(NodeRef node)
    {
        String url = null;
        if (node != null && nodeService.exists(node) && nodeService.hasAspect(node, FlickrPublishingModel.ASPECT_ASSET))
        {
            url = (String)nodeService.getProperty(node, FlickrPublishingModel.PROP_ASSET_URL);
        }
        return url;
    }
    
    @Override
    public String getAuthorisationUrl(Channel channel, String callbackUrl)
    {
        ParameterCheck.mandatory("channel", channel);
        ParameterCheck.mandatory("callbackUrl", callbackUrl);
        if (!ID.equals(channel.getChannelType().getId()))
        {
            throw new IllegalArgumentException("Invalid channel type: " + channel.getChannelType().getId());
        }
        OAuth1Operations oauthOperations = publishingHelper.getConnectionFactory().getOAuthOperations();
        OAuthToken requestToken = oauthOperations.fetchRequestToken(callbackUrl, null);

        NodeRef channelNodeRef = channel.getNodeRef();
        nodeService.setProperty(channelNodeRef, PublishingModel.PROP_OAUTH1_TOKEN_SECRET, requestToken.getSecret());
        nodeService.setProperty(channelNodeRef, PublishingModel.PROP_OAUTH1_TOKEN_VALUE, requestToken.getValue());

        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        params.add("perms", "delete");
        OAuth1Parameters oauthParams = new OAuth1Parameters(callbackUrl, params);
        return oauthOperations.buildAuthorizeUrl(requestToken.getValue(), oauthParams);
    }
    
    @Override
    public boolean acceptAuthorisationCallback(Channel channel, Map<String, String[]> callbackHeaders,
            Map<String, String[]> callbackParams)
    {
        boolean authorised = false;
        String[] verifier = callbackParams.get("oauth_verifier");
        if (verifier != null)
        {
            OAuth1Operations oauthOperations = publishingHelper.getConnectionFactory().getOAuthOperations();
            NodeRef channelNodeRef = channel.getNodeRef();

            Map<QName, Serializable> currentProps = nodeService.getProperties(channelNodeRef);
            String tokenValue = (String) currentProps.get(PublishingModel.PROP_OAUTH1_TOKEN_VALUE);
            String tokenSecret = (String) currentProps.get(PublishingModel.PROP_OAUTH1_TOKEN_SECRET);
            OAuthToken token = new OAuthToken(tokenValue, tokenSecret);
            OAuthToken accessToken = oauthOperations.exchangeForAccessToken(new AuthorizedRequestToken(token, verifier[0]), null);
            
            Map<QName, Serializable> newProps = new HashMap<QName, Serializable>();
            newProps.put(PublishingModel.PROP_OAUTH1_TOKEN_VALUE, accessToken.getValue());
            newProps.put(PublishingModel.PROP_OAUTH1_TOKEN_SECRET, accessToken.getSecret());
            getChannelService().updateChannel(channel, newProps);
            authorised = true;
        }
        return authorised;
    }
}
