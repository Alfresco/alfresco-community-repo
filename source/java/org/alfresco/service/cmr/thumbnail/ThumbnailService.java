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
package org.alfresco.service.cmr.thumbnail;

import java.util.List;
import java.util.Map;

import org.alfresco.repo.thumbnail.ThumbnailDefinition;
import org.alfresco.repo.thumbnail.ThumbnailRegistry;
import org.alfresco.service.Auditable;
import org.alfresco.service.NotAuditable;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.service.namespace.QName;

/**
 * Thumbnail Service API
 * 
 * @author Roy Wetherall (based on original contribution by Ray Gauss II)
 */
public interface ThumbnailService
{
    /**
     * Gets the thumbnail registry
     * 
     * @return  {@link ThumbnailRegistry} thumbnail registry
     */
    @NotAuditable
    ThumbnailRegistry getThumbnailRegistry();
    
    /**
     * Creates a new thumbnail for the given node and content property.
     * 
     * The mimetype and transformation options are used to determine the content transformer that
     * will be best suited to create the thumbnail.
     * 
     * The thumbnail name is optional, but is usually set to provide an easy way to identify a particular
     * 'type' of thumbnail.
     * 
     * Once created the source node will have the 'rn:renditioned' aspect applied and an
     * association to the thumbnail node (of type 'rn:rendition') will be created.
     * 
     * The returned node reference is to the 'rn:rendition' content node that contains
     * the thumbnail content in the standard 'cm:content' property.
     * 
     * @see org.alfresco.service.cmr.thumnail.ThumbnailDefinition
     * 
     * @param  node                     the source content node
     * @param  contentProperty          the content property
     * @param  mimetype                 the thumbnail mimetype
     * @param  transformationOptions    the thumbnail transformation options
     * @param  name                     the name of the thumbnail (optional, pass null for unnamed thumbnail)
     * @return NodeRef                  node reference to the newly created thumbnail 
     */
    @Auditable(parameters = {"node", "contentProperty", "mimetype", "transformationOptions", "name"})
    NodeRef createThumbnail(NodeRef node, QName contentProperty, String mimetype, TransformationOptions transformationOptions, String name);
    
    /**
     * @see ThumbnailService#createThumbnail(NodeRef, QName, String, TransformationOptions, String)
     * 
     * If parent association details are specified then the thumbnail is created as a child of the specified parent and linked
     * via a non-primary association to the original content node.
     * 
     * @param node                      the source content node
     * @param contentProperty           the content property
     * @param mimetype                  the thumbnail mimetype
     * @param transformationOptions     the thumbnail transformation options
     * @param name                      the name of the thumbnail (optional, pass null for unnamed thumbnail)
     * @param assocDetails              the thumbnail parent association details
     * @return NodeRef                  node reference to the newly created thumbnail
     */
    @Auditable(parameters = {"node", "contentProperty", "mimetype", "transformationOptions", "name", "assocDetails"})
    NodeRef createThumbnail(NodeRef node, QName contentProperty, String mimetype, TransformationOptions transformationOptions, String name, ThumbnailParentAssociationDetails assocDetails);
    
    /**
     * Updates the content of a thumbnail.
     * 
     * The original thumbnail content is updated from the source content using the transformation
     * options provided.  The mimetype and name of the thumbnail remain unchanged.
     * 
     * To change the name or mimetype of an updated thumbnail it should be deleted and recreated.
     * 
     * An error is raised if the original content no longer exists.
     * 
     * @param thumbnail             the thumbnail node
     * @param transformationOptions the transformation options used when updating the thumbnail
     */
    @Auditable(parameters = {"thumbnail", "transformationOptions"})
    void updateThumbnail(NodeRef thumbnail, TransformationOptions transformationOptions);
    
    /**
     * Gets the thumbnail for a given content property with a given name.
     * 
     * Returns null if no thumbnail with that name for that content property is found.
     * 
     * @param node              node reference
     * @param contentProperty   content property name
     * @param thumbnailName     thumbnail name
     * @return NodeRef          the thumbnail node reference, null if not found
     */
    @Auditable(parameters = {"node", "contentProperty", "thumbnailName"})
    NodeRef getThumbnailByName(NodeRef node, QName contentProperty, String thumbnailName);
    
    /**
     * Gets a list of thumbnail nodes for a given content property that match the provided mimetype and
     * transformation options.
     * 
     * Both mimetype and transformation options are optional parameters.  If only one or other is specified the
     * only the other is considered during.  If neither are provided all thumbnails for that content property 
     * are returned.
     * 
     * If no matches are found then an empty list is returned.
     * 
     * @param node                  node reference
     * @param contentProperty       content property name
     * @param mimetype              mimetype
     * @param options               transformation options
     * @return List<NodeRef>        list of matching thumbnail node references, empty if no matches found
     */
    @Auditable(parameters = {"node", "contentProperty", "mimetype", "options"})
    List<NodeRef> getThumbnails(NodeRef node, QName contentProperty, String mimetype, TransformationOptions options);
    
    /**
     * This method returns a {@link Map} of {@link FailedThumbnailInfo failed thumbnails} for the specified source node.
     * The map is keyed by {@link ThumbnailDefinition#getName() thumbnail definition name}
     * and the values are the {@link FailedThumbnailInfo failed thumbnails}.
     * 
     * @param sourceNode the node whose thumbnails are to be checked.
     * @return a Map of failed thumbnails, if any. If there
     *         are no such failures, an empty Map will be returned.
     * @since 3.5.0
     */
    @Auditable(parameters = {"sourceNode"})
    Map<String, FailedThumbnailInfo> getFailedThumbnails(NodeRef sourceNode);
}
