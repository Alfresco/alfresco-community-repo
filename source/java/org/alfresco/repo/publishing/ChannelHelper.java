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

import static org.alfresco.model.ContentModel.ASSOC_CONTAINS;
import static org.alfresco.repo.publishing.PublishingModel.ASPECT_CONTENT_ROOT;
import static org.alfresco.repo.publishing.PublishingModel.ASPECT_PUBLISHED;
import static org.alfresco.repo.publishing.PublishingModel.ASSOC_SOURCE;
import static org.alfresco.repo.publishing.PublishingModel.NAMESPACE;
import static org.alfresco.repo.publishing.PublishingModel.PROP_CHANNEL;
import static org.alfresco.repo.publishing.PublishingModel.PROP_CHANNEL_TYPE;
import static org.alfresco.repo.publishing.PublishingModel.PROP_CHANNEL_TYPE_ID;
import static org.alfresco.repo.publishing.PublishingModel.TYPE_DELIVERY_CHANNEL;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.publishing.channels.Channel;
import org.alfresco.service.cmr.publishing.channels.ChannelService;
import org.alfresco.service.cmr.publishing.channels.ChannelType;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.GUID;
import org.alfresco.util.Pair;
import org.alfresco.util.collections.CollectionUtils;
import org.alfresco.util.collections.Filter;
import org.alfresco.util.collections.Function;

/**
 * @author Nick Smith
 * @since 4.0
 *
 */
public class ChannelHelper
{
    private static final QName ROOT_NAME = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "root");

    private NodeService nodeService;
    private DictionaryService dictionaryService;
    private FileFolderService fileFolderService;
    
    public ChannelHelper()
    {
        super();
    }
    
    public ChannelHelper(NodeService nodeService, DictionaryService dictionaryService)
    {
        this.nodeService =nodeService;
        this.dictionaryService = dictionaryService;
    }

    public NodeRef createChannelNode(NodeRef parent, ChannelType channelType, String channelName,
            Map<QName, Serializable> props)
    {
        QName channelQName = getChannelQName(channelName);
        QName channelNodeType = channelType.getChannelNodeType();
        ChildAssociationRef channelAssoc = 
            nodeService.createNode(parent, ASSOC_CONTAINS, channelQName, channelNodeType, props);
        NodeRef channelNode = channelAssoc.getChildRef();
        
        QName rootNodeType = channelType.getContentRootNodeType();
        ChildAssociationRef rootAssoc = nodeService.createNode(channelNode, ASSOC_CONTAINS, ROOT_NAME, rootNodeType);
        nodeService.addAspect(rootAssoc.getChildRef(), ASPECT_CONTENT_ROOT, null);
        return channelNode;
    }

    public Channel buildChannelObject(NodeRef nodeRef, ChannelService channelService)
    {
        if(nodeRef == null || nodeService.exists(nodeRef)==false)
        {
            return null;
        }
        Map<QName, Serializable> props = nodeService.getProperties(nodeRef);
        String channelTypeId = (String) props.get(PublishingModel.PROP_CHANNEL_TYPE_ID);
        ChannelType channelType = channelService.getChannelType(channelTypeId);
        String name = (String) props.get(ContentModel.PROP_NAME);
        return new ChannelImpl(channelType, nodeRef, name, this);
    }

    public NodeRef addChannelToEnvironment(NodeRef environment, Channel channel, Map<QName, Serializable> properties)
    {
        ChannelType channelType = channel.getChannelType();
        String channelName = channel.getName();
        return createChannelNode(environment, channelType, channelName, properties);
    }

    public Channel getChannel(NodeRef environment, String channelName, ChannelService channelService)
    {
        NodeRef channelNode = getChannelNodeForEnvironment(environment, channelName);
        if(channelNode != null)
        {
            return buildChannelObject(channelNode, channelService);
        }
        return null;
    }

    public NodeRef getChannelNodeForEnvironment(NodeRef environment, String channelName)
    {
        QName channelQName = getChannelQName(channelName);
        List<ChildAssociationRef> channelAssocs = nodeService.getChildAssocs(environment, ASSOC_CONTAINS, channelQName);
        return getSingleValue(channelAssocs, true);
    }
    
    public NodeRef getChannelRootNode(NodeRef channel)
    {
        List<ChildAssociationRef> rootAssocs = nodeService.getChildAssocs(channel, ASSOC_CONTAINS, ROOT_NAME);
        NodeRef root = getSingleValue(rootAssocs, true);
        if(root ==null || nodeService.hasAspect(root, ASPECT_CONTENT_ROOT)==false)
        {
            throw new IllegalStateException("All channels must have a root folder!");
        }
        return root;
    }

    /**
     * Given a noderef from the editorial space (e.g. the doclib), this returns the corresponding noderef in the specified channel and environment.
     * @param source
     * @param environment
     * @param channelName
     * @return
     */
    public NodeRef mapSourceToEnvironment(NodeRef source, NodeRef environment, String channelName)
    {
        NodeRef channel = getChannelNodeForEnvironment(environment, channelName);
        return mapSourceToEnvironment(source, channel);
    }
    
    /**
     * Given a noderef from the editorial space (e.g. the doclib), this returns the corresponding noderef in the specified channelt
     * @param source
     * @param channel
     * @return
     */
    public NodeRef mapSourceToEnvironment(NodeRef source, NodeRef channel)
    {
        List<ChildAssociationRef> parentAssocs = nodeService.getParentAssocs(source, ASSOC_SOURCE, RegexQNamePattern.MATCH_ALL);
        if(parentAssocs != null)
        {
            NodeRef root = getChannelRootNode(channel);
            for (ChildAssociationRef parentAssoc : parentAssocs)
            {
                NodeRef publishedNode = parentAssoc.getParentRef();
                NodeRef parent = nodeService.getPrimaryParent(publishedNode).getParentRef();
                if(root.equals(parent))
                {
                    return publishedNode;
                }
            }
        }
        return null;
    }
    
    /**
     * Given a published noderef, this returns the corresponding source noderef in the editorial space (doclib).
     * @param publishedNode
     * @return
     */
    public NodeRef mapEnvironmentToSource(NodeRef publishedNode)
    {
        List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(publishedNode, ASSOC_SOURCE, RegexQNamePattern.MATCH_ALL);
        return getSingleValue(childAssocs, true);
    }
    
    /**
     * Finds the {@link Channel} NodeRef and {@link ChannelType} id for a given node, if such a Channel exists.
     * @param node
     * @return a {@link Pair} containing the Channel {@link NodeRef} and ChannelType Id.
     */
    public Pair<NodeRef, String> findChannelAndType(NodeRef node)
    {
        Pair<NodeRef, String> result = getChannelAndTypeIfChannel(node);
        if(result == null)
        {
            result = getChannelAndType(node);
            if(result == null)
            {
                ChildAssociationRef parentAssoc = nodeService.getPrimaryParent(node);
                if (parentAssoc != null)
                {
                    NodeRef parent = parentAssoc.getParentRef();
                    if (parent != null)
                    {
                        result = findChannelAndType(parent);
                    }
                }
            }
        }
        return result;
    }

    public Map<QName, Serializable> getChannelProperties(NodeRef channel)
    {
        return nodeService.getProperties(channel);
    }
    
    public ChildAssociationRef createMapping(NodeRef source, NodeRef publishedNode)
    {
        QName qName = QName.createQName(NAMESPACE, GUID.generate());
        ChildAssociationRef assoc = nodeService.addChild(publishedNode, source, ASSOC_SOURCE, qName);
        nodeService.addAspect(source, ASPECT_PUBLISHED, null);
        return assoc;
    }

    public boolean canPublish(NodeRef nodeToPublish, ChannelType type)
    {
        if(type.canPublish() == false)
        {
            return false;
        }
        FileInfo file = fileFolderService.getFileInfo(nodeToPublish);
        ContentData contentData = file.getContentData();
        String mimetype = contentData == null ? null : contentData.getMimetype();
        boolean isContentTypeSupported = isContentTypeSupported(file.getType(), type);
        boolean isMimetypeSupported = isMimetypeSupported(mimetype, type);
        return isContentTypeSupported && isMimetypeSupported;
    }

    private boolean isMimetypeSupported(String mimetype, ChannelType type)
    {
        Set<String> supportedMimetypes = type.getSupportedMimetypes();
        if (supportedMimetypes == null || supportedMimetypes.isEmpty())
        {
            return true;
        }
        return supportedMimetypes.contains(mimetype);
    }

    private boolean isContentTypeSupported(QName contentType, ChannelType type)
    {
        Set<QName> supportedContentTypes = type.getSupportedContentTypes();
        if(supportedContentTypes == null || supportedContentTypes.isEmpty())
        {
            return true;
        }
        for (QName supportedType : supportedContentTypes)
        {
            if(contentType.equals(supportedType) 
                    || dictionaryService.isSubClass(contentType, supportedType))
            {
                return true;
            }
        }
        return false;
    }

    private QName getChannelQName(String channelName)
    {
        return QName.createQName(NamespaceService.APP_MODEL_1_0_URI, channelName);
    }

    private Pair<NodeRef, String> getChannelAndTypeIfChannel(NodeRef node)
    {
        QName type = nodeService.getType(node);
        if(dictionaryService.isSubClass(type, TYPE_DELIVERY_CHANNEL))
        {
            String channelTypeId = (String) nodeService.getProperty(node, PROP_CHANNEL_TYPE_ID);
            if(channelTypeId == null)
            {
                TypeDefinition typeDef = dictionaryService.getType(type);
                PropertyDefinition channelTypeProp = typeDef.getProperties().get(PROP_CHANNEL_TYPE_ID);
                if(channelTypeProp !=null)
                {
                    channelTypeId = channelTypeProp.getDefaultValue();
                }
            }
            return new Pair<NodeRef, String>(node, channelTypeId);
        }
        return null;
    }

    public List<Channel> getChannels(NodeRef channelContainer, final ChannelService channelService)
    {
        List<ChildAssociationRef> channelAssocs = getChannelAssocs(channelContainer);
        return CollectionUtils.transform(channelAssocs, getChannelTransformer(channelService));
    }

    public List<Channel> getChannelsByType(NodeRef containerNode, String channelTypeId, ChannelService channelService)
    {
        List<ChildAssociationRef> channelAssocs = getChannelAssocsByType(containerNode, channelTypeId);
        return CollectionUtils.transform(channelAssocs, getChannelTransformer(channelService));
    }
    
    public List<ChannelType> getReleventChannelTypes(final NodeRef nodeToPublish, Collection<ChannelType> channelTypes)
    {
        return CollectionUtils.filter(channelTypes, new Filter<ChannelType>()
        {
            public Boolean apply(ChannelType type)
            {
                return canPublish(nodeToPublish, type);
            }
        });
    }
    
    public List<ChannelType> getStatusUpdateChannelTypes(Collection<ChannelType> channelTypes)
    {
        return CollectionUtils.filter(channelTypes, new Filter<ChannelType>()
        {
            public Boolean apply(ChannelType type)
            {
                return type.canPublishStatusUpdates();
            }
        });
    }

    private List<ChildAssociationRef> getChannelAssocs(NodeRef channelContainer)
    {
        if(channelContainer == null)
        {
            return null;
        }
        Collection<QName> channelNodeTypes = dictionaryService.getSubTypes(TYPE_DELIVERY_CHANNEL, true);
        HashSet<QName> childNodeTypeQNames = new HashSet<QName>(channelNodeTypes);
        return nodeService.getChildAssocs(channelContainer, childNodeTypeQNames);
    }
    
    private List<ChildAssociationRef> getChannelAssocsByType(NodeRef channelContainer, String channelTypeId)
    {
        if(channelContainer == null)
        {
            return null;
        }
        return nodeService.getChildAssocsByPropertyValue(channelContainer, PROP_CHANNEL_TYPE_ID, channelTypeId);
    }
    
    private Pair<NodeRef, String> getChannelAndType(NodeRef node)
    {
        NodeRef channel = (NodeRef) nodeService.getProperty(node, PROP_CHANNEL);
        if(channel != null)
        {
            String channelType = (String) nodeService.getProperty(node, PROP_CHANNEL_TYPE);
            return new Pair<NodeRef, String>(channel, channelType);
        }
        return null;
    }
    
    private NodeRef getSingleValue(List<ChildAssociationRef> assocs, boolean getChild)
    {
        if(assocs != null && assocs.size()==1 )
        {
            ChildAssociationRef association = assocs.get(0);
            return getChild ? association.getChildRef() : association.getParentRef();
        }
        return null;
    }

    private Function<ChildAssociationRef, Channel> getChannelTransformer(final ChannelService channelService)
    {
        return new Function<ChildAssociationRef, Channel>()
        {
            public Channel apply(ChildAssociationRef value)
            {
                NodeRef channelNode = value.getChildRef();
                return buildChannelObject(channelNode, channelService);
            }
        };
    }

    /**
     * @param nodeService the nodeService to set
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    /**
     * @param dictionaryService the dictionaryService to set
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * @param fileFolderService the fileFolderService to set
     */
    public void setFileFolderService(FileFolderService fileFolderService)
    {
        this.fileFolderService = fileFolderService;
    }

}
