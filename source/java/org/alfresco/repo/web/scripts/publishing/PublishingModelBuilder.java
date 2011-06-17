/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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

package org.alfresco.repo.web.scripts.publishing;

import static org.alfresco.util.collections.CollectionUtils.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.publishing.channels.Channel;
import org.alfresco.service.cmr.publishing.channels.ChannelType;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.util.collections.Function;
import org.springframework.extensions.surf.util.URLEncoder;

/**
 * @author Nick Smith
 * @since 4.0
 *
 */
public class PublishingModelBuilder
{
    // General Keys
    public static final String ID = "id";
    public static final String URL = "url";

    // Channel Type Keys
    public static final String CHANNEL_NODE_TYP = "channelNodeType";
    public static final String CONTENT_ROOT_NODE_TYP = "contentRootNodeType";
    public static final String SUPPORTED_CONTENT_TYPES = "supportedContentTypes";
    public static final String SUPPORTED_MIME_TYPES = "supportedMimeTypes";
    public static final String CAN_PUBLISH = "canPublish";
    public static final String CAN_PUBLISH_STATUS_UPDATES = "canPublishStatusUpdates";
    public static final String CAN_UNPUBLISH = "canUnpublish";

    // Channel Keys
    public static final String CHANNEL_TYPE = "channelType";
    public static final String NAME = "name";
    
    public Map<String, Object> buildChannel(Channel channel)
    {
        Map<String, Object> model = new HashMap<String, Object>();
        
        model.put(NAME, channel.getName());
        model.put(URL, getUrl(channel));
        model.put(CHANNEL_TYPE, buildChannelType(channel.getChannelType()));

        return model;
    }

    public List<Map<String, Object>> buildChannels(List<Channel> channels)
    {
        return transform(channels, new Function<Channel, Map<String, Object>>()
        {
            public Map<String, Object> apply(Channel value)
            {
                return buildChannel(value);
            }
        });
    }

    public Map<String, Object> buildChannelType(ChannelType type)
    {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put(ID, type.getId());
        model.put(URL, getUrl(type));
        
        model.put(CHANNEL_NODE_TYP, type.getChannelNodeType().toString());
        model.put(CONTENT_ROOT_NODE_TYP, type.getContentRootNodeType().toString());
        model.put(SUPPORTED_CONTENT_TYPES, toListOfStrings(type.getSupportedContentTypes()));
        model.put(SUPPORTED_MIME_TYPES, type.getSupportedMimetypes());
        model.put(CAN_PUBLISH, toString(type.canPublish()));
        model.put(CAN_PUBLISH_STATUS_UPDATES, toString(type.canPublishStatusUpdates()));
        model.put(CAN_UNPUBLISH, toString(type.canUnpublish()));

        return model;
    }
    
    public static String getUrl(ChannelType type)
    {
        return "api/publishing/channelTypes/"+URLEncoder.encode(type.getId());
    }

    public static String getUrl(Channel channel)
    {
        NodeRef node = channel.getNodeRef();
        StoreRef storeRef = node.getStoreRef();

        StringBuilder sb = new StringBuilder("api/publishing/channels/");
        sb.append(storeRef.getProtocol()).append("/")
        .append(storeRef.getIdentifier()).append("/")
        .append(node.getId());
        return sb.toString();
    }

    private String toString(boolean b)
    {
        return Boolean.toString(b);
    }

}
