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

import static org.alfresco.repo.publishing.PublishingModel.ASPECT_PUBLISHED;
import static org.alfresco.repo.publishing.PublishingModel.ASSOC_LAST_PUBLISHING_EVENT;
import static org.alfresco.repo.publishing.PublishingModel.NAMESPACE;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeUtils;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.publishing.NodeSnapshot;
import org.alfresco.service.cmr.publishing.PublishingEvent;
import org.alfresco.service.cmr.publishing.PublishingPackageEntry;
import org.alfresco.service.cmr.publishing.channels.Channel;
import org.alfresco.service.cmr.publishing.channels.ChannelType;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.GUID;
import org.alfresco.util.ParameterCheck;

/**
 * @author Brian
 * @author Nick Smith
 * @since 4.0
 */
public class ChannelImpl implements Channel
{
    private static final String PERMISSIONS_ERR_ACCESS_DENIED = "permissions.err_access_denied";
    private final NodeRef nodeRef;
    private final AbstractChannelType channelType;
    private final String name;
    private final ChannelHelper channelHelper;
    private final NodeService nodeService;
    private final DictionaryService dictionaryService;
    private final PublishingEventHelper eventHelper;
    

    public ChannelImpl(ServiceRegistry serviceRegistry, AbstractChannelType channelType, NodeRef nodeRef, String name,
            ChannelHelper channelHelper, PublishingEventHelper eventHelper)
    {
        this.nodeRef = nodeRef;
        this.channelType = channelType;
        this.name = name;
        this.channelHelper = channelHelper;
        this.nodeService = serviceRegistry.getNodeService();
        this.dictionaryService = serviceRegistry.getDictionaryService();
        this.eventHelper = eventHelper;
    }

    /**
    * {@inheritDoc}
    */
    public String getId()
    {
        return nodeRef.toString();
    }
    
    /**
    * {@inheritDoc}
     */
    public ChannelType getChannelType()
    {
        return channelType;
    }

    /**
     * {@inheritDoc}
    */
    public String getName()
    {
        return name;
    }

    /**
     * {@inheritDoc}
    */
    public NodeRef getNodeRef()
    {
        return nodeRef;
    }

    /**
     * {@inheritDoc}
    */
    public Map<QName, Serializable> getProperties()
    {
        return channelHelper.getChannelProperties(nodeRef);
    }

    public void publishEvent(PublishingEvent event)
    {
         NodeRef eventNode = eventHelper.getPublishingEventNode(event.getId());
         for (PublishingPackageEntry entry : event.getPackage().getEntries())
         {
             if (entry.isPublish())
             {
                 publishEntry(entry, eventNode);
             }
             else
             {
                 unpublishEntry(entry);
             }
         }
     }

    public void unpublishEntry(final PublishingPackageEntry entry)
    {
        final NodeRef channelNode = getNodeRef();
        if (channelHelper.hasPublishPermissions(channelNode))
        {
            AuthenticationUtil.runAsSystem(new RunAsWork<NodeRef>()
            {
                @Override
                public NodeRef doWork() throws Exception
                {
                    NodeRef unpublishedNode = channelHelper.mapSourceToEnvironment(entry.getNodeRef(), channelNode);
                    if (NodeUtils.exists(unpublishedNode, nodeService))
                    {
                        unpublish(unpublishedNode);
                        // Need to set as temporary to delete node instead of archiving.
                        nodeService.addAspect(unpublishedNode, ContentModel.ASPECT_TEMPORARY, null);
                        nodeService.deleteNode(unpublishedNode);
                    }
                    return unpublishedNode;
                }
            });
        }
    }

    public NodeRef publishEntry(final PublishingPackageEntry entry, final NodeRef eventNode)
    {
        NodeRef publishedNode;
        //We decouple the permissions needed to publish from the permissions needed to do what's
        //necessary to actually do the publish. If that makes sense...
        //For example, a user may be able to publish to a channel even if they do not have permission
        //to add an aspect to a published node (which is a necessary part of the publishing process).
        if (channelHelper.hasPublishPermissions(getNodeRef()))
        {
            publishedNode = AuthenticationUtil.runAsSystem(new RunAsWork<NodeRef>()
            {
                @Override
                public NodeRef doWork() throws Exception
                {
                    NodeRef publishedNode = channelHelper.mapSourceToEnvironment(entry.getNodeRef(), getNodeRef());
                    if (publishedNode == null)
                    {
                        publishedNode = publishNewNode(getNodeRef(),  entry.getSnapshot());
                    }
                    else
                    {
                        updatePublishedNode(publishedNode, entry);
                    }
                    eventHelper.linkToLastEvent(publishedNode, eventNode);
                    publish(publishedNode);
                    return publishedNode;
                }
            });
        }
        else
        {
            throw new AccessDeniedException(PERMISSIONS_ERR_ACCESS_DENIED);
        }
        return publishedNode;
    }
    
    /**
     * Creates a new node under the root of the specified channel. The type,
     * aspects and properties of the node are determined by the supplied
     * snapshot.
     * 
     * @param channel
     * @param snapshot
     * @return the newly published node.
     */
    private NodeRef publishNewNode(NodeRef channel, NodeSnapshot snapshot)
    {
        ParameterCheck.mandatory("channel", channel);
        ParameterCheck.mandatory("snapshot", snapshot);
        
        NodeRef publishedNode = createPublishedNode(channel, snapshot);
        addAspects(publishedNode, snapshot.getAspects());
        NodeRef source = snapshot.getNodeRef();
        channelHelper.createMapping(source, publishedNode);
        return publishedNode;
    }

    private void updatePublishedNode(NodeRef publishedNode, PublishingPackageEntry entry)
    {
       NodeSnapshot snapshot = entry.getSnapshot();
       Set<QName> newAspects = snapshot.getAspects();
       removeUnwantedAspects(publishedNode, newAspects);

       Map<QName, Serializable> snapshotProps = snapshot.getProperties();
       removeUnwantedProperties(publishedNode, snapshotProps);
       
       // Add new properties
       Map<QName, Serializable> newProps= new HashMap<QName, Serializable>(snapshotProps);
       newProps.remove(ContentModel.PROP_NODE_UUID);
       nodeService.setProperties(publishedNode, snapshotProps);
       
       // Add new aspects
       addAspects(publishedNode, newAspects);
       
       List<ChildAssociationRef> assocs = nodeService.getChildAssocs(publishedNode, ASSOC_LAST_PUBLISHING_EVENT, RegexQNamePattern.MATCH_ALL);
       for (ChildAssociationRef assoc : assocs)
       {
           nodeService.removeChildAssociation(assoc);
       }
    }

    /**
     * @param publishedNode
     * @param snapshotProps
     */
    private void removeUnwantedProperties(NodeRef publishedNode, Map<QName, Serializable> snapshotProps)
    {
        Map<QName, Serializable> publishProps = nodeService.getProperties(publishedNode);
        Set<QName> propsToRemove = new HashSet<QName>(publishProps.keySet());
        propsToRemove.removeAll(snapshotProps.keySet());

        //We want to retain the published asset id and URL in the updated node...
        snapshotProps.put(PublishingModel.PROP_ASSET_ID, nodeService.getProperty(publishedNode, 
                PublishingModel.PROP_ASSET_ID));
        snapshotProps.put(PublishingModel.PROP_ASSET_URL, nodeService.getProperty(publishedNode, 
                PublishingModel.PROP_ASSET_URL));
        
        for (QName propertyToRemove : propsToRemove)
        {
            nodeService.removeProperty(publishedNode, propertyToRemove);
        }
    }

    /**
     * @param publishedNode
     * @param newAspects
     */
    private void removeUnwantedAspects(NodeRef publishedNode, Set<QName> newAspects)
    {
        Set<QName> aspectsToRemove = nodeService.getAspects(publishedNode);
        aspectsToRemove.removeAll(newAspects);
        aspectsToRemove.remove(ASPECT_PUBLISHED);
        aspectsToRemove.remove(PublishingModel.ASPECT_ASSET);
        for (QName publishedAssetAspect : dictionaryService.getSubAspects(PublishingModel.ASPECT_ASSET, true))
        {
            aspectsToRemove.remove(publishedAssetAspect);
        }

        for (QName aspectToRemove : aspectsToRemove)
        {
            nodeService.removeAspect(publishedNode, aspectToRemove);
        }
    }

    private void addAspects(NodeRef publishedNode, Collection<QName> aspects)
    {
        Set<QName> currentAspects = nodeService.getAspects(publishedNode);
        for (QName aspect : aspects)
        {
            if (currentAspects.contains(aspect) == false)
            {
                nodeService.addAspect(publishedNode, aspect, null);
            }
        }
    }

    private NodeRef createPublishedNode(NodeRef root, NodeSnapshot snapshot)
    {
        QName type = snapshot.getType();
        Map<QName, Serializable> actualProps = getPropertiesToPublish(snapshot);
        String name = (String) actualProps.get(ContentModel.PROP_NAME);
        if (name == null)
        {
            name = GUID.generate();
        }
        QName assocName = QName.createQName(NAMESPACE, name);
        ChildAssociationRef publishedAssoc = nodeService.createNode(root, PublishingModel.ASSOC_PUBLISHED_NODES, assocName, type, actualProps);
        NodeRef publishedNode = publishedAssoc.getChildRef();
       return publishedNode;
    }

    private Map<QName, Serializable> getPropertiesToPublish(NodeSnapshot snapshot)
    {
        Map<QName, Serializable> properties = snapshot.getProperties();
        // Remove the Node Ref Id
        Map<QName, Serializable> actualProps = new HashMap<QName, Serializable>(properties);
        actualProps.remove(ContentModel.PROP_NODE_UUID);
        return actualProps;
    }

    
    private void publish(NodeRef nodeToPublish)
    {
        if (channelHelper.canPublish(nodeToPublish, channelType))
        {
            channelHelper.addPublishedAspect(nodeToPublish, nodeRef);
            channelType.publish(nodeToPublish, getProperties());
        }
    }

    private void unpublish(NodeRef nodeToUnpublish)
    {
        if (channelType.canUnpublish())
        {
            channelType.unpublish(nodeToUnpublish, getProperties());
        }
    }

    /**
    * {@inheritDoc}
    */
    public void sendStatusUpdate(String status, String nodeUrl)
    {
        if (channelType.canPublishStatusUpdates())
        {
            int urlLength = nodeUrl == null ? 0 : nodeUrl.length();
            int maxLength = channelType.getMaximumStatusLength() - urlLength;
            if (maxLength > 0)
            {
                int endpoint = Math.min(maxLength, status.length());
                status = status.substring(0, endpoint );
            }
            String msg = nodeUrl == null ? status : status + nodeUrl;
            channelType.sendStatusUpdate(this, msg);
        }
    }

    /**
    * {@inheritDoc}
    */
    public String getUrl(NodeRef publishedNode)
    {
        NodeRef mappedNode = channelHelper.mapSourceToEnvironment(publishedNode, nodeRef);
        return channelType.getNodeUrl(mappedNode);
    }

    /**
    * {@inheritDoc}
     */
    public boolean isAuthorised()
    {
        return channelHelper.isChannelAuthorised(nodeRef);
    }

    /**
    * {@inheritDoc}
    */
    public boolean canPublish()
    {
        return channelType.canPublish() &&
            isAuthorised() &&
            channelHelper.hasPublishPermissions(nodeRef);
    }

    /**
    * {@inheritDoc}
    */
    public boolean canUnpublish()
    {
        return channelType.canPublish() &&
            isAuthorised() &&
            channelHelper.hasPublishPermissions(nodeRef);
    }

    /**
    * {@inheritDoc}
    */
    public boolean canPublishStatusUpdates()
    {
        return channelType.canPublish() &&
            isAuthorised() &&
            channelHelper.hasPublishPermissions(nodeRef);
    }
}
