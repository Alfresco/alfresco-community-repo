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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
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

    private static final String CHANNEL_CONTAINER_NAME = "channels";

    public static final String NAME = "channelService";

    private final Map<String, ChannelType> channelTypes = new TreeMap<String, ChannelType>();
    private SiteService siteService;
    private NodeService nodeService;
    private DictionaryService dictionaryService;
    private EnvironmentHelper environmentHelper;
    private ChannelHelper channelHelper;
    
    /**
     * @param siteService
     *            the siteService to set
     */
    public void setSiteService(SiteService siteService)
    {
        this.siteService = siteService;
    }

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
     * @param environmentHelper
     *            the environmentHelper to set
     */
    public void setEnvironmentHelper(EnvironmentHelper environmentHelper)
    {
        this.environmentHelper = environmentHelper;
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
    public Channel createChannel(String siteId, String channelTypeId, String name, Map<QName, Serializable> properties)
    {
        NodeRef channelContainer = getChannelContainer(siteId);
        if(channelContainer==null)
        {
            channelContainer = createChannelContainer(siteId);
        }
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
        NodeRef channelNode = channelHelper.createChannelNode(channelContainer, channelType, name, actualProps);
        Channel channel = channelHelper.buildChannelObject(channelNode, this);

        // Now create the corresponding channel nodes in each of the
        // configured environments
        // FIXME: BJR: 20110506: Should we provide a means for supplying
        // separate properties for each environment?
        Map<String, NodeRef> environments = environmentHelper.getEnvironments(siteId);
        for (NodeRef environment : environments.values())
        {
            channelHelper.addChannelToEnvironment(environment, channel, actualProps);
        }
        return channel;
    }

    /**
     * {@inheritDoc}
     */
    public void deleteChannel(String siteId, String channelName)
    {
        Set<NodeRef> containers = getAllChannelContainers(siteId);
        for (NodeRef channelContainer : containers)
        {
            NodeRef channel = nodeService.getChildByName(channelContainer, ContentModel.ASSOC_CONTAINS, channelName);
            if (channel != null)
            {
                nodeService.deleteNode(channel);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public List<Channel> getChannels(String siteId)
    {
        ParameterCheck.mandatory("siteId", siteId);
        NodeRef channelContainer = getChannelContainer(siteId);
        return channelHelper.getChannels(channelContainer, this);
    }

    /**
    * {@inheritDoc}
    */
    public Channel getChannel(String siteId, String channelName)
    {
        ParameterCheck.mandatory("siteId", siteId);
        ParameterCheck.mandatory("channelName", channelName);

        NodeRef channelContainer = getChannelContainer(siteId);
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
                return channelHelper.buildChannelObject(child, this);
            }
        }
        return null;
    }
    
    /**
    * {@inheritDoc}
    */
    public List<Channel> getRelevantPublishingChannels(NodeRef nodeToPublish)
    {
        SiteInfo siteInfo = siteService.getSite(nodeToPublish);
        if(siteInfo != null)
        {
            final NodeRef containerNode = getChannelContainer(siteInfo.getShortName());
            if(containerNode != null)
            {
                List<ChannelType> types = channelHelper.getReleventChannelTypes(nodeToPublish, channelTypes.values());
                return getChannelsForTypes(containerNode, types);
            }
        }
        return Collections.emptyList();
    }
    
    /**
    * {@inheritDoc}
    */
    public List<Channel> getPublishingChannels(String siteId)
    {
        final NodeRef containerNode = getChannelContainer(siteId);
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
    public List<Channel> getStatusUpdateChannels(String siteId)
    {
        final NodeRef containerNode = getChannelContainer(siteId);
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
            return getStatusUpdateChannels(site.getShortName());
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

    private NodeRef getChannelContainer(final String siteId)
    {
        return siteService.getContainer(siteId, CHANNEL_CONTAINER_NAME);
    }

    private Set<NodeRef> getAllChannelContainers(String siteId)
    {
        Set<NodeRef> containers = new HashSet<NodeRef>();
        Map<String, NodeRef> environments = environmentHelper.getEnvironments(siteId);
        containers.addAll(environments.values());
        NodeRef editorialContainer = getChannelContainer(siteId);
        if(editorialContainer!=null)
        {
            containers.add(editorialContainer);
        }
        return containers;
    }

    private NodeRef createChannelContainer(final String siteId)
    {
        return AuthenticationUtil.runAs(new RunAsWork<NodeRef>()
        {
            public NodeRef doWork() throws Exception
            {
                    return siteService.createContainer(siteId, CHANNEL_CONTAINER_NAME,
                            PublishingModel.TYPE_CHANNEL_CONTAINER, null);
            }
        }, AuthenticationUtil.getSystemUserName());
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
    public void renameChannel(String siteId, String oldName, String newName)
    {
        Set<NodeRef> containers = getAllChannelContainers(siteId);
        for (NodeRef channelContainer : containers)
        {
            NodeRef channel = nodeService.getChildByName(channelContainer, ContentModel.ASSOC_CONTAINS, oldName);
            if (channel != null)
            {
                nodeService.setProperty(channel, ContentModel.PROP_NAME, newName);
                nodeService.moveNode(channel, channelContainer, ContentModel.ASSOC_CONTAINS, QName.createQName(
                        NamespaceService.APP_MODEL_1_0_URI, newName));
            }
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
    public void updateChannel(String siteId, String channelName, Map<QName, Serializable> properties)
    {
        Set<NodeRef> containers = getAllChannelContainers(siteId);
        for (NodeRef channelContainer : containers)
        {
            NodeRef channel = nodeService.getChildByName(channelContainer, ContentModel.ASSOC_CONTAINS, channelName);
            if (channel != null)
            {
                nodeService.setProperties(channel, properties);
            }
        }
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public Channel getChannel(String id)
    {
        if(id!=null)
        {
            NodeRef node = new NodeRef(id);
            return channelHelper.buildChannelObject(node, this);
        }
        return null;
    }

}
