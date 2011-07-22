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

import static org.alfresco.repo.publishing.PublishingModel.PROP_CHANNEL_TYPE_ID;
import static org.alfresco.repo.publishing.PublishingModel.TYPE_DELIVERY_CHANNEL;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.publishing.channels.Channel;
import org.alfresco.service.cmr.publishing.channels.ChannelService;
import org.alfresco.service.cmr.publishing.channels.ChannelType;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.transfer.NodeFilter;
import org.alfresco.service.cmr.transfer.NodeFinder;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import org.alfresco.util.collections.CollectionUtils;
import org.alfresco.util.collections.Filter;
import org.alfresco.util.collections.Function;

/**
 * @author Nick Smith
 * @author Brian
 * @since 4.0
 * 
 */
public class ChannelServiceImpl implements ChannelService
{
    public static final String NAME = "channelService";

    private final Map<String, ChannelType> channelTypes = new TreeMap<String, ChannelType>();
    private SiteService siteService;
    private NodeService nodeService;
    private DictionaryService dictionaryService;
    private ChannelHelper channelHelper;
    private PublishingRootObject rootObject;
    
    /**
     * @param nodeService
     *            the nodeService to set
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @param dictionaryService
     *            the dictionaryService to set
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * @param rootObject the rootObject to set
     */
    public void setPublishingRootObject(PublishingRootObject rootObject)
    {
        this.rootObject = rootObject;
    }
    
    /**
     * @param channelHelper the channelHelper to set
     */
    public void setChannelHelper(ChannelHelper channelHelper)
    {
        this.channelHelper = channelHelper;
    }
    
    /**
     * {@inheritDoc}
     */
    public void register(ChannelType channelType)
    {
        ParameterCheck.mandatory("channelType", channelType);
        String id = channelType.getId();
        if (channelTypes.containsKey(id))
        {
            throw new IllegalArgumentException("Channel type " + id + " is already registered!");
        }
        channelTypes.put(id, channelType);
    }

    /**
     * {@inheritDoc}
     */
    public List<ChannelType> getChannelTypes()
    {
        return new ArrayList<ChannelType>(channelTypes.values());
    }

    /**
     * {@inheritDoc}
     */
    public Channel createChannel(String channelTypeId, String name, Map<QName, Serializable> properties)
    {
        NodeRef channelContainer = getChannelContainer();
        ChannelType channelType = channelTypes.get(channelTypeId);
        if(channelType == null)
        {
            String message = "Channel Type: " + channelTypeId + " does not exist!";
            throw new IllegalArgumentException(message);
        }
        HashMap<QName, Serializable> actualProps = new HashMap<QName, Serializable>();
        if (properties != null)
        {
            actualProps.putAll(properties);
        }
        actualProps.put(ContentModel.PROP_NAME, name);
        actualProps.put(PROP_CHANNEL_TYPE_ID, channelType.getId());
        actualProps.put(PublishingModel.PROP_AUTHORISATION_COMPLETE, Boolean.FALSE);
        NodeRef channelNode = channelHelper.createChannelNode(channelContainer, channelType, name, actualProps);
        return channelHelper.buildChannelObject(channelNode, this);
    }

    /**
     * {@inheritDoc}
     */
    public void deleteChannel(Channel channel)
    {
        nodeService.deleteNode(channel.getNodeRef());
    }

    /**
     * {@inheritDoc}
     */
    public List<Channel> getChannels()
    {
        NodeRef channelContainer = getChannelContainer();
        return channelHelper.getChannels(channelContainer, this);
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public Channel getChannelByName(String channelName)
    {
        NodeRef node = getChannelNodeByName(channelName);
        return channelHelper.buildChannelObject(node, this);
    }
    
    private NodeRef getChannelNodeByName(String channelName)
    {
        ParameterCheck.mandatory("channelName", channelName);

        NodeRef channelContainer = getChannelContainer();
        if(channelContainer == null)
        {
            return null;
        }
        
        NodeRef child = nodeService.getChildByName(channelContainer, ContentModel.ASSOC_CONTAINS, channelName);
        if(child!=null)
        {
            QName type = nodeService.getType(child);
            if(dictionaryService.isSubClass(type, TYPE_DELIVERY_CHANNEL))
            {
                return child;
            }
        }
        return null;
    }
    
    /**
    * {@inheritDoc}
    */
    public List<Channel> getRelevantPublishingChannels(NodeRef nodeToPublish)
    {
        NodeRef containerNode = getChannelContainer();
        List<ChannelType> types = channelHelper.getReleventChannelTypes(nodeToPublish, channelTypes.values());
        return getChannelsForTypes(containerNode, types);
    }
    
    /**
    * {@inheritDoc}
    */
    public List<Channel> getPublishingChannels()
    {
        final NodeRef containerNode = getChannelContainer();
        if(containerNode != null)
        {
            List<ChannelType> types = CollectionUtils.filter(channelTypes.values(), new Filter<ChannelType>()
            {
                public Boolean apply(ChannelType type)
                {
                    return type.canPublish();
                }
            });
            return getChannelsForTypes(containerNode, types);
        }
        return Collections.emptyList();
    }
    
    /**
     * {@inheritDoc}
     */
    public List<Channel> getStatusUpdateChannels()
    {
        final NodeRef containerNode = getChannelContainer();
        if (containerNode != null)
        {
            List<ChannelType> types = channelHelper.getStatusUpdateChannelTypes(channelTypes.values());
            return getChannelsForTypes(containerNode, types);
        }
        return Collections.emptyList();
    }    
    
    /**
    * {@inheritDoc}
    */
    public List<Channel> getStatusUpdateChannels(NodeRef nodeToPublish)
    {
        SiteInfo site = siteService.getSite(nodeToPublish);
        if(site!=null)
        {
            return getStatusUpdateChannels();
        }
        return Collections.emptyList();
    }
    
    private List<Channel> getChannelsForTypes(final NodeRef containerNode, List<ChannelType> types)
    {
        return CollectionUtils.transformFlat(types, new Function<ChannelType, List<Channel>>()
        {
            public List<Channel> apply(ChannelType channelType)
            {
                return channelHelper.getChannelsByType(containerNode, channelType.getId(), ChannelServiceImpl.this);
            }
        });
    }

    private NodeRef getChannelContainer()
    {
        return rootObject.getChannelContainer();
    }

    /**
     * {@inheritDoc}
     */
    public ChannelType getChannelType(String id)
    {
        return channelTypes.get(id);
    }

    /**
     * {@inheritDoc}
     */
    public NodeFinder getChannelDependancyNodeFinder()
    {
        return new ChannelDependancyNodeFinder(this);
    }

    /**
     * {@inheritDoc}
     */
    public NodeFilter getChannelDependancyNodeFilter()
    {
        return new ChannelDependancyNodeFilter(this);
    }

    /**
    * {@inheritDoc}
    */
    public void renameChannel(String oldName, String newName)
    {
        NodeRef channelContainer = getChannelContainer();
        NodeRef channel = nodeService.getChildByName(channelContainer, ContentModel.ASSOC_CONTAINS, oldName);
        if (channel != null)
        {
            nodeService.setProperty(channel, ContentModel.PROP_NAME, newName);
            nodeService.moveNode(channel, channelContainer, ContentModel.ASSOC_CONTAINS,
                    QName.createQName(NamespaceService.APP_MODEL_1_0_URI, newName));
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.alfresco.service.cmr.publishing.channels.ChannelService#updateChannel
     * (java.lang.String, java.lang.String, java.util.Map)
     */
    @Override
    public void updateChannel(Channel channel, Map<QName, Serializable> properties)
    {
        HashMap<QName, Serializable> actualProps = new HashMap<QName, Serializable>(properties);
        actualProps.remove(ContentModel.PROP_NODE_UUID);
        NodeRef editorialNode = channel.getNodeRef();
        for (Map.Entry<QName, Serializable> entry : actualProps.entrySet())
        {
            nodeService.setProperty(editorialNode, entry.getKey(), entry.getValue());
        }
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public Channel getChannelById(String id)
    {
        if(id!=null&& id.isEmpty()==false
                && NodeRef.isNodeRef(id))
        {
            NodeRef node = new NodeRef(id);
            return channelHelper.buildChannelObject(node, this);
        }
        return null;
    }

}
