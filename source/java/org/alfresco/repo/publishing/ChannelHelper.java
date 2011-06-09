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

import static org.alfresco.repo.publishing.PublishingModel.*;

import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.publishing.channels.Channel;
import org.alfresco.service.cmr.publishing.channels.ChannelType;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;

/**
 * @author Nick Smith
 * @since 4.0
 *
 */
public class ChannelHelper
{

    /**
     * Finds the {@link Channel} NodeRef and {@link ChannelType} id for a given node, if such a Channel exists.
     * @param node
     * @param nodeService
     * @param dictionaryService
     * @return a {@link Pair} containing the Channel {@link NodeRef} and ChannelType Id.
     */
    public static Pair<NodeRef, String> findChannelAndType(NodeRef node, NodeService nodeService, DictionaryService dictionaryService)
    {
        Pair<NodeRef, String> result = getChannelAndTypeIfChannel(node, nodeService, dictionaryService);
        if(result == null)
        {
            result = getChannelAndType(node, nodeService);
            if(result == null)
            {
                ChildAssociationRef parentAssoc = nodeService.getPrimaryParent(node);
                if (parentAssoc != null)
                {
                    NodeRef parent = parentAssoc.getParentRef();
                    if (parent != null)
                    {
                        result = findChannelAndType(parent, nodeService, dictionaryService);
                    }
                }
            }
        }
        return result;
    }

    private static Pair<NodeRef, String> getChannelAndTypeIfChannel(NodeRef node, NodeService nodeService,
            DictionaryService dictionaryService)
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
    
    private static Pair<NodeRef, String> getChannelAndType(NodeRef node, NodeService nodeService)
    {
        NodeRef channel = (NodeRef) nodeService.getProperty(node, PROP_CHANNEL);
        if(channel != null)
        {
            String channelType = (String) nodeService.getProperty(node, PROP_CHANNEL_TYPE);
            return new Pair<NodeRef, String>(channel, channelType);
        }
        return null;
    }
}
