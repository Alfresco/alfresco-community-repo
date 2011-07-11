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
package org.alfresco.repo.publishing.facebook;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.publishing.AbstractChannelType;
import org.alfresco.repo.publishing.PublishingModel;
import org.alfresco.service.cmr.publishing.channels.Channel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import org.springframework.social.connect.Connection;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.oauth1.AuthorizedRequestToken;
import org.springframework.social.oauth1.OAuth1Operations;
import org.springframework.social.oauth1.OAuth1Parameters;
import org.springframework.social.oauth1.OAuthToken;
import org.springframework.social.oauth2.GrantType;
import org.springframework.social.oauth2.OAuth2Operations;
import org.springframework.social.oauth2.OAuth2Parameters;
import org.springframework.social.twitter.api.Twitter;

public class FacebookChannelType extends AbstractChannelType
{
    public final static String ID = "facebook";
    private NodeService nodeService;
    private FacebookPublishingHelper publishingHelper;
    
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setPublishingHelper(FacebookPublishingHelper facebookPublishingHelper)
    {
        this.publishingHelper = facebookPublishingHelper;
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
        return FacebookPublishingModel.TYPE_DELIVERY_CHANNEL;
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
    }

    @Override
    public void unpublish(NodeRef nodeToUnpublish, Map<QName, Serializable> properties)
    {
    }

    @Override
    public void updateStatus(Channel channel, String status, Map<QName, Serializable> properties)
    {
        Connection<Facebook> connection = publishingHelper.getFacebookConnectionForChannel(channel.getNodeRef());
        connection.updateStatus(status);
    }

    @Override
    public String getNodeUrl(NodeRef node)
    {
        String url = null;
        if (node != null && nodeService.exists(node) && nodeService.hasAspect(node, FacebookPublishingModel.ASPECT_ASSET))
        {
            url = (String)nodeService.getProperty(node, FacebookPublishingModel.PROP_ASSET_URL);
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
        
        OAuth2Operations oauthOperations = publishingHelper.getConnectionFactory().getOAuthOperations();
        return oauthOperations.buildAuthorizeUrl(GrantType.AUTHORIZATION_CODE, new OAuth2Parameters(callbackUrl));
    }
    
    @Override
    public boolean acceptAuthorisationCallback(Channel channel, Map<String, String[]> callbackHeaders,
            Map<String, String[]> callbackParams)
    {
        boolean authorised = false;
        //FIXME: BJR: 20110708: Write this.
//        String[] verifier = callbackParams.get("oauth_verifier");
//        if (verifier != null)
//        {
//            OAuth2Operations oauthOperations = publishingHelper.getConnectionFactory().getOAuthOperations();
//            NodeRef channelNodeRef = channel.getNodeRef();
//
//            Map<QName, Serializable> props = nodeService.getProperties(channelNodeRef);
//            String tokenValue = (String) props.get(PublishingModel.PROP_OAUTH1_TOKEN_VALUE);
//            String tokenSecret = (String) props.get(PublishingModel.PROP_OAUTH1_TOKEN_SECRET);
//            OAuthToken token = new OAuthToken(tokenValue, tokenSecret);
//            OAuthToken accessToken = oauthOperations.exchangeForAccessToken(new AuthorizedRequestToken(token, verifier[0]), null);
//            nodeService.setProperty(channelNodeRef, PublishingModel.PROP_OAUTH1_TOKEN_VALUE, accessToken.getValue());
//            nodeService.setProperty(channelNodeRef, PublishingModel.PROP_OAUTH1_TOKEN_SECRET, accessToken.getSecret());
//            
//            authorised = true;
//        }
        return authorised;
    }
}
