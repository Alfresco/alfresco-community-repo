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
import org.alfresco.service.PublicService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.Auditable;
import org.alfresco.service.NotAuditable;

/**
 * The Rendition service.
 * @author Nick Smith
 * @author Neil McErlean
 */
@PublicService
public interface RenditionService extends RenditionDefinitionPersister
{
    /**
     * This optional {@link NodeRef} parameter specifies an existing {@link NodeRef} to use
     * as the rendition node. Properties on this node (including the content
     * property cm:content) will be updated as required but the location and
     * name of the node will not change. This parameter takes precedence over
     * PARAM_DESTINATION_PATH_TEMPLATE.
     */
    public static final String PARAM_DESTINATION_NODE = "rendition-destination-node";

    /**
     * This optional {@link String} parameter indicates where the rendition will be
     * created. The parameter may specify either the actual file path to the
     * rendition or it may specify a Freemarker template which can be resolved
     * to a file path. In either case the path is relative to the root node of
     * the store in which the source node exists. If the parameter
     * PARAM_DESTINATION_NODE has been set then this parameter will be ignored.
     */
    public static final String PARAM_DESTINATION_PATH_TEMPLATE = "destination-path-template";

    /**
     * This optional {@link QName} parameter specifies what the node type of the created rendition will
     * be. If no type is specified then this defaults to cm:content.
     */
    public static final String PARAM_RENDITION_NODETYPE = "rendition-nodetype";

    /**
     * This optional {@link Boolean} flag parameter determines whether an
     * existing rendition is moved or orphaned. If the source node already has a
     * rendition and this parameter is <code>false</code> the old rendition will
     * be moved to the new destination location and updated appropriately. If
     * this parameter is set to <code>true</code> then the old rendition will be
     * left in its current location and the rold rendition association from the
     * source node to the old rendition will be deleted.
     */
    public static final String PARAM_ORPHAN_EXISTING_RENDITION = "orphan-existing-rendition";

    /**
     * This optional boolean parameter specified whether the rendition is a component within
     * a composite rendition. Such component renditions should not execute the standard
     * pre- and post-rendition behaviour as it will be taken care of by the CompositeRenderingEngine's
     * execution.
     */
    public static final String PARAM_IS_COMPONENT_RENDITION = "is-component-rendition";

    /**
     * Returns the {@link RenderingEngineDefinition} associated with the
     * specified rendering engine name.
     * 
     * @param name The rendering engine name.
     * @return The {@link RenderingEngineDefinition} or null.
     */
    @NotAuditable
    RenderingEngineDefinition getRenderingEngineDefinition(String name);

    /**
     * @return A {@link List} of all available {@link RenderingEngineDefinition}
     *         s.
     */
    @NotAuditable
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
    @NotAuditable
    RenditionDefinition createRenditionDefinition(QName renditionName, String renderingEngineName);

    /**
     * Creates a new {@link CompositeRenditionDefinition} and sets the rendition
     * name and the rendering engine name to the specified values.
     * 
     * @param renditionName A unique identifier used to specify the created
     *            {@link RenditionDefinition}.
     * @return the created {@link CompositeRenditionDefinition}.
     */
    @NotAuditable
    CompositeRenditionDefinition createCompositeRenditionDefinition(QName renditionName);

    /**
     * This method gets all the renditions of the specified node.
     * 
     * @return a list of ChildAssociationRefs which link the source node to the
     *         renditions.
     */
    @NotAuditable
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
    @NotAuditable
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
    @NotAuditable
    ChildAssociationRef getRenditionByName(NodeRef node, QName renditionName);

    /**
     * This method returns <code>true</code> if the specified NodeRef is a valid
     * rendition node, else <code>false</code>. A nodeRef is a rendition node
     * if it has the rn:rendition aspect (or sub-aspect) applied.
     * 
     * @param node
     * @return <code>true</code> if a rendition, else <code>false</code>
     */
    @NotAuditable
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
    @NotAuditable
    ChildAssociationRef getSourceNode(NodeRef renditionNode);
    
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
    @NotAuditable
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
    @NotAuditable
    void render(NodeRef sourceNode, RenditionDefinition renditionDefinition,
            RenderCallback callback);
    
    /**
     * This method synchronously renders content as specified by the given
     * {@link RenditionDefinition#getRenditionName() rendition name}.
     * The content to be rendered is provided by the specified source node.
     * <p/>
     * The Rendition Definition will be loaded from the standard location as system
     * thus allowing rendition definitions to be used even when the Data Dictionary
     * has restricted read access.
     * 
     * @param sourceNode the node from which the content is retrieved.
     * @param renditionDefinitionQName the rendition definition which is to
     *            be performed.
     * @return a child association reference which is the link from the source
     *         node to the newly rendered content.
     * @throws RenditionServiceException if there is a problem in rendering the
     *             given sourceNode
     * @since 3.4.2
     */
    @NotAuditable
    ChildAssociationRef render(NodeRef sourceNode, QName renditionDefinitionQName);

    /**
     * This method asynchronously renders content as specified by the given
     * {@link RenditionDefinition#getRenditionName() rendition definition name}.
     * The content to be rendered is provided by the specified source node.
     * <p/>
     * The Rendition Definition will be loaded from the standard location as system
     * thus allowing rendition definitions to be used even when the Data Dictionary
     * has restricted read access.
     * 
     * @param sourceNode the node from which the content is retrieved.
     * @param renditionDefinitionQName the rendition definition which is to
     *            be performed.
     * @param callback a callback object to handle the ultimate result of the rendition.
     * @since 3.4.2
     */
    @NotAuditable
    void render(NodeRef sourceNode, QName renditionDefinitionQName, RenderCallback callback);
}
