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

import org.alfresco.service.Auditable;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Thumbnail Service API
 * 
 * @author Roy Wetherall (based on original contribution by Ray Gauss II)
 */
public interface ThumbnailService
{
    /**
     * Creates a new thumbnail for the given node and content property.
     * 
     * The passed create options specify the details of the thumbnail, including the 
     * mimetpye, size and location of the thumbnail.
     * 
     * Once created the source node will have the 'tn:thumbnailed' aspect applied and an
     * association to the thumbnail node (or type 'tn:thumbnail') will be created.
     * 
     * The returned node reference is to the 'tn:thumbnail' content node that contains
     * the thumnail content in the standard 'cm:content' property.
     * 
     * @see org.alfresco.service.cmr.thumnail.GenerateOptions
     * 
     * @param  node                 the source content node
     * @param  contentProperty      the content property
     * @param  createOptions        the create options
     * @return NodeRef              node reference to the newly created thumbnail 
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"node", "contentProperty", "createOptions"})
    NodeRef createThumbnail(NodeRef node, QName contentProperty, GenerateOptions createOptions);
    
    /**
     * Updates the content of a thumbnail.
     * 
     * The original creation options are used when updating the thumbnail.  The content of 
     * the associated thumbnailed node is used to update.
     * 
     * An error is raised if the original content no longer exisits.
     * 
     * @param thumbnail             the thumbnail node
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"thumbnail"})
    void updateThumbnail(NodeRef thumbnail);
    
    /**
     * Gets a list of the thumbnails that are available for the node's content property, given a
     * the accept options.
     * 
     * The accept options contain details about the desired mimetypes, size and other parameters of
     * a potential thumbnail.  If any of the nodes thumbnails match these accept options they are 
     * added to the return list.
     * 
     * The list of returned thumbnails is ordered, with the most appropriate thumbnail first.  If no 
     * appropriate thumbnails are found then the list is returned empty.
     * 
     * When no accept options are provided all available thumbnails are returned.
     * 
     * @see org.alfresco.service.cmr.thumbnail.AcceptOptions
     * 
     * @param  node                 the content node
     * @param  contentProperty      the content property
     * @param  acceptOptions        the accept options
     * @return List<NodeRef>        list of thumbnails that match the accept options
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"node", "contentProperty", "acceptOptions"})
    List<NodeRef> getThumbnails(NodeRef node, QName contentProperty, AcceptOptions acceptOptions);
}
