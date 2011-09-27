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
import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.publishing.channels.Channel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.support.OAuth1ConnectionFactory;
import org.springframework.social.oauth1.AuthorizedRequestToken;
import org.springframework.social.oauth1.OAuth1Operations;
import org.springframework.social.oauth1.OAuth1Parameters;
import org.springframework.social.oauth1.OAuthToken;

/**
 * @author Brian
 * @author Nick Smith
 * @since 4.0
 *
 * @param <A> The API type, e.g. Twitter, Flickr, LinkedIn, etc.
 */
public abstract class AbstractOAuth1ChannelType<A> extends AbstractChannelType
{
    private OAuth1ConnectionFactory<A> connectionFactory;
    
    protected Connection<A> getConnectionForChannel(NodeRef channelNode)
    {
        NodeService nodeService = getNodeService();
        Connection<A> connection = null;
        if (nodeService.exists(channelNode)
                && nodeService.hasAspect(channelNode, PublishingModel.ASPECT_OAUTH1_DELIVERY_CHANNEL))
        {
            String tokenValue = (String) getEncryptor().decrypt(PublishingModel.PROP_OAUTH1_TOKEN_VALUE, nodeService
                    .getProperty(channelNode, PublishingModel.PROP_OAUTH1_TOKEN_VALUE));
            String tokenSecret = (String) getEncryptor().decrypt(PublishingModel.PROP_OAUTH1_TOKEN_SECRET, nodeService
                    .getProperty(channelNode, PublishingModel.PROP_OAUTH1_TOKEN_SECRET));
            Boolean danceComplete = (Boolean) nodeService.getProperty(channelNode, PublishingModel.PROP_AUTHORISATION_COMPLETE);
            
            if (danceComplete)
            {
                OAuthToken token = new OAuthToken(tokenValue, tokenSecret);
                connection = connectionFactory.createConnection(token);
            }
        }
        return connection;
    }
    
    @Override
    public String getAuthorisationUrl(Channel channel, String callbackUrl)
    {
        ParameterCheck.mandatory("channel", channel);
        ParameterCheck.mandatory("callbackUrl", callbackUrl);
        if (!getId().equals(channel.getChannelType().getId()))
        {
            throw new IllegalArgumentException("Invalid channel type: " + channel.getChannelType().getId());
        }
        
        NodeService nodeService = getNodeService();
        OAuth1Operations oauthOperations = getOAuth1Operations();
        OAuthToken requestToken = oauthOperations.fetchRequestToken(callbackUrl, null);

        NodeRef channelNodeRef = channel.getNodeRef();
        nodeService.setProperty(channelNodeRef, PublishingModel.PROP_OAUTH1_TOKEN_SECRET, 
                getEncryptor().encrypt(PublishingModel.PROP_OAUTH1_TOKEN_SECRET, requestToken.getSecret()));
        nodeService.setProperty(channelNodeRef, PublishingModel.PROP_OAUTH1_TOKEN_VALUE, 
                getEncryptor().encrypt(PublishingModel.PROP_OAUTH1_TOKEN_VALUE, requestToken.getValue()));

        return oauthOperations.buildAuthorizeUrl(requestToken.getValue(), getOAuth1Parameters(callbackUrl));
    }
    
    @Override
    protected AuthStatus internalAcceptAuthorisation(Channel channel, Map<String, String[]> callbackHeaders,
            Map<String, String[]> callbackParams)
    {
        NodeService nodeService = getNodeService();
        AuthStatus authorised = AuthStatus.UNAUTHORISED;
        String[] verifier = callbackParams.get(getOAuthVerifierParamName());
        if (verifier != null)
        {
            OAuth1Operations oauthOperations = getOAuth1Operations();
            NodeRef channelNodeRef = channel.getNodeRef();

            Map<QName, Serializable> currentProps = nodeService.getProperties(channelNodeRef);
            String tokenValue = (String) getEncryptor().decrypt(PublishingModel.PROP_OAUTH1_TOKEN_VALUE, currentProps
                    .get(PublishingModel.PROP_OAUTH1_TOKEN_VALUE));
            String tokenSecret = (String) getEncryptor().decrypt(PublishingModel.PROP_OAUTH1_TOKEN_SECRET, currentProps
                    .get(PublishingModel.PROP_OAUTH1_TOKEN_SECRET));
            OAuthToken token = new OAuthToken(tokenValue, tokenSecret);
            OAuthToken accessToken = oauthOperations.exchangeForAccessToken(new AuthorizedRequestToken(token, verifier[0]), null);
            
            Map<QName, Serializable> newProps = new HashMap<QName, Serializable>();
            newProps.put(PublishingModel.PROP_OAUTH1_TOKEN_VALUE, accessToken.getValue());
            newProps.put(PublishingModel.PROP_OAUTH1_TOKEN_SECRET, accessToken.getSecret());
            newProps = getEncryptor().encrypt(newProps);
            getChannelService().updateChannel(channel, newProps);
            authorised = AuthStatus.AUTHORISED;
        }
        return authorised;
    }
    
    /**
     * Override this method to add additonal parameters onto the URL that the user is redirected to 
     * to authorise access to their account. By default, no parameters are added, but this may be useful to
     * specify things such as the permissions being sought, and so on.
     * @param callbackUrl
     * @return Do not return null. If no parameters are to be added, return {@link OAuth1Parameters#NONE}
     */
    protected OAuth1Parameters getOAuth1Parameters(String callbackUrl)
    {
        return OAuth1Parameters.NONE;
    }
    
    protected String getOAuthVerifierParamName()
    {
        return "oauth_verifier";
    }

    private OAuth1Operations getOAuth1Operations()
    {
        return connectionFactory.getOAuthOperations();
    }

    /**
     * @param connectionFactory the connectionFactory to set
     */
    public void setConnectionFactory(OAuth1ConnectionFactory<A> connectionFactory)
    {
        this.connectionFactory = connectionFactory;
    }
}