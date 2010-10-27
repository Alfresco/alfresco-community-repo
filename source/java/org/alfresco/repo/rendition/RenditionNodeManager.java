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

import static org.alfresco.model.ContentModel.PROP_NODE_REF;
import static org.alfresco.model.ContentModel.PROP_STORE_NAME;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.alfresco.model.ContentModel;
import org.alfresco.model.RenditionModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.rendition.executer.AbstractRenderingEngine;
import org.alfresco.service.cmr.rendition.RenditionDefinition;
import org.alfresco.service.cmr.rendition.RenditionService;
import org.alfresco.service.cmr.rendition.RenditionServiceException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class is responsible for placing a rendition node in the correct
 * location given a temporary rendition, a source node, a rendition location and
 * optionally an old rendition. This manages the complex logic of deciding
 * whether to move an old rendition or orphan it and create a new one amongst
 * other things.
 * 
 * @author Nick Smith
 */
public class RenditionNodeManager
{
    /** Logger */
    private static Log logger = LogFactory.getLog(RenditionNodeManager.class);

    private static final List<QName> unchangedProperties = Arrays.asList(PROP_NODE_REF, PROP_STORE_NAME);
    private static final String LINE_BREAK = System.getProperty("line.separator", "\n");
    

    /**
     * The source node being rendered.
     */
    private final NodeRef sourceNode;
    private final NodeRef tempRenditionNode;
    private final RenditionDefinition renditionDefinition;
    private final RenditionLocation location;
    private final NodeService nodeService;
    private BehaviourFilter behaviourFilter;
    private final RenditionService renditionService;
    private final NodeRef oldRendition;
    private ChildAssociationRef finalRenditionAssoc;
    
    /**
     * 
     * @param sourceNode the source node which is being rendered.
     * @param tempRenditionNode the temporary rendition
     * @param location the proposed location of the rendition node.
     * @param renditionDefinition
     * @param nodeService
     * @param renditionService
     */
    public RenditionNodeManager(NodeRef sourceNode, NodeRef tempRenditionNode, RenditionLocation location,
                RenditionDefinition renditionDefinition, NodeService nodeService, RenditionService renditionService,
                BehaviourFilter behaviourFilter)
    {
        this.sourceNode = sourceNode;
        this.tempRenditionNode = tempRenditionNode;
        this.location = location;
        this.renditionDefinition = renditionDefinition;
        this.nodeService = nodeService;
        this.renditionService = renditionService;
        this.behaviourFilter = behaviourFilter;
        
        this.oldRendition = this.getOldRenditionIfExists(sourceNode, renditionDefinition);

        if (logger.isDebugEnabled())
        {
            StringBuilder msg = new StringBuilder();
            msg.append("Creating/updating rendition based on:").append(LINE_BREAK).append("    sourceNode: ").append(
                        sourceNode).append(LINE_BREAK).append("    tempRendition: ").append(tempRenditionNode).append(
                        LINE_BREAK).append("    parentNode: ").append(location.getParentRef()).append(LINE_BREAK).append(
                        "    childName: ").append(location.getChildName()).append(LINE_BREAK).append(
                        "    renditionDefinition.name: ").append(renditionDefinition.getRenditionName());
            logger.debug(msg.toString());
        }
    }

    /**
     * This method returns the {@link ChildAssociationRef} for the rendition node. In doing this
     * it may reuse an existing rendition node, move an existing rendition node or create a new rendition node
     * as appropriate.
     */
    public ChildAssociationRef findOrCreateRenditionNode()
    {
        QName renditionName = renditionDefinition.getRenditionName();
        
        // If no rendition already exists create a new rendition node and
        // association.
        if (oldRendition == null)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("No old rendition was found.");
            }
            
            finalRenditionAssoc = getSpecifiedRenditionOrCreateNewRendition(renditionName);
        }
        else
        {
            // If a rendition exists and is already in the correct location then
            // return that rendition's primary parent association
            if (isOldRenditionInCorrectLocation())
            {
                finalRenditionAssoc = nodeService.getPrimaryParent(oldRendition);
            }
            else
            {
                // If the old rendition is in the wrong location and the 'orphan
                // existing rendition' param is set to true or the RenditionLocation
                // specifies a destination NodeRef then delete the old
                // rendition association and create a new rendition node.
                if (isOrphaningRequired())
                {
                    orphanOldRendition(renditionName);
                    finalRenditionAssoc = getSpecifiedRenditionOrCreateNewRendition(renditionName);
                }
                
                // If the old rendition is in the wrong place and the 'orphan existing
                // rendition' param is not set to true then move the existing rendition
                // to the correct location.
                finalRenditionAssoc = moveOldRendition(renditionName);
            }
        }
        
        return finalRenditionAssoc;
    }

    /**
     * This method moves the old rendition to the required location giving it the correct parent-assoc type and
     * the specified association name.
     * 
     * @param associationName the name to put on the newly created association.
     * @return the ChildAssociationRef of the moved nodeRef.
     */
    private ChildAssociationRef moveOldRendition(QName associationName)
    {
        NodeRef parent = location.getParentRef();
        QName assocType = sourceNode.equals(parent) ? RenditionModel.ASSOC_RENDITION : ContentModel.ASSOC_CONTAINS;
        ChildAssociationRef result = nodeService.moveNode(oldRendition, parent, assocType, associationName);
        
        if (logger.isDebugEnabled())
        {
            logger.debug("The old rendition was moved to " + result);
        }
        
        return result;
    }

    /**
     * This method performs the 'orphaning' of the oldRendition. It removes the rendition aspect(s) and removes
     * the child-association linking the old rendition to its source node. The old rendition node is not deleted.
     * 
     * @param renditionName the name of the rendition.
     * @throws RenditionServiceException if there was not exactly one parent assoc from the oldRendition having the specified renditionName
     *                                   or if the matching parent assoc was not to the correct source node.
     */
    private void orphanOldRendition(QNamePattern renditionName)
    {
        // Get all parent assocs from the old rendition of the specified renditionName.
        List<ChildAssociationRef> parents = nodeService.getParentAssocs(oldRendition, RenditionModel.ASSOC_RENDITION, renditionName);
        // There should only be one matching assoc.
        if(parents.size() == 1)
        {
            ChildAssociationRef parentAssoc = parents.get(0);
            if(parentAssoc.getParentRef().equals(sourceNode))
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Orphaning old rendition node " + oldRendition);
                }
                nodeService.removeAspect(oldRendition, RenditionModel.ASPECT_HIDDEN_RENDITION);
                nodeService.removeAspect(oldRendition, RenditionModel.ASPECT_VISIBLE_RENDITION);
                nodeService.removeChildAssociation(parentAssoc);
                return;
            }
        }
        String msg = "Node: " + oldRendition 
            + " is not a rendition of type: " + renditionName 
            + " for source node: " + sourceNode;
        if (logger.isDebugEnabled())
        {
            logger.debug(msg);
        }
        throw new RenditionServiceException(msg);
    }

    /**
     * This method determines whether or not orphaning of the old rendition is required.
     * 
     * @return <code>true</code> if orphaning is required, else <code>false</code>.
     */
    private boolean isOrphaningRequired()
    {
        boolean result;
        // Orphaning is required if the old rendition is in the wrong location and the 'orphan
        // existing rendition' param is set to true or the RenditionLocation specifies a destination NodeRef.
        if (location.getChildRef() != null)
            result = true;
        else
            result = AbstractRenderingEngine.getParamWithDefault(RenditionService.PARAM_ORPHAN_EXISTING_RENDITION,
                        Boolean.FALSE, renditionDefinition);
        
        if (logger.isDebugEnabled())
        {
            StringBuilder msg = new StringBuilder();
            msg.append("The old rendition does ");
            if (result == false)
            {
                msg.append("not ");
            }
            msg.append("require orphaning.");
            logger.debug(msg.toString());
        }
        return result;
    }

    /**
     * This method determines whether or not the old rendition is already in the correct location.
     */
    private boolean isOldRenditionInCorrectLocation()
    {
        boolean result;
        NodeRef destination = location.getChildRef();
        if (destination != null)
        {
            result = destination.equals(oldRendition);
        }
        else
        {
            ChildAssociationRef oldParentAssoc = nodeService.getPrimaryParent(oldRendition);
            NodeRef oldParent = oldParentAssoc.getParentRef();
            if (oldParent.equals(location.getParentRef()))
            {
                String childName = location.getChildName();
                if (childName == null)
                    result = true;
                else
                {
                    Serializable oldName = nodeService.getProperty(oldRendition, ContentModel.PROP_NAME);
                    result = childName.equals(oldName);
                }
            }
            result = false;
        }
        
        if (logger.isDebugEnabled())
        {
            StringBuilder msg = new StringBuilder();
            msg.append("The old rendition was ");
            if (result == false)
            {
                msg.append("not ");
            }
            msg.append("in the correct location");
            logger.debug(msg.toString());
        }
        
        return result;
    }

    private ChildAssociationRef getSpecifiedRenditionOrCreateNewRendition(QName renditionName)
    {
        ChildAssociationRef result;
        NodeRef destination = location.getChildRef();
        if (destination != null)
            result = nodeService.getPrimaryParent(destination);
        else
            result = createNewRendition(renditionName);
        
        return result;
    }

    /**
     * This method creates a new rendition node. If the source node for this rendition is not
     * the primary parent of the newly created rendition node, the rendition node is added as
     * a child of the source node.
     * 
     * @param renditionName
     * @return the primary parent association of the newly created rendition node.
     */
    private ChildAssociationRef createNewRendition(QName renditionName)
    {
        NodeRef parentRef = location.getParentRef();
        boolean parentIsSource = parentRef.equals(sourceNode);
        QName renditionType = RenditionModel.ASSOC_RENDITION;
        QName assocTypeQName = parentIsSource ? renditionType : ContentModel.ASSOC_CONTAINS;
        QName nodeTypeQName = ContentModel.TYPE_CONTENT;

        ChildAssociationRef primaryAssoc = nodeService.createNode(parentRef, assocTypeQName, renditionName,
                    nodeTypeQName);

        if (logger.isDebugEnabled())
        {
            StringBuilder msg = new StringBuilder();
            msg.append("Created new rendition node ").append(primaryAssoc);
            logger.debug(msg.toString());
        }

        // If the new rendition is not directly under the source node then add
        // the rendition association.
        if (parentIsSource == false)
        {
            NodeRef rendition = primaryAssoc.getChildRef();
            ChildAssociationRef newChild = null;
            behaviourFilter.disableBehaviour(sourceNode, ContentModel.ASPECT_AUDITABLE);
            try
            {
                newChild = nodeService.addChild(sourceNode, rendition, renditionType, renditionName);
            }
            finally
            {
                behaviourFilter.enableBehaviour(sourceNode, ContentModel.ASPECT_AUDITABLE);
            }

            if (logger.isDebugEnabled())
            {
                StringBuilder msg = new StringBuilder();
                msg.append("Added new rendition node as child of source node ").append(newChild);
                logger.debug(msg.toString());
            }
        }
        return primaryAssoc;
    }

    /**
     * This method returns the rendition on the given sourceNode with the given renditionDefinition, if such
     * a rendition exists.
     * 
     * @param sourceNode
     * @param renditionDefinition
     * @return the rendition node if one exists, else null.
     */
    private NodeRef getOldRenditionIfExists(NodeRef sourceNode, RenditionDefinition renditionDefinition)
    {
        QName renditionName=renditionDefinition.getRenditionName();
        ChildAssociationRef renditionAssoc = renditionService.getRenditionByName(sourceNode, renditionName);
        
        NodeRef result = (renditionAssoc == null) ? null : renditionAssoc.getChildRef();
        if (logger.isDebugEnabled())
        {
            StringBuilder msg = new StringBuilder();
            msg.append("Existing rendition with name ")
               .append(renditionName)
               .append(": ")
               .append(result);
            logger.debug(msg.toString());
        }
        
        return result;
    }
    
    /**
     * This method copies properties from the temporary rendition node onto the targetNode. It also sets the node type.
     * {@link #unchangedProperties Some properties} are not copied.
     */
    public void transferNodeProperties()
    {
        NodeRef targetNode = finalRenditionAssoc.getChildRef();
        if (logger.isDebugEnabled())
        {
            StringBuilder msg = new StringBuilder();
            msg.append("Transferring some properties from ").append(tempRenditionNode).append(" to ").append(targetNode);
            logger.debug(msg.toString());
        }

        // Copy the type from the temporary rendition to the real rendition
        QName type = nodeService.getType(tempRenditionNode);
        nodeService.setType(targetNode, type);
        
        // Copy over all regular properties from the temporary rendition
        Map<QName, Serializable> newProps = new HashMap<QName, Serializable>();
        for(Entry<QName,Serializable> entry : nodeService.getProperties(tempRenditionNode).entrySet())
        {
            QName propKey = entry.getKey();
            if(unchangedProperties.contains(propKey) ||
               NamespaceService.SYSTEM_MODEL_1_0_URI.equals(propKey.getNamespaceURI()))
            {
                // These shouldn't be copied over
                continue;
            }
            newProps.put(propKey, entry.getValue());
        }
        nodeService.setProperties(targetNode, newProps);
    }
}
