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

import static org.alfresco.repo.web.scripts.WebScriptUtil.buildCalendarModel;
import static org.alfresco.repo.web.scripts.WebScriptUtil.buildDateModel;
import static org.alfresco.repo.web.scripts.publishing.PublishingWebScriptConstants.CAN_PUBLISH;
import static org.alfresco.repo.web.scripts.publishing.PublishingWebScriptConstants.CAN_PUBLISH_STATUS_UPDATES;
import static org.alfresco.repo.web.scripts.publishing.PublishingWebScriptConstants.CAN_UNPUBLISH;
import static org.alfresco.repo.web.scripts.publishing.PublishingWebScriptConstants.CHANNEL;
import static org.alfresco.repo.web.scripts.publishing.PublishingWebScriptConstants.CHANNEL_NODE_TYPE;
import static org.alfresco.repo.web.scripts.publishing.PublishingWebScriptConstants.CHANNEL_TYPE;
import static org.alfresco.repo.web.scripts.publishing.PublishingWebScriptConstants.COMMENT;
import static org.alfresco.repo.web.scripts.publishing.PublishingWebScriptConstants.CREATED_TIME;
import static org.alfresco.repo.web.scripts.publishing.PublishingWebScriptConstants.CREATOR;
import static org.alfresco.repo.web.scripts.publishing.PublishingWebScriptConstants.ICON;
import static org.alfresco.repo.web.scripts.publishing.PublishingWebScriptConstants.ID;
import static org.alfresco.repo.web.scripts.publishing.PublishingWebScriptConstants.MAX_STATUS_LENGTH;
import static org.alfresco.repo.web.scripts.publishing.PublishingWebScriptConstants.NAME;
import static org.alfresco.repo.web.scripts.publishing.PublishingWebScriptConstants.NODEREF;
import static org.alfresco.repo.web.scripts.publishing.PublishingWebScriptConstants.PUBLISH_NODES;
import static org.alfresco.repo.web.scripts.publishing.PublishingWebScriptConstants.SCHEDULED_TIME;
import static org.alfresco.repo.web.scripts.publishing.PublishingWebScriptConstants.STATUS;
import static org.alfresco.repo.web.scripts.publishing.PublishingWebScriptConstants.SUPPORTED_CONTENT_TYPES;
import static org.alfresco.repo.web.scripts.publishing.PublishingWebScriptConstants.SUPPORTED_MIME_TYPES;
import static org.alfresco.repo.web.scripts.publishing.PublishingWebScriptConstants.TITLE;
import static org.alfresco.repo.web.scripts.publishing.PublishingWebScriptConstants.UNPUBLISH_NODES;
import static org.alfresco.repo.web.scripts.publishing.PublishingWebScriptConstants.URL;
import static org.alfresco.repo.web.scripts.publishing.PublishingWebScriptConstants.VERSION;
import static org.alfresco.util.collections.CollectionUtils.toListOfStrings;
import static org.alfresco.util.collections.CollectionUtils.transform;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.publishing.NodeSnapshot;
import org.alfresco.service.cmr.publishing.PublishingEvent;
import org.alfresco.service.cmr.publishing.PublishingPackage;
import org.alfresco.service.cmr.publishing.PublishingPackageEntry;
import org.alfresco.service.cmr.publishing.channels.Channel;
import org.alfresco.service.cmr.publishing.channels.ChannelService;
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
    
    public Map<String, Object> buildPublishingEvent(PublishingEvent event, ChannelService channelService)
    {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put(ID, event.getId());
        model.put(URL, getUrl(event));
        model.put(STATUS, event.getStatus().name());
        model.put(COMMENT, event.getComment());
        model.put(SCHEDULED_TIME, buildCalendarModel(event.getScheduledTime()));
        model.put(CREATOR, event.getCreator());
        model.put(CREATED_TIME, buildDateModel(event.getCreatedTime()));
        
        model.put(PUBLISH_NODES, buildNodes(event.getPackage(), true));
        model.put(UNPUBLISH_NODES, buildNodes(event.getPackage(), false));
        
        String channelId = event.getChannelId();
        Channel channel = channelService.getChannel(channelId);
        if(channel!= null)
        {
            model.put(CHANNEL, buildChannel(channel));
        }
        else
        {
            model.put(CHANNEL_TYPE, channelId);
        }
        return model;
    }    

    public List<Map<String, Object>> buildPublishingEvents(List<PublishingEvent> events,
            final ChannelService channelService)
    {
        return transform(events, new Function<PublishingEvent, Map<String, Object>>()
        {
            public Map<String, Object> apply(PublishingEvent event)
            {
                return buildPublishingEvent(event, channelService);
            }
        });
    }

    public Map<String, Object> buildChannel(Channel channel)
    {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put(URL, getUrl(channel));
        model.put(ID, channel.getId());
        model.put(NAME, channel.getName());
        //TODO Localize the title.
        model.put(TITLE, channel.getName());
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
        //TODO Localize the title.
        model.put(TITLE, type.getId());
        model.put(URL, getUrl(type));
        
        model.put(CHANNEL_NODE_TYPE, type.getChannelNodeType().toString());
        model.put(SUPPORTED_CONTENT_TYPES, toListOfStrings(type.getSupportedContentTypes()));
        model.put(SUPPORTED_MIME_TYPES, type.getSupportedMimetypes());

        model.put(CAN_PUBLISH, toString(type.canPublish()));
        model.put(CAN_PUBLISH_STATUS_UPDATES, toString(type.canPublishStatusUpdates()));
        model.put(CAN_UNPUBLISH, toString(type.canUnpublish()));

        model.put(MAX_STATUS_LENGTH, type.getMaximumStatusLength());
        model.put(ICON, "");
        return model;
    }
    
    public List<Map<String, Object>> buildChannelTypes(List<ChannelType> types)
    {
        return transform(types, new Function<ChannelType, Map<String, Object>>()
        {
            public Map<String, Object> apply(ChannelType value)
            {
                return buildChannelType(value);
            }
        });
    }

    public static String getUrl(PublishingEvent event)
    {
        return "api/publishing/events/"+URLEncoder.encode(event.getId());
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


    private List<Map<String, Object>> buildNodes(PublishingPackage pckg, boolean isPublish)
    {
        Collection<NodeRef> nodes = isPublish ? pckg.getNodesToPublish() : pckg.getNodesToUnpublish();
        return buildNodes(pckg, nodes);
    }

    private List<Map<String, Object>> buildNodes(PublishingPackage pckg, Collection<NodeRef> nodes)
    {
        List<Map<String, Object>> results = new ArrayList<Map<String,Object>>(nodes.size());
        Map<NodeRef, PublishingPackageEntry> entryMap = pckg.getEntryMap();
        for (NodeRef node : nodes)
        {
            PublishingPackageEntry entry = entryMap.get(node);
            results.add(buildPackageEntry(entry));
        }
        return results;
    }

    private Map<String, Object> buildPackageEntry(PublishingPackageEntry entry)
    {
        Map<String, Object> model = new HashMap<String, Object>();
        NodeSnapshot snapshot = entry.getSnapshot();
        model.put(NODEREF, snapshot.getNodeRef().toString());
        String version = snapshot.getVersion();
        if(version!=null && version.isEmpty()==false)
        {
            model.put(VERSION, version);
        }
        String name = (String) snapshot.getProperties().get(ContentModel.PROP_NAME);
        if(name != null && name.isEmpty() == false)
        {
            model.put(NAME, name);
        }
        return model;
    }

}
