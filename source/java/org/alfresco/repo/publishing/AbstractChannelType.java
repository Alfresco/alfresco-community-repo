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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.node.encryption.MetadataEncryptor;
import org.alfresco.service.cmr.publishing.channels.Channel;
import org.alfresco.service.cmr.publishing.channels.ChannelService;
import org.alfresco.service.cmr.publishing.channels.ChannelType;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * @author Nick Smith
 * @since 4.0
 */
public abstract class AbstractChannelType implements ChannelType, ChannelTypePublishingOperations
{
    private NodeService nodeService;
    private ChannelService channelService;
    private MetadataEncryptor encryptor;

    public void setChannelService(ChannelService channelService)
    {
        this.channelService = channelService;
        channelService.register(this);
    }

    protected ChannelService getChannelService()
    {
        return channelService;
    }

    public void setEncryptor(MetadataEncryptor encryptor)
    {
        this.encryptor = encryptor;
    }

    protected MetadataEncryptor getEncryptor()
    {
        return encryptor;
    }

    protected NodeService getNodeService()
    {
        return nodeService;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    @Override
    public String getTitle()
    {
        String title = I18NUtil.getMessage("publishing.channel-type." + getId() + ".title");
        return title == null ? getId() : title;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMaximumStatusLength()
    {
        return 0;
    }

    @Override
    public String getAuthorisationUrl(Channel channel, String callbackUrl)
    {
        // Returning a null here to indicate that we should use our own
        // credential-gathering mechanism.
        return null;
    }

    @Override
    public final AuthStatus acceptAuthorisationCallback(Channel channel, Map<String, String[]> callbackHeaders,
            Map<String, String[]> callbackParams)
    {
        ParameterCheck.mandatory("channel", channel);
        ParameterCheck.mandatory("callbackHeaders", callbackHeaders);
        ParameterCheck.mandatory("callbackParams", callbackParams);
        if (!getId().equals(channel.getChannelType().getId()))
        {
            throw new IllegalArgumentException("Supplied channel is of the incorrect type. Expected " + getId()
                    + "; Received " + channel.getChannelType().getId());
        }

        AuthStatus result = internalAcceptAuthorisation(channel, callbackHeaders, callbackParams);

        Map<QName, Serializable> props = new HashMap<QName, Serializable>();
        props.put(PublishingModel.PROP_AUTHORISATION_COMPLETE, Boolean.valueOf(AuthStatus.AUTHORISED.equals(result)));
        channelService.updateChannel(channel, props);
        return result;
    }

    protected AuthStatus internalAcceptAuthorisation(Channel channel, Map<String, String[]> callbackHeaders,
            Map<String, String[]> callbackParams)
    {
        AuthStatus result = AuthStatus.UNAUTHORISED;
        String[] username = callbackParams.get("username");
        String[] password = callbackParams.get("password");
        if (username != null && password != null)
        {
            Map<QName, Serializable> props = new HashMap<QName, Serializable>();
            props.put(PublishingModel.PROP_CHANNEL_USERNAME, username[0]);
            props.put(PublishingModel.PROP_CHANNEL_PASSWORD, password[0]);
            channelService.updateChannel(channel, props);
            // TODO: BJR: 20110707: Should test the connection here
            result = AuthStatus.AUTHORISED;
        }
        return result;
    }

    public Resource getIcon(String sizeSuffix)
    {
        String className = this.getClass().getCanonicalName();
        className = className.replaceAll("\\.", "\\/");
        StringBuilder iconPath = new StringBuilder(className);
        iconPath.append(sizeSuffix).append('.').append(getIconFileExtension());
        Resource resource = new ClassPathResource(iconPath.toString());
        return resource.exists() ? resource : null;
    }

    public String getIconFileExtension()
    {
        return "png";
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
    public void sendStatusUpdate(Channel channel, String status)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void publish(NodeRef nodeToPublish, Map<QName, Serializable> channelProperties)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void unpublish(NodeRef nodeToUnpublish, Map<QName, Serializable> channelProperties)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getNodeUrl(NodeRef node)
    {
        String url = null;
        if (node != null && nodeService.exists(node) && nodeService.hasAspect(node, PublishingModel.ASPECT_ASSET))
        {
            url = (String)nodeService.getProperty(node, PublishingModel.PROP_ASSET_URL);
        }
        return url;
    }

}
