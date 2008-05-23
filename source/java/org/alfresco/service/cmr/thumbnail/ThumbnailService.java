/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.service.cmr.thumbnail;

import java.util.List;

import org.alfresco.repo.thumbnail.ThumbnailRegistry;
import org.alfresco.service.Auditable;
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
    ThumbnailRegistry getThumbnailRegistry();
    
    /**
     * Creates a new thumbnail for the given node and content property.
     * 
     * The mimetype and transformation options are used to determine the content transformer that
     * will be best suited to create the thumbnail.
     * 
     * The thumbnail name is optional, but is usally set to provide an easy way to identify a perticular
     * 'type' of thumbnail.
     * 
     * Once created the source node will have the 'tn:thumbnailed' aspect applied and an
     * association to the thumbnail node (or type 'tn:thumbnail') will be created.
     * 
     * The returned node reference is to the 'tn:thumbnail' content node that contains
     * the thumnail content in the standard 'cm:content' property.
     * 
     * @see org.alfresco.service.cmr.thumnail.ThumbnailDetails
     * 
     * @param  node                     the source content node
     * @param  contentProperty          the content property
     * @param  mimetype                 the thumbnail mimetype
     * @param  transformationOptions    the thumbnail transformation options
     * @param  name                     the name of the thumbnail (optional, pass null for unnamed thumbnail)
     * @return NodeRef                  node reference to the newly created thumbnail 
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"node", "contentProperty", "mimetype", "transformationOptions", "name"})
    NodeRef createThumbnail(NodeRef node, QName contentProperty, String mimetype, TransformationOptions transformationOptions, String name);
    
    NodeRef createThumbnail(NodeRef node, QName contentProperty, String mimetype, TransformationOptions transformationOptions, String name, ThumbnailParentAssociationDetails assocDetails);
    
    /**
     * Updates the content of a thumbnail.
     * 
     * The origional thumbnail content is updated from the source content using the transformation
     * options provided.  The mimetype and name of the thumbnail remain unchanged.
     * 
     * To change the name or mimertpe of an updated thumbnail it should be deleted and recreated.
     * 
     * An error is raised if the original content no longer exisits.
     * 
     * @param thumbnail             the thumbnail node
     * @param transformationOptions the transformation options used when updating the thumbnail
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"thumbnail", "transformationOptions"})
    void updateThumbnail(NodeRef thumbnail, TransformationOptions transformationOptions);
    
    /**
     * Get's the thumbnail for a given content property with a given name.
     * 
     * Returns null if no thumbnail with that name for that content property is found.
     * 
     * @param node              node reference
     * @param contentProperty   content property name
     * @param thumbnailName     thumbnail name
     * @return NodeRef          the thumbnail node reference, null if not found
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"node", "contentProperty", "thumbnailName"})
    NodeRef getThumbnailByName(NodeRef node, QName contentProperty, String thumbnailName);
    
    /**
     * Get's a list of thumbnail nodes for a given content property that match the provided mimetype and
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
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"node", "contentProperty", "mimetype", "options"})
    List<NodeRef> getThumbnails(NodeRef node, QName contentProperty, String mimetype, TransformationOptions options);
    
}
