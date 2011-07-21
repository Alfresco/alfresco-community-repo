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
import static org.alfresco.repo.publishing.PublishingModel.ASPECT_PUBLISHED;
import static org.alfresco.repo.publishing.PublishingModel.ASSOC_PUBLISHED_CHANNEL;
import static org.alfresco.repo.publishing.PublishingModel.ASSOC_SOURCE;
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
import org.alfresco.repo.node.NodeUtils;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.publishing.channels.Channel;
import org.alfresco.service.cmr.publishing.channels.ChannelService;
import org.alfresco.service.cmr.publishing.channels.ChannelType;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
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
    public static final String NAME = "channelHelper";

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
        return channelNode;
    }

    public Channel buildChannelObject(NodeRef nodeRef, ChannelService channelService)
    {
        if(nodeRef == null || nodeService.exists(nodeRef)==false)
        {
            return null;
        }
        Map<QName, Serializable> props = nodeService.getProperties(nodeRef);
        String channelTypeId = (String) props.get(PROP_CHANNEL_TYPE_ID);
        ChannelType channelType = channelService.getChannelType(channelTypeId);
        String name = (String) props.get(ContentModel.PROP_NAME);
        return new ChannelImpl(channelType, nodeRef, name, this);
    }

    /**
     * Given a noderef from the editorial space (e.g. the doclib), this returns the corresponding noderef published to the specified channel.
     * @param source
     * @param channelNode
     * @return
     */
    public NodeRef mapSourceToEnvironment(NodeRef source, final NodeRef channelNode)
    {
        if(source == null || channelNode == null)
        {
            return null;
        }
        List<AssociationRef> sourceAssocs = nodeService.getSourceAssocs(source, ASSOC_SOURCE);
        Function<? super AssociationRef, Boolean> acceptor = new Filter<AssociationRef>()
        {
            public Boolean apply(AssociationRef assoc)
            {
                NodeRef publishedNode = assoc.getSourceRef();
                List<AssociationRef> channelAssoc = nodeService.getTargetAssocs(publishedNode, ASSOC_PUBLISHED_CHANNEL);
                if(CollectionUtils.isEmpty(channelAssoc))
                {
                    return false;
                }
                NodeRef target = channelAssoc.get(0).getTargetRef();
                return target.equals(channelNode);
            }
        };
        AssociationRef assoc = CollectionUtils.findFirst(sourceAssocs, acceptor);
        return assoc == null ? null : assoc.getSourceRef();
    }
    
    /**
     * Given a published noderef, this returns the corresponding source noderef in the editorial space (doclib).
     * @param publishedNode
     * @return
     */
    public NodeRef mapEnvironmentToSource(NodeRef publishedNode)
    {
        List<AssociationRef> assocs = nodeService.getTargetAssocs(publishedNode, ASSOC_SOURCE);
        return NodeUtils.getSingleAssocNode(assocs, true);
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
    
    public AssociationRef createMapping(NodeRef source, NodeRef publishedNode)
    {
        AssociationRef assoc = nodeService.createAssociation(publishedNode, source, ASSOC_SOURCE);
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
        Set<String> supportedMimetypes = type.getSupportedMimeTypes();
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

    public void addPublishedAspect(NodeRef publishedNode, NodeRef channelNode)
    {
        nodeService.addAspect(publishedNode, ASPECT_PUBLISHED, null);
        List<AssociationRef> channelAssoc = nodeService.getTargetAssocs(publishedNode, ASSOC_PUBLISHED_CHANNEL);
        if(CollectionUtils.isEmpty(channelAssoc))
        {
            nodeService.createAssociation(publishedNode, channelNode, ASSOC_PUBLISHED_CHANNEL);
        }
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
