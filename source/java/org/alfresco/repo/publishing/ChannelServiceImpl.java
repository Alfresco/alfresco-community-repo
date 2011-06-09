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
import java.util.ArrayList;
import java.util.Collection;
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
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.transfer.NodeFilter;
import org.alfresco.service.cmr.transfer.NodeFinder;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;

/**
 * @author Nick Smith
 * @author Brian
 * @since 4.0
 * 
 */
public class ChannelServiceImpl implements ChannelService
{
    private static final String CHANNEL_CONTAINER_NAME = "channels";

    private final Map<String, ChannelType> channelTypes = new TreeMap<String, ChannelType>();
    private SiteService siteService;
    private NodeService nodeService;
    private DictionaryService dictionaryService;
    private EnvironmentHelper environmentHelper;

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
        ChannelType channelType = channelTypes.get(channelTypeId);
        Channel channel = null;
        if (channelType != null)
        {
            QName channelNodeType = channelType.getChannelNodeType();
            HashMap<QName, Serializable> actualProps = new HashMap<QName, Serializable>();
            if (properties != null)
            {
                actualProps.putAll(properties);
            }
            actualProps.put(ContentModel.PROP_NAME, name);
            actualProps.put(PublishingModel.PROP_CHANNEL_TYPE_ID, channelType.getId());
            QName channelQName = QName.createQName(NamespaceService.APP_MODEL_1_0_URI, name);
            ChildAssociationRef assoc = nodeService.createNode(channelContainer, ContentModel.ASSOC_CONTAINS,
                    channelQName, channelNodeType, actualProps);
            channel = buildChannelObject(assoc.getChildRef());
            QName rootNodeType = channelType.getContentRootNodeType();
            nodeService.createNode(assoc.getChildRef(), ContentModel.ASSOC_CONTAINS, QName.createQName(
                    NamespaceService.CONTENT_MODEL_1_0_URI, "root"), rootNodeType);

            // Now create the corresponding channel nodes in each of the
            // configured environments
            // FIXME: BJR: 20110506: Should we provide a means for supplying
            // separate properties for each environment?
            Map<String, NodeRef> environments = environmentHelper.getEnvironments(siteId);
            for (NodeRef environment : environments.values())
            {
                nodeService.createNode(environment, ContentModel.ASSOC_CONTAINS, channelQName, channelNodeType,
                        actualProps);
            }
        }
        return channel;
    }

    /**
     * {@inheritDoc}
     */
    public void deleteChannel(String siteId, String channelName)
    {
        Map<String, NodeRef> environments = environmentHelper.getEnvironments(siteId);
        Set<NodeRef> containers = new HashSet<NodeRef>();
        containers.add(getChannelContainer(siteId));
        containers.addAll(environments.values());
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
        Collection<QName> channelNodeTypes = dictionaryService.getSubTypes(PublishingModel.TYPE_DELIVERY_CHANNEL, true);
        List<ChildAssociationRef> channelAssocs = nodeService.getChildAssocs(channelContainer, new HashSet<QName>(
                channelNodeTypes));
        List<Channel> channelList = new ArrayList<Channel>(channelAssocs.size());
        for (ChildAssociationRef channelAssoc : channelAssocs)
        {
            channelList.add(buildChannelObject(channelAssoc.getChildRef()));
        }
        return Collections.unmodifiableList(channelList);
    }

    /**
     * @param siteId
     * @return
     */
    private NodeRef getChannelContainer(final String siteId)
    {
        return AuthenticationUtil.runAs(new RunAsWork<NodeRef>()
        {
            public NodeRef doWork() throws Exception
            {
                NodeRef channelContainer = siteService.getContainer(siteId, CHANNEL_CONTAINER_NAME);
                if (channelContainer == null)
                {
                    // No channel container exists for this site yet. Create it.
                    channelContainer = siteService.createContainer(siteId, CHANNEL_CONTAINER_NAME,
                            PublishingModel.TYPE_CHANNEL_CONTAINER, null);
                }
                return channelContainer;
            }
        }, AuthenticationUtil.getSystemUserName());

    }

    /**
     * @param childRef
     * @return
     */
    private Channel buildChannelObject(NodeRef nodeRef)
    {
        Map<QName, Serializable> props = nodeService.getProperties(nodeRef);
        Serializable channelTypeId = props.get(PublishingModel.PROP_CHANNEL_TYPE_ID);
        ChannelType channelType = channelTypes.get(channelTypeId);
        String name = (String) props.get(ContentModel.PROP_NAME);
        return new ChannelImpl(channelType, nodeRef, name, this, nodeService);
    }

    /**
     * {@inheritDoc}
     */
    public ChannelType getChannelType(String id)
    {
        return channelTypes.get(id);
    }

    public NodeFinder getChannelDependancyNodeFinder()
    {
        return new ChannelDependancyNodeFinder(this);
    }

    public NodeFilter getChannelDependancyNodeFilter()
    {
        return new ChannelDependancyNodeFilter(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.alfresco.service.cmr.publishing.channels.ChannelService#renameChannel
     * (java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void renameChannel(String siteId, String oldName, String newName)
    {
        Map<String, NodeRef> environments = environmentHelper.getEnvironments(siteId);
        Set<NodeRef> containers = new HashSet<NodeRef>();
        containers.add(getChannelContainer(siteId));
        containers.addAll(environments.values());
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
        Map<String, NodeRef> environments = environmentHelper.getEnvironments(siteId);
        Set<NodeRef> containers = new HashSet<NodeRef>();
        containers.add(getChannelContainer(siteId));
        containers.addAll(environments.values());
        for (NodeRef channelContainer : containers)
        {
            NodeRef channel = nodeService.getChildByName(channelContainer, ContentModel.ASSOC_CONTAINS, channelName);
            if (channel != null)
            {
                nodeService.setProperties(channel, properties);
            }
        }
    }

}
