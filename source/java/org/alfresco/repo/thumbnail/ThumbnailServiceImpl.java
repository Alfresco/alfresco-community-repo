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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.service.cmr.thumbnail.CreateOptions;
import org.alfresco.service.cmr.thumbnail.ThumbnailException;
import org.alfresco.service.cmr.thumbnail.ThumbnailService;
import org.alfresco.service.cmr.thumbnail.CreateOptions.ParentAssociationDetails;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.GUID;
import org.alfresco.util.ParameterCheck;

/**
 * @author Roy Wetherall
 */
public class ThumbnailServiceImpl implements ThumbnailService
{
    /** Error messages */
    private static final String ERR_NO_CREATE = "Thumbnail could not be created as required transformation is not supported.";
    private static final String ERR_DUPLICATE_NAME = "Thumbnail could not be created because of a duplicate name";
    private static final String ERR_NO_PARENT = "Thumbnail has no parent so update cannot take place.";
    private static final String ERR_TOO_PARENT = "Thumbnail has more than one source content node.  This is invalid so update cannot take place.";
    private static final String ERR_TRANS_CLASS = "Unable to create transformation options from saved class.  Update of thumbnail cannnot take place.";
    
    /** Node service */
    private NodeService nodeService;
    
    /** Content service */
    private ContentService contentService;
    
    /** Mimetype map */
    private MimetypeMap mimetypeMap;
    
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
     * Sets the mimetype map
     * 
     * @param mimetypeMap   the mimetype map
     */
    public void setMimetypeMap(MimetypeMap mimetypeMap)
    {
        this.mimetypeMap = mimetypeMap;
    }
    
    /**
     * @see org.alfresco.service.cmr.thumbnail.ThumbnailService#createThumbnail(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName, org.alfresco.service.cmr.thumbnail.GenerateOptions)
     */
    public NodeRef createThumbnail(NodeRef node, QName contentProperty, CreateOptions createOptions)
    {
        // Parameter check
        ParameterCheck.mandatory("node", node); 
        ParameterCheck.mandatory("contentProperty", contentProperty);
        
        NodeRef thumbnail = null;
        
        // Check for duplicate names
        if (createOptions.getThumbnailName() != null && getThumbnailByName(node, contentProperty, createOptions.getThumbnailName()) != null)
        {
            // We can't continue because there is already an thumnail with the given name for that content property
            throw new ThumbnailException(ERR_DUPLICATE_NAME);
        }
        
        // Apply the thumbnailed aspect to the node if it doesn't already have it
        if (this.nodeService.hasAspect(node, ContentModel.ASPECT_THUMBNAILED) == false)
        {
            this.nodeService.addAspect(node, ContentModel.ASPECT_THUMBNAILED, null);
        }
        
        // Get the name of the thumbnail and add to properties map
        String thumbnailName = createOptions.getThumbnailName();
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>(2);
        if (thumbnailName == null)
        {
            thumbnailName = GUID.generate();
        }
        else
        {
            String thumbnailFileName = generateThumbnailFileName(thumbnailName, createOptions.getDestinationMimetype());
            properties.put(ContentModel.PROP_NAME, thumbnailFileName);
        }
        
        // Add the name of the content property
        properties.put(ContentModel.PROP_CONTENT_PROPERTY_NAME, contentProperty);
       
        // Add the class name of the transformation options
        if (createOptions.getTransformationOptions() != null)
        {
            properties.put(ContentModel.PROP_TRANSFORMATION_OPTIONS_CLASS, createOptions.getTransformationOptions().getClass().getName());
        }
        
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
        
        // Get the content reader and writer for content nodes
        ContentReader reader = this.contentService.getReader(node, contentProperty);
        ContentWriter writer = this.contentService.getWriter(thumbnail, ContentModel.PROP_CONTENT, true);
        writer.setMimetype(createOptions.getDestinationMimetype());
        writer.setEncoding(reader.getEncoding());
        
        // Catch the failure to create the thumbnail
        if (this.contentService.isTransformable(reader, writer, createOptions.getTransformationOptions()) == false)
        {
            // Throw exception indicating that the thumbnail could not be created
            throw new ThumbnailException(ERR_NO_CREATE);
        }
        else
        {
            // Do the thumnail transformation
            this.contentService.transform(reader, writer, createOptions.getTransformationOptions());
            
            // Store the transformation options on the thumbnail
            createOptions.getTransformationOptions().saveToNode(thumbnail, this.nodeService);
        }
        
        // Return the created thumbnail
        return thumbnail;
    }

    /**
     * Generates the thumbnail name from the name and destination mimertype
     * 
     * @param  thumbnailName         the thumbnail name
     * @param  destinationMimetype   the destination name 
     * @return String                the thumbnail file name
     */
    private String generateThumbnailFileName(String thumbnailName, String destinationMimetype)
    {
        return thumbnailName + "." + this.mimetypeMap.getExtension(destinationMimetype);
    }

    /**
     * @see org.alfresco.service.cmr.thumbnail.ThumbnailService#updateThumbnail(org.alfresco.service.cmr.repository.NodeRef)
     */
    public void updateThumbnail(NodeRef thumbnail)
    {
        // First check that we are dealing with a thumbnail
        if (ContentModel.TYPE_THUMBNAIL.equals(this.nodeService.getType(thumbnail)) == true)
        {
            // Get the transformation options
            TransformationOptions options = null;
            String transformationOptionsClassName = (String)this.nodeService.getProperty(thumbnail, ContentModel.PROP_TRANSFORMATION_OPTIONS_CLASS);
            if (transformationOptionsClassName == null)
            {
                options = new TransformationOptions();
            }
            else
            {
                // Create an options object of the type specified on the thumbnail
                try
                {
                    Class transformationClass = Class.forName(transformationOptionsClassName);
                    options = (TransformationOptions)transformationClass.newInstance();                    
                }
                catch (Exception exception)
                {
                    throw new ThumbnailException(ERR_TRANS_CLASS);
                }
                
                // Populate the options from the node
                options.populateFromNode(thumbnail, this.nodeService);
            }            
            
            // Get the node that is the source of the thumbnail
            NodeRef node = null;
            List<ChildAssociationRef> parents = this.nodeService.getParentAssocs(thumbnail, ContentModel.ASSOC_THUMBNAILS, RegexQNamePattern.MATCH_ALL);
            if (parents.size() == 0)
            {
                throw new ThumbnailException(ERR_NO_PARENT);
            }
            else if (parents.size() != 1)
            {
                throw new ThumbnailException(ERR_TOO_PARENT);
            }
            else
            {
                node = parents.get(0).getParentRef();
            }
            
            // Get the content property
            QName contentProperty = (QName)this.nodeService.getProperty(thumbnail, ContentModel.PROP_CONTENT_PROPERTY_NAME);
            
            // Get the reader and writer            
            ContentReader reader = this.contentService.getReader(node, contentProperty);
            ContentWriter writer = this.contentService.getWriter(node, ContentModel.PROP_CONTENT, true);
            
            // Set the basic detail of the transformation options
            options.setSourceNodeRef(node);
            options.setSourceContentProperty(contentProperty);
            options.setTargetNodeRef(thumbnail);
            options.setTargetContentProperty(ContentModel.PROP_CONTENT);
            
            // Catch the failure to create the thumbnail
            if (this.contentService.isTransformable(reader, writer, options) == false)
            {
                // Throw exception indicating that the thumbnail could not be created
                throw new ThumbnailException(ERR_NO_CREATE);
            }
            else
            {
                // Do the thumnail transformation
                this.contentService.transform(reader, writer, options);
            }
        }
        // TODO else should we throw an exception?
    }

    /**
     * @see org.alfresco.service.cmr.thumbnail.ThumbnailService#getThumbnailByName(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName, java.lang.String)
     */
    public NodeRef getThumbnailByName(NodeRef node, QName contentProperty, String thumbnailName)
    {
        NodeRef thumbnail = null;
        
        //
        // NOTE:
        //
        // Since there is not an easy alternative and for clarity the node service is being used to retrieve the thumbnails.
        // If retrieval performance becomes an issue then this code can be replaced
        //
        
        // Check that the node has the thumbnailed aspect applied
        if (nodeService.hasAspect(node, ContentModel.ASPECT_THUMBNAILED) == true)
        {
            // Get all the thumnails that match the thumbnail name
            List<ChildAssociationRef> assocs = this.nodeService.getChildAssocs(node, ContentModel.ASSOC_THUMBNAILS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, thumbnailName));
            for (ChildAssociationRef assoc : assocs)
            {
                // Check the child to see if it matches the content property we are concerned about.
                // We can assume there will only ever be one per content property since createThumbnail enforces this.
                NodeRef child = assoc.getChildRef();
                if (contentProperty.equals(this.nodeService.getProperty(child, ContentModel.PROP_CONTENT_PROPERTY_NAME)) == true)
                {
                    thumbnail = child;
                    break;
                }
            }
        }
        
        return thumbnail;
    }

    /**
     * @see org.alfresco.service.cmr.thumbnail.ThumbnailService#getThumbnails(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName, java.lang.String, org.alfresco.service.cmr.repository.TransformationOptions)
     */
    public List<NodeRef> getThumbnails(NodeRef node, QName contentProperty, String mimetype, TransformationOptions options)
    {
        List<NodeRef> thumbnails = new ArrayList<NodeRef>(5);
        
        //
        // NOTE:
        //
        // Since there is not an easy alternative and for clarity the node service is being used to retrieve the thumbnails.
        // If retrieval performance becomes an issue then this code can be replaced
        //
        
        // Check that the node has the thumbnailed aspect applied
        if (nodeService.hasAspect(node, ContentModel.ASPECT_THUMBNAILED) == true)
        {
            // Get all the thumnails that match the thumbnail name
            List<ChildAssociationRef> assocs = this.nodeService.getChildAssocs(node, ContentModel.ASSOC_THUMBNAILS, RegexQNamePattern.MATCH_ALL);
            for (ChildAssociationRef assoc : assocs)
            {
                // Check the child to see if it matches the content property we are concerned about.
                // We can assume there will only ever be one per content property since createThumbnail enforces this.
                NodeRef child = assoc.getChildRef();
                if (contentProperty.equals(this.nodeService.getProperty(child, ContentModel.PROP_CONTENT_PROPERTY_NAME)) == true &&
                    matchMimetypeOptions(child, mimetype, options) == true)
                {
                    thumbnails.add(child);
                }
            }
        }
        
        return thumbnails;
    }
    
    /**
     * Determine whether the thumbnail meta-data matches the given mimetype and options
     * 
     * @param  thumbnail     thumbnail node reference
     * @param  mimetype      mimetype
     * @param  options       transformation options
     * @return boolean       true if the mimetype and options match the thumbnail metadata, false otherwise
     */
    private boolean matchMimetypeOptions(NodeRef thumbnail, String mimetype, TransformationOptions options)
    {
        boolean result = true;
        
        // Check the mimetype
        String thumbnailMimetype = ((ContentData)this.nodeService.getProperty(thumbnail, ContentModel.PROP_CONTENT)).getMimetype();
        if (mimetype.equals(thumbnailMimetype) == true)
        {
            // TODO continue to check options ...
        }
        else
        {
            result = false;
        }
        
        return result;
    }

    /**
     * @see org.alfresco.service.cmr.thumbnail.ThumbnailService#getThumbnails(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName, java.lang.String)
     */
    public List<NodeRef> getThumbnails(NodeRef node, QName contentProperty, String mimetype)
    {        
        return getThumbnails(node, contentProperty, mimetype, null);
    }

}
