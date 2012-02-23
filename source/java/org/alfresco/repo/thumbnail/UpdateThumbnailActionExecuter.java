/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.repo.thumbnail;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.rendition.RenditionService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.thumbnail.ThumbnailService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Update thumbnail action executer.
 * 
 * NOTE:  This action is used to facilitate the async update of thumbnails.  It is not intended for general usage.
 * 
 * @author Roy Wetherall
 * @author Neil McErlean
 * @author Ph Dubois (optional thumbnail creation by mimetype and in general)
 */
public class UpdateThumbnailActionExecuter extends ActionExecuterAbstractBase
{
    /** Logger */
    private static Log logger = LogFactory.getLog(UpdateThumbnailActionExecuter.class);

    /** Rendition Service */
    private RenditionService renditionService;
    
    /** Thumbnail Service */
    private ThumbnailService thumbnailService;
    
    /** Node Service */
    private NodeService nodeService;
    
    /** Property turns on and off all thumbnail creation */
    private boolean generateThumbnails = true;

    // Size limitations indexed by mime type for thumbnail creation
    private HashMap<String,Long> mimetypeMaxSourceSizeKBytes;

    /** Action name and parameters */
    public static final String NAME = "update-thumbnail";
    public static final String PARAM_CONTENT_PROPERTY = "content-property";
    public static final String PARAM_THUMBNAIL_NODE = "thumbnail-node";
    
    /**
     * Injects the rendition service.
     * 
     * @param renditionService the rendition service.
     */
    public void setRenditionService(RenditionService renditionService) 
    {
        this.renditionService = renditionService;
    }

    /**
     * Set the thumbnail service
     * 
     * @param thumbnailService  the thumbnail service
     */
    public void setThumbnailService(ThumbnailService thumbnailService)
    {
        this.thumbnailService = thumbnailService;
    }
    
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
     * Set the maximum size for each mimetype above which thumbnails are not created.
     * @param mimetypeMaxSourceSizeKBytes map of mimetypes to max source sizes.
     */
    public void setMimetypeMaxSourceSizeKBytes(HashMap<String, Long> mimetypeMaxSourceSizeKBytes)
    {
        this.mimetypeMaxSourceSizeKBytes = mimetypeMaxSourceSizeKBytes;
    }
    
    /**
     * Enable thumbnail creation at all regardless of mimetype.
     * @param generateThumbnails a {@code false} value turns off all thumbnail creation.
     */
    public void setGenerateThumbnails(boolean generateThumbnails)
    {
        this.generateThumbnails = generateThumbnails;
    }
    
    /**
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
    {
        // Check if thumbnailing is generally disabled
        if (!generateThumbnails)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Thumbnail transformations are not enabled");
            }
            return;
        }
        
        // Get the thumbnail
        NodeRef thumbnailNodeRef = (NodeRef)action.getParameterValue(PARAM_THUMBNAIL_NODE);
        if (thumbnailNodeRef == null)
        {
            thumbnailNodeRef = actionedUponNodeRef;
        }
        
        if (this.nodeService.exists(thumbnailNodeRef) == true &&
                renditionService.isRendition(thumbnailNodeRef))
        {            
            // Get the thumbnail Name
            ChildAssociationRef parent = renditionService.getSourceNode(thumbnailNodeRef);
            String thumbnailName = parent.getQName().getLocalName();
            
            // Get the details of the thumbnail
            ThumbnailRegistry registry = this.thumbnailService.getThumbnailRegistry();
            ThumbnailDefinition details = registry.getThumbnailDefinition(thumbnailName);
            if (details == null)
            {
                throw new AlfrescoRuntimeException("The thumbnail name '" + thumbnailName + "' is not registered");
            }
            
            // Get the content property
            QName contentProperty = (QName)action.getParameterValue(PARAM_CONTENT_PROPERTY);
            if (contentProperty == null)
            {
                contentProperty = ContentModel.PROP_CONTENT;
            }
            
            Serializable contentProp = nodeService.getProperty(actionedUponNodeRef, contentProperty);
            if (contentProp == null)
            {
                logger.info("Creation of thumbnail, null content for " + details.getName());
                return;
            }

            if(contentProp instanceof ContentData)
            {
                ContentData content = (ContentData)contentProp;
                String mimetype = content.getMimetype();
                if (mimetypeMaxSourceSizeKBytes != null)
                {
                    Long maxSourceSizeKBytes = mimetypeMaxSourceSizeKBytes.get(mimetype);
                    if (maxSourceSizeKBytes != null && maxSourceSizeKBytes >= 0 && maxSourceSizeKBytes < (content.getSize()/1024L))
                    {
                        logger.debug("Unable to create thumbnail '" + details.getName() + "' for " +
                                mimetype + " as content is too large ("+(content.getSize()/1024L)+"K > "+maxSourceSizeKBytes+"K)");
                        return; //avoid transform
                    }
                }
            }
            // Create the thumbnail
            this.thumbnailService.updateThumbnail(thumbnailNodeRef, details.getTransformationOptions());
        }
    }

    /**
     * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#addParameterDefinitions(java.util.List)
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {
        paramList.add(new ParameterDefinitionImpl(PARAM_CONTENT_PROPERTY, DataTypeDefinition.QNAME, false, getParamDisplayLabel(PARAM_CONTENT_PROPERTY)));
        paramList.add(new ParameterDefinitionImpl(PARAM_THUMBNAIL_NODE, DataTypeDefinition.QNAME, false, getParamDisplayLabel(PARAM_THUMBNAIL_NODE)));
    }
}
