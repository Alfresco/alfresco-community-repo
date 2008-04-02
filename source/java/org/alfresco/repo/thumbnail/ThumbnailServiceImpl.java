/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
package org.alfresco.repo.thumbnail;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.thumbnail.AcceptOptions;
import org.alfresco.service.cmr.thumbnail.GenerateOptions;
import org.alfresco.service.cmr.thumbnail.ThumbnailService;
import org.alfresco.service.cmr.thumbnail.GenerateOptions.ParentAssociationDetails;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;

/**
 * @author Roy Wetherall
 */
public class ThumbnailServiceImpl implements ThumbnailService
{
    /** Node service */
    private NodeService nodeService;
    
    /** Content service */
    private ContentService contentService;
    
    /**
     * Set the node service
     * 
     * @param nodeService   node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    /**
     * Set the content service
     * 
     * @param contentService    content service
     */
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }
    
    /**
     * @see org.alfresco.service.cmr.thumbnail.ThumbnailService#createThumbnail(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName, org.alfresco.service.cmr.thumbnail.GenerateOptions)
     */
    public NodeRef createThumbnail(NodeRef node, QName contentProperty, GenerateOptions createOptions)
    {
        NodeRef thumbnail = null;
        
        // Apply the thumbnailed aspect to the node if it doesn't already have it
        if (this.nodeService.hasAspect(node, ContentModel.ASPECT_THUMBNAILED) == false)
        {
            this.nodeService.addAspect(node, ContentModel.ASPECT_THUMBNAILED, null);
        }
        
        // Get the name of the thumbnail and add to properties map
        String thumbnailName = createOptions.getThumbnailName();
        if (thumbnailName == null)
        {
            thumbnailName = GUID.generate();
        }
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>(1);
        properties.put(ContentModel.PROP_NAME, thumbnailName);
        // TODO .. somehow we need to store the details of the thumbnail on the node so we can regen the thumbnail later ..
        
        // See if parent association details have been specified for the thumbnail
        ParentAssociationDetails assocDetails = createOptions.getParentAssociationDetails();
        if (assocDetails == null)
        {
            // Create the thumbnail using the thumbnails child association
            thumbnail = this.nodeService.createNode(
                    node, 
                    ContentModel.ASSOC_THUMBNAILS,
                    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, thumbnailName), 
                    ContentModel.TYPE_THUMBNAIL, 
                    properties).getChildRef();
        }
        else
        {
            // Create the thumbnail using the specified parent assoc details
            thumbnail = this.nodeService.createNode(
                    assocDetails.getParent(),
                    assocDetails.getAssociationType(),
                    assocDetails.getAssociationName(),
                    ContentModel.TYPE_THUMBNAIL,
                    properties).getChildRef();
            
            // Associate the new thumbnail to the source
            this.nodeService.addChild(
                    node, 
                    thumbnail, 
                    ContentModel.ASSOC_THUMBNAILS, 
                    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, thumbnailName));
        }
        
        // TODO .. do the work of actually creating the thumbnail content ...
        
        // Return the created thumbnail
        return thumbnail;
    }

    /**
     * @see org.alfresco.service.cmr.thumbnail.ThumbnailService#getThumbnails(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName, org.alfresco.service.cmr.thumbnail.AcceptOptions)
     */
    public List<NodeRef> getThumbnails(NodeRef node, QName contentProperty,  AcceptOptions acceptOptions)
    {
        return null;
    }

    /**
     * @see org.alfresco.service.cmr.thumbnail.ThumbnailService#updateThumbnail(org.alfresco.service.cmr.repository.NodeRef)
     */
    public void updateThumbnail(NodeRef thumbnail)
    {
    }

}
