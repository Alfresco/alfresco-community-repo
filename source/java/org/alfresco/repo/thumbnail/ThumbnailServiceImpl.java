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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.service.cmr.thumbnail.ThumbnailException;
import org.alfresco.service.cmr.thumbnail.ThumbnailParentAssociationDetails;
import org.alfresco.service.cmr.thumbnail.ThumbnailService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.GUID;
import org.springframework.extensions.surf.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Roy Wetherall
 */
public class ThumbnailServiceImpl implements ThumbnailService
{
    /** Logger */
    private static Log logger = LogFactory.getLog(ThumbnailServiceImpl.class);
    
    /** Error messages */
    private static final String ERR_NO_CREATE = "Thumbnail could not be created as required transformation is not supported from {0} to {1}";
    private static final String ERR_DUPLICATE_NAME = "Thumbnail could not be created because of a duplicate name";
    private static final String ERR_NO_PARENT = "Thumbnail has no parent so update cannot take place.";
    
    /** Mimetype wildcard postfix */
    private static final String SUBTYPES_POSTFIX = "/*";
    
    /** Node service */
    private NodeService nodeService;
    
    /** Content service */
    private ContentService contentService;
    
    /** Mimetype map */
    private MimetypeMap mimetypeMap;
    
    /** Behaviour filter */
    private BehaviourFilter behaviourFilter;
    
    /** Thumbnail registry */
    private ThumbnailRegistry thumbnailRegistry;
    
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
     * @param behaviourFilter  policy behaviour filter 
     */
    public void setBehaviourFilter(BehaviourFilter behaviourFilter)
    {
        this.behaviourFilter = behaviourFilter;
    }
    
    /**
     * Set thumbnail registry
     * 
     * @param thumbnailRegistry     thumbnail registry
     */
    public void setThumbnailRegistry(ThumbnailRegistry thumbnailRegistry)
    {
        this.thumbnailRegistry = thumbnailRegistry;
    }
    
    /**
     * @see org.alfresco.service.cmr.thumbnail.ThumbnailService#getThumbnailRegistry()
     */
    public ThumbnailRegistry getThumbnailRegistry()
    {
       return this.thumbnailRegistry;
    }    
    
    /**
     * @see org.alfresco.service.cmr.thumbnail.ThumbnailService#createThumbnail(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName, java.lang.String, org.alfresco.service.cmr.repository.TransformationOptions, java.lang.String)
     */
    public NodeRef createThumbnail(NodeRef node, QName contentProperty, String mimetype, TransformationOptions transformationOptions, String thumbnailName)
    {
        return createThumbnail(node, contentProperty, mimetype, transformationOptions, thumbnailName, null);
    }
    
    /**
     * @see org.alfresco.service.cmr.thumbnail.ThumbnailService#createThumbnail(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName, java.lang.String, org.alfresco.service.cmr.repository.TransformationOptions, java.lang.String, org.alfresco.service.cmr.thumbnail.ThumbnailParentAssociationDetails)
     */
    public NodeRef createThumbnail(final NodeRef node, final QName contentProperty, final String mimetype,
            final TransformationOptions transformationOptions, final String thumbnailName, final ThumbnailParentAssociationDetails assocDetails)
    {
        // Parameter check
        ParameterCheck.mandatory("node", node); 
        ParameterCheck.mandatory("contentProperty", contentProperty);
        ParameterCheck.mandatoryString( "mimetype", mimetype);
        ParameterCheck.mandatory("transformationOptions", transformationOptions);
        
        if (logger.isDebugEnabled() == true)
        {
            logger.debug("Creating thumbnail (node=" + node.toString() + "; contentProperty=" + contentProperty.toString() + "; mimetype=" + mimetype);
        }
        
        // Check for duplicate names
        if (thumbnailName != null && getThumbnailByName(node, contentProperty, thumbnailName) != null)
        {
            if (logger.isDebugEnabled() == true)
            {
                logger.debug("Creating thumbnail: There is already a thumbnail with the name '" + thumbnailName + "' (node=" + node.toString() + "; contentProperty=" + contentProperty.toString() + "; mimetype=" + mimetype);
            }
            
            // We can't continue because there is already an thumbnail with the given name for that content property
            throw new ThumbnailException(ERR_DUPLICATE_NAME);
        }
        
        NodeRef thumbnail = AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<NodeRef>()
        {
            public NodeRef doWork() throws Exception
            {
                NodeRef thumbnail;
                
                // Apply the thumbnailed aspect to the node if it doesn't already have it
                if (nodeService.hasAspect(node, ContentModel.ASPECT_THUMBNAILED) == false)
                {
                    // Ensure we do not update the 'modifier' due to thumbnail addition
                    behaviourFilter.disableBehaviour(node, ContentModel.ASPECT_AUDITABLE);
                    try
                    {
                        nodeService.addAspect(node, ContentModel.ASPECT_THUMBNAILED, null);
                    }
                    finally
                    {
                        behaviourFilter.enableBehaviour(node, ContentModel.ASPECT_AUDITABLE);
                    }
                }
                
                // Get the name of the thumbnail and add to properties map
                String thumbName = thumbnailName;
                Map<QName, Serializable> properties = new HashMap<QName, Serializable>(4);
                if (thumbName == null || thumbName.length() == 0)
                {
                    thumbName = GUID.generate();
                }
                else
                {
                    String thumbnailFileName = generateThumbnailFileName(thumbName, mimetype);
                    properties.put(ContentModel.PROP_NAME, thumbnailFileName);
                }
                properties.put(ContentModel.PROP_THUMBNAIL_NAME, thumbName);
                
                // Add the name of the content property
                properties.put(ContentModel.PROP_CONTENT_PROPERTY_NAME, contentProperty);
                
                // See if parent association details have been specified for the thumbnail
                if (assocDetails == null)
                {
                    // Create the thumbnail using the thumbnails child association
                    thumbnail = nodeService.createNode(
                            node, 
                            ContentModel.ASSOC_THUMBNAILS,
                            QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, thumbName), 
                            ContentModel.TYPE_THUMBNAIL, 
                            properties).getChildRef();
                }
                else
                {
                    // Create the thumbnail using the specified parent assoc details
                    thumbnail = nodeService.createNode(
                            assocDetails.getParent(),
                            assocDetails.getAssociationType(),
                            assocDetails.getAssociationName(),
                            ContentModel.TYPE_THUMBNAIL,
                            properties).getChildRef();

                    // Associate the new thumbnail to the source
                    nodeService.addChild(
                            node, 
                            thumbnail, 
                            ContentModel.ASSOC_THUMBNAILS, 
                            QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, thumbName));
                }
                
                // Get the content reader and writer for content nodes
                ContentReader reader = contentService.getReader(node, contentProperty);
                ContentWriter writer = contentService.getWriter(thumbnail, ContentModel.PROP_CONTENT, true);
                writer.setMimetype(mimetype);
                writer.setEncoding(reader.getEncoding());
                
                // Catch the failure to create the thumbnail
                if (contentService.isTransformable(reader, writer, transformationOptions) == false)
                {
                    if (logger.isDebugEnabled() == true)
                    {
                        logger.debug("Creating thumbnail: There is no transformer to generate the thumbnail required (node=" + node.toString() + "; contentProperty=" + contentProperty.toString() + "; mimetype=" + mimetype + ")");
                    }

                    // Throw exception indicating that the thumbnail could not be created
                    throw new ThumbnailException(MessageFormat.format(ERR_NO_CREATE, reader.getMimetype(), writer.getMimetype()));
                }
                else
                {
                    // Do the thumbnail transformation
                    contentService.transform(reader, writer, transformationOptions);
                }
                
                return thumbnail;
            }
        }, AuthenticationUtil.getSystemUserName());
        
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
     * @see org.alfresco.service.cmr.thumbnail.ThumbnailService#updateThumbnail(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.TransformationOptions)
     */
    public void updateThumbnail(final NodeRef thumbnail, final TransformationOptions transformationOptions)
    {
        if (logger.isDebugEnabled() == true)
        {
            logger.debug("Updating thumbnail (thumbnail=" + thumbnail.toString() + ")");
        }
        
        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
        {
            public Object doWork() throws Exception
            {
                // First check that we are dealing with a thumbnail
                if (ContentModel.TYPE_THUMBNAIL.equals(nodeService.getType(thumbnail)) == true)
                {
                    // Get the node that is the source of the thumbnail
                    NodeRef node = null;
                    List<ChildAssociationRef> parents = nodeService.getParentAssocs(thumbnail, ContentModel.ASSOC_THUMBNAILS, RegexQNamePattern.MATCH_ALL);
                    if (parents.size() == 0)
                    {
                        if (logger.isDebugEnabled() == true)
                        {
                            logger.debug("Updating thumbnail: The thumbnails parent cannot be found (thumbnail=" + thumbnail.toString() + ")");
                        }

                        throw new ThumbnailException(ERR_NO_PARENT);
                    }
                    else
                    {
                        node = parents.get(0).getParentRef();
                    }
                    
                    // Get the content property
                    QName contentProperty = (QName)nodeService.getProperty(thumbnail, ContentModel.PROP_CONTENT_PROPERTY_NAME);
                    
                    // Get the reader and writer            
                    ContentReader reader = contentService.getReader(node, contentProperty);
                    ContentWriter writer = contentService.getWriter(thumbnail, ContentModel.PROP_CONTENT, true);
                    
                    // Set the basic detail of the transformation options
                    transformationOptions.setSourceNodeRef(node);
                    transformationOptions.setSourceContentProperty(contentProperty);
                    transformationOptions.setTargetNodeRef(thumbnail);
                    transformationOptions.setTargetContentProperty(ContentModel.PROP_CONTENT);
                    
                    // Catch the failure to create the thumbnail
                    if (contentService.isTransformable(reader, writer, transformationOptions) == false)
                    {
                        if (logger.isDebugEnabled() == true)
                        {
                            logger.debug("Updating thumbnail: there is not transformer to update the thumbnail with (thumbnail=" + thumbnail.toString() + ")");
                        }

                        // Throw exception indicating that the thumbnail could not be created
                        throw new ThumbnailException(MessageFormat.format(ERR_NO_CREATE, reader.getMimetype(), writer.getMimetype()));
                    }
                    else
                    {
                        // Do the thumbnail transformation
                        contentService.transform(reader, writer, transformationOptions);
                    }
                }
                else
                {
                    if (logger.isDebugEnabled() == true)
                    {
                        logger.debug("Updating thumbnail: cannot update a thumbnail node that isn't the correct thumbnail type (thumbnail=" + thumbnail.toString() + ")");
                    }
                }
                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
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
        
        if (logger.isDebugEnabled() == true)
        {
            logger.debug("Getting thumbnail by name (nodeRef=" + node.toString() + "; contentProperty=" + contentProperty.toString() + "; thumbnailName=" + thumbnailName + ")");
        }
        
        // Check that the node has the thumbnailed aspect applied
        if (nodeService.hasAspect(node, ContentModel.ASPECT_THUMBNAILED) == true)
        {
            // Get all the thumbnails that match the thumbnail name
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
        
        if (logger.isDebugEnabled() == true)
        {
            logger.debug("Getting thumbnails (nodeRef=" + node.toString() + "; contentProperty=" + contentProperty.toString() + "; mimetype=" + mimetype + ")");
        }
        
        // Check that the node has the thumbnailed aspect applied
        if (nodeService.hasAspect(node, ContentModel.ASPECT_THUMBNAILED) == true)
        {
            // Get all the thumbnails that match the thumbnail name
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
     * If mimetype and transformation options are null then match is guarenteed
     * 
     * @param  thumbnail     thumbnail node reference
     * @param  mimetype      mimetype
     * @param  options       transformation options
     * @return boolean       true if the mimetype and options match the thumbnail metadata, false otherwise
     */
    private boolean matchMimetypeOptions(NodeRef thumbnail, String mimetype, TransformationOptions options)
    {
        boolean result = true;
        
        if (mimetype != null)
        {
            // Check the mimetype
            String thumbnailMimetype = ((ContentData) this.nodeService.getProperty(thumbnail, ContentModel.PROP_CONTENT)).getMimetype();

            if (mimetype.endsWith(SUBTYPES_POSTFIX))
            {
                String baseMimetype = mimetype.substring(0, mimetype.length() - SUBTYPES_POSTFIX.length());
                if (thumbnailMimetype == null || thumbnailMimetype.startsWith(baseMimetype) == false)
                {
                    result = false;
                }
            }
            else
            {
                if (mimetype.equals(thumbnailMimetype) == false)
                {
                    result = false;
                }
            }
        }
        
        if (result != false && options != null)
        {
            // TODO .. check for matching options here ...
        }
        
        return result;
    }
}
