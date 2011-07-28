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
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.publishing.AbstractOAuth1ChannelType;
import org.alfresco.repo.publishing.PublishingModel;
import org.alfresco.repo.publishing.flickr.springsocial.api.Flickr;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.publishing.channels.Channel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.collections.CollectionUtils;
import org.springframework.social.oauth1.OAuth1Parameters;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class FlickrChannelType extends AbstractOAuth1ChannelType<Flickr>
{
    public final static String ID = "flickr";
    private final static Set<String> DEFAULT_SUPPORTED_MIME_TYPES = CollectionUtils.unmodifiableSet(
            MimetypeMap.MIMETYPE_IMAGE_GIF,
            MimetypeMap.MIMETYPE_IMAGE_JPEG,
            MimetypeMap.MIMETYPE_IMAGE_PNG);
    
    private ActionService actionService;
    private Set<String> supportedMimeTypes = DEFAULT_SUPPORTED_MIME_TYPES;
    
    public void setActionService(ActionService actionService)
    {
        this.actionService = actionService;
    }
    
    public void setSupportedMimeTypes(Set<String> mimeTypes)
    {
        supportedMimeTypes = Collections.unmodifiableSet(new TreeSet<String>(mimeTypes));
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
    public Set<String> getSupportedMimeTypes()
    {
        return supportedMimeTypes;
    }

    @Override
    public void publish(NodeRef nodeToPublish, Map<QName, Serializable> properties)
    {
        // TODO Nick S: Not sure it is very useful to use an Action hee.
        // The Action assumes the nodeToPublish is under a properly configured DeliveryChannel.
        // Ie. the action assumes the node was generated via the Publishing Service.
        // The Action only really has value if it can be called independant of the Publishing Service IMO.
        Action publishAction = actionService.createAction(FlickrPublishAction.NAME);
        actionService.executeAction(publishAction, nodeToPublish);
    }

    @Override
    public void unpublish(NodeRef nodeToUnpublish, Map<QName, Serializable> properties)
    {
        //NOOP
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
        NodeService nodeService = getNodeService();
        if (node != null && nodeService.exists(node) && nodeService.hasAspect(node, FlickrPublishingModel.ASPECT_ASSET))
        {
            url = (String)nodeService.getProperty(node, PublishingModel.PROP_ASSET_URL);
        }
        return url;
    }

    @Override
    protected OAuth1Parameters getOAuth1Parameters(String callbackUrl)
    {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        params.add("perms", "delete");
        return new OAuth1Parameters(callbackUrl, params);
    }

}
