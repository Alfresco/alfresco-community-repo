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

package org.alfresco.service.cmr.rendition;

import java.util.List;

import org.alfresco.repo.rendition.RenditionDefinitionPersister;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * @author Nick Smith
 * @author Neil McErlean
 */
public interface RenditionService extends RenditionDefinitionPersister
{
    public static final String PARAM_DESTINATION_NODE = "rendition-destination-node";
    public static final String PARAM_DESTINATION_PATH_TEMPLATE = "destination-path-template";
    public static final String PARAM_RENDITION_NODETYPE = "rendition-nodetype";
    public static final String PARAM_ORPHAN_EXISTING_RENDITION = "orphan-existing-rendition";
    
    /**
     * Returns the {@link RenderingEngineDefinition} associated with the
     * specified rendering engine name.
     * 
     * @param name The rendering engine name.
     * @return The {@link RenderingEngineDefinition} or null.
     */
    RenderingEngineDefinition getRenderingEngineDefinition(String name);

    /**
     * @return A {@link List} of all available {@link RenderingEngineDefinition}
     *         s.
     */
    List<RenderingEngineDefinition> getRenderingEngineDefinitions();

    /**
     * Creates a new {@link RenditionDefinition} and sets the rendition name and
     * the rendering engine name to the specified values.
     * 
     * @param renditionName A unique identifier used to specify the created
     *            {@link RenditionDefinition}.
     * @param renderingEngineName The name of the rendering engine associated
     *            with this {@link RenditionDefinition}.
     * @return the created {@link RenditionDefinition}.
     */
    RenditionDefinition createRenditionDefinition(QName renditionName, String renderingEngineName);

    /**
     * Creates a new {@link CompositeRenditionDefinition} and sets the rendition
     * name and the rendering engine name to the specified values.
     * 
     * @param renditionName A unique identifier used to specify the created
     *            {@link RenditionDefinition}.
     * @return the created {@link CompositeRenditionDefinition}.
     */
    CompositeRenditionDefinition createCompositeRenditionDefinition(QName renditionName);

    /**
     * This method gets all the renditions of the specified node.
     * 
     * @return a list of ChildAssociationRefs which link the source node to the
     *         renditions.
     */
    List<ChildAssociationRef> getRenditions(NodeRef node);

    /**
     * This method gets all the renditions of the specified node filtered by
     * MIME-type prefix. Renditions whose MIME-type string startsWith the prefix
     * will be returned.
     * 
     * @param node the source node for the renditions
     * @param mimeTypePrefix a prefix to check against the rendition MIME-types.
     *            This must not be null and must not be an empty String
     * @return a list of ChildAssociationRefs which link the source node to the
     *         filtered renditions.
     */
    List<ChildAssociationRef> getRenditions(NodeRef node, String mimeTypePrefix);

    /**
     * This method gets the rendition of the specified node identified by
     * the provided rendition name.
     * 
     * @param node the source node for the renditions
     * @param renditionName the renditionName used to identify a rendition.
     * @return the ChildAssociationRef which links the source node to the
     *         rendition or <code>null</code> if there is no such rendition.
     */
    ChildAssociationRef getRenditionByName(NodeRef node, QName renditionName);

    /**
     * This method returns <code>true</code> if the specified NodeRef is a valid
     * rendition node, else <code>false</code>. A nodeRef is a rendition node
     * if it has the rn:rendition aspect (or sub-aspect) applied.
     * 
     * @param node
     * @return <code>true</code> if a rendition, else <code>false</code>
     */
    boolean isRendition(NodeRef node);
    
    /**
     * This method gets the source node for the specified rendition node. There
     * should only be one source node for any given rendition node.
     * 
     * @param renditionNode the nodeRef holding the rendition.
     * @return the ChildAssociationRef whose parentNodeRef is the source node, or
     *         <code>null</code> if there is no source node.
     * 
     * @see RenditionService#isRendition(NodeRef)
     */
    ChildAssociationRef getSourceNode(NodeRef renditionNode);
    
    //TODO The result should be the link to the primary parent.
    /**
     * This method synchronously renders content as specified by the given
     * {@link RenditionDefinition}. The content to be rendered is provided by
     * the specified source node.
     * 
     * @param sourceNode the node from which the content is retrieved.
     * @param renditionDefinition this fully specifies the rendition which is to
     *            be performed.
     * @return a child association reference which is the link from the source
     *         node to the newly rendered content.
     * @throws RenditionServiceException if there is a problem in rendering the
     *             given sourceNode
     */
    ChildAssociationRef render(NodeRef sourceNode, RenditionDefinition renditionDefinition);

    /**
     * This method asynchronously renders content as specified by the given
     * {@link RenditionDefinition}. The content to be rendered is provided by
     * the specified source node.
     * 
     * @param sourceNode the node from which the content is retrieved.
     * @param renditionDefinition this fully specifies the rendition which is to
     *            be performed.
     * @param callback a callback object to handle the ultimate result of the rendition.
     */
    void render(NodeRef sourceNode, RenditionDefinition renditionDefinition,
            RenderCallback callback);
}
