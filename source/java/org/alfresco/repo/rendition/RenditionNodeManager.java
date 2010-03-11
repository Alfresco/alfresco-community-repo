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

package org.alfresco.repo.rendition;

import java.io.Serializable;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.model.RenditionModel;
import org.alfresco.repo.rendition.executer.AbstractRenderingEngine;
import org.alfresco.service.cmr.rendition.RenditionDefinition;
import org.alfresco.service.cmr.rendition.RenditionService;
import org.alfresco.service.cmr.rendition.RenditionServiceException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNamePattern;

/**
 * This class is responsible for placing a rendition node in the correct
 * location given a temporary rendition, a source node, a rendition location and
 * optionally an old rendition. This manages the complex logic of deciding
 * whether to move and old rendition or orphan it and create a new one amongst
 * other things.
 * 
 * @author Nick Smith
 * 
 */
public class RenditionNodeManager
{
    private final NodeRef sourceNode;
    private final NodeRef oldRendition;
    private final RenditionDefinition renditionDefinition;
    private final RenditionLocation location;
    private final NodeService nodeService;

    public RenditionNodeManager(NodeRef sourceNode, NodeRef oldRendition, RenditionLocation location,
                RenditionDefinition renditionDefinition, NodeService nodeService)
    {
        this.sourceNode = sourceNode;
        this.oldRendition = oldRendition;
        this.location = location;
        this.renditionDefinition = renditionDefinition;
        this.nodeService = nodeService;
    }

    public ChildAssociationRef findOrCreateRenditionNode()
    {
        QName renditionName = renditionDefinition.getRenditionName();
        // If no rendition already exists create anew rendition node and
        // association.
        if (oldRendition == null)
        {
            return getSpecifiedRenditionOrCreateNewRendition(renditionName);
        }
        // If a rendition exists and is already in the correct location then
        // return that renditions primary parent association
        if (renditionLocationMatches())
        {
            return nodeService.getPrimaryParent(oldRendition);
        }
        // If the old rendition is in the wrong location and the 'orphan
        // existing rendition' param is set to true or the RenditionLocation
        // specifies a destination NodeRef then ldelete the old
        // rendition association and create a new rendition node.
        if (orphanExistingRendition())
        {
            orphanRendition( renditionName);
            return getSpecifiedRenditionOrCreateNewRendition(renditionName);
        }
        // If the old rendition is in the wrong place and the 'orphan existing
        // rendition' param is not set to true then move the existing rendition
        // to the correct location.
        return moveRendition(renditionName);
    }

    private ChildAssociationRef moveRendition(QName associationName)
    {
        return nodeService.moveNode(oldRendition, location.getParentRef(), ContentModel.ASSOC_CONTAINS, associationName);
    }

    private void orphanRendition(QNamePattern renditionName)
    {
        List<ChildAssociationRef> parents = nodeService.getParentAssocs(oldRendition, RenditionModel.ASSOC_RENDITION, renditionName);
        if(parents.size() ==1)
        {
            ChildAssociationRef parentAssoc = parents.get(0);
            if(parentAssoc.getParentRef().equals(sourceNode))
            {
                nodeService.removeAspect(oldRendition, RenditionModel.ASPECT_HIDDEN_RENDITION);
                nodeService.removeAspect(oldRendition, RenditionModel.ASPECT_VISIBLE_RENDITION);
                nodeService.removeChildAssociation(parentAssoc);
                return;
            }
        }
        String msg = "Node: " + oldRendition 
            + " is not a rendition of type: " + renditionName 
            + " for source node: " + sourceNode;
        throw new RenditionServiceException(msg);
    }

    private boolean orphanExistingRendition()
    {
        if (location.getChildRef() != null)
            return true;
        else
            return AbstractRenderingEngine.getParamWithDefault(RenditionService.PARAM_ORPHAN_EXISTING_RENDITION,
                        Boolean.FALSE, renditionDefinition);
    }

    private boolean renditionLocationMatches()
    {
        NodeRef destination = location.getChildRef();
        if (destination != null)
        {
            return destination.equals(oldRendition);
        }
        ChildAssociationRef oldParentAssoc = nodeService.getPrimaryParent(oldRendition);
        NodeRef oldParent = oldParentAssoc.getParentRef();
        if (oldParent.equals(location.getParentRef()))
        {
            String childName = location.getChildName();
            if (childName == null)
                return true;
            else
            {
                Serializable oldName = nodeService.getProperty(oldRendition, ContentModel.PROP_NAME);
                return childName.equals(oldName);
            }
        }
        return false;
    }

    private ChildAssociationRef getSpecifiedRenditionOrCreateNewRendition(QName renditionName)
    {
        NodeRef destination = location.getChildRef();
        if (destination != null)
            return nodeService.getPrimaryParent(destination);
        else
            return createNewRendition(renditionName);
    }

    private ChildAssociationRef createNewRendition(QName renditionName)
    {
        NodeRef parentRef = location.getParentRef();
        boolean parentIsSource = parentRef.equals(sourceNode);
        QName renditionType = RenditionModel.ASSOC_RENDITION;
        QName assocTypeQName = parentIsSource ? renditionType : ContentModel.ASSOC_CONTAINS;
        QName nodeTypeQName = ContentModel.TYPE_CONTENT;
        ChildAssociationRef primaryAssoc = nodeService.createNode(parentRef, assocTypeQName, renditionName,
                    nodeTypeQName);

        // If the new rendition is not directly under the source node then add
        // the rendition association.
        if (parentIsSource == false)
        {
            NodeRef rendition = primaryAssoc.getChildRef();
            nodeService.addChild(sourceNode, rendition, renditionType, renditionName);
        }
        return primaryAssoc;
    }

}
