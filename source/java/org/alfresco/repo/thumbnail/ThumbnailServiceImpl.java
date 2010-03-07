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
package org.alfresco.repo.thumbnail;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.transform.magick.ImageTransformationOptions;
import org.alfresco.repo.content.transform.swf.SWFTransformationOptions;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.rendition.executer.ImageRenderingEngine;
import org.alfresco.repo.rendition.executer.ReformatRenderingEngine;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.rendition.RenditionDefinition;
import org.alfresco.service.cmr.rendition.RenditionService;
import org.alfresco.service.cmr.rendition.RenditionServiceException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.service.cmr.thumbnail.ThumbnailException;
import org.alfresco.service.cmr.thumbnail.ThumbnailParentAssociationDetails;
import org.alfresco.service.cmr.thumbnail.ThumbnailService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.ParameterCheck;

/**
 * @author Roy Wetherall
 * @author Neil McErlean
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
    
    /** Rendition service */
    private RenditionService renditionService;
    
    /**
     * Set the rendition service.
     * 
     * @param renditionService
     */
    public void setRenditionService(RenditionService renditionService)
    {
        this.renditionService = renditionService;
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
            
            // We can't continue because there is already a thumbnail with the given name for that content property
            throw new ThumbnailException(ERR_DUPLICATE_NAME);
        }
        
        ChildAssociationRef thumbnailRef = AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<ChildAssociationRef>()
        {
            public ChildAssociationRef doWork() throws Exception
            {
                // Need a variable scoped to this inner class in order to assign a new value to it.
                String localThumbnailName = thumbnailName;
                // Get the name of the thumbnail and add to properties map
                if (localThumbnailName == null || localThumbnailName.length() == 0)
                {
                    localThumbnailName = GUID.generate();
                }
                
                // We're prepending the cm namespace here.
                QName thumbnailQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, localThumbnailName);

                RenditionDefinition renderingAction = renditionService.loadRenditionDefinition(thumbnailQName);
                if (renderingAction == null)
                {
                    // If the provided thumbnailName does not map to a built-in rendition definition
                    // then we must create a dynamic rendition definition for this thumbnail.
                    // To do this we must have a renderingEngineName.
                    //
                    // The transformation will either be a imageRenderingEngine or a reformat (pdf2swf)
                    String renderingEngineName = getRenderingEngineNameFor(transformationOptions);

                    renderingAction = renditionService.createRenditionDefinition(thumbnailQName, renderingEngineName);
                }
                Map<String, Serializable> params = thumbnailRegistry.getThumbnailRenditionConvertor().convert(transformationOptions, assocDetails);
                for (String key : params.keySet())
                {
                    renderingAction.setParameterValue(key, params.get(key));
                }

                
                ChildAssociationRef chAssRef = null;
                try
                {
                    chAssRef = renditionService.render(node, renderingAction);
                } catch (RenditionServiceException rsx)
                {
                    throw new ThumbnailException(rsx.getMessage(), rsx);
                }

                return chAssRef;
            }
        }, AuthenticationUtil.getSystemUserName());
        
        // Return the created thumbnail
        return getThumbnailNode(thumbnailRef);
    }
    
    private String getRenderingEngineNameFor(TransformationOptions options)
    {
        if (options instanceof ImageTransformationOptions)
        {
            return ImageRenderingEngine.NAME;
        }
        else if (options instanceof SWFTransformationOptions)
        {
            return ReformatRenderingEngine.NAME;
        }
        else
        {
            // TODO What can we do here? Can we treat this as an error?
            // Isn't this a 'standard' TransformationOptions?
            return "";
        }
    }

    /**
     * This method returns the NodeRef for the thumbnail, using the ChildAssociationRef
     * that links the sourceNode to its associated Thumbnail node.
     * 
     * @param thumbnailRef the ChildAssociationRef containing the child NodeRef.
     * @return the NodeRef of the thumbnail itself.
     */
    public NodeRef getThumbnailNode(ChildAssociationRef thumbnailRef)
    {
        return thumbnailRef.getChildRef();
    }

    /**
     * @see org.alfresco.service.cmr.thumbnail.ThumbnailService#updateThumbnail(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.TransformationOptions)
     */
    public void updateThumbnail(final NodeRef thumbnail, final TransformationOptions transformationOptions)
    {
        // Seeing as how this always gives its own options, maybe this should be a delete and recreate?
        if (logger.isDebugEnabled() == true)
        {
            logger.debug("Updating thumbnail (thumbnail=" + thumbnail.toString() + ")");
        }
        
        // First check that we are dealing with a rendition object
        if (renditionService.isRendition(thumbnail))
        {
            // Get the node that is the source of the thumbnail
            ChildAssociationRef parentAssoc = renditionService.getSourceNode(thumbnail);
            
            if (parentAssoc == null)
            {
                if (logger.isDebugEnabled() == true)
                {
                    logger.debug("Updating thumbnail: The thumbnails parent cannot be found (thumbnail=" + thumbnail.toString() + ")");
                }
                throw new ThumbnailException(ERR_NO_PARENT);
            }
            
            final QName renditionAssociationName = parentAssoc.getQName();
            NodeRef sourceNode = parentAssoc.getParentRef();

            // Get the content property
            QName contentProperty = (QName)nodeService.getProperty(thumbnail, ContentModel.PROP_CONTENT_PROPERTY_NAME);

            // Set the basic detail of the transformation options
            transformationOptions.setSourceNodeRef(sourceNode);
            transformationOptions.setSourceContentProperty(contentProperty);
            transformationOptions.setTargetContentProperty(ContentModel.PROP_CONTENT);

            // Do the thumbnail transformation
            RenditionDefinition rendDefn = renditionService.loadRenditionDefinition(renditionAssociationName);
            if (rendDefn == null)
            {
                String renderingEngineName = getRenderingEngineNameFor(transformationOptions);

                rendDefn = renditionService.createRenditionDefinition(parentAssoc.getQName(), renderingEngineName);
            }
            Map<String, Serializable> params = thumbnailRegistry.getThumbnailRenditionConvertor().convert(transformationOptions, null);
            for (String key : params.keySet())
            {
                rendDefn.setParameterValue(key, params.get(key));
            }
            
            renditionService.render(sourceNode, rendDefn);
        }
        else
        {
            if (logger.isDebugEnabled() == true)
            {
                logger.debug("Updating thumbnail: cannot update a thumbnail node that isn't the correct thumbnail type (thumbnail=" + thumbnail.toString() + ")");
            }
        }
    }

    /**
     * @see org.alfresco.service.cmr.thumbnail.ThumbnailService#getThumbnailByName(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName, java.lang.String)
     */
    public NodeRef getThumbnailByName(NodeRef node, QName contentProperty, String thumbnailName)
    {
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
        
        // Thumbnails have a cm: prefix.
        QName namespacedThumbnailName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, thumbnailName);
        
        ChildAssociationRef existingRendition = renditionService.getRenditionByName(node, namespacedThumbnailName);
        NodeRef thumbnail = null;
        
        // Check the child to see if it matches the content property we are concerned about.
        // We can assume there will only ever be one per content property since createThumbnail enforces this.
        if (existingRendition != null)
        {
            NodeRef child = existingRendition.getChildRef();
            Serializable contentPropertyName = this.nodeService.getProperty(child, ContentModel.PROP_CONTENT_PROPERTY_NAME);
            if (contentProperty.equals(contentPropertyName) == true)
            {
                thumbnail = child;
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
        
        List<ChildAssociationRef> renditions = this.renditionService.getRenditions(node);
        
        for (ChildAssociationRef assoc : renditions)
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
        
        //TODO Ensure this doesn't return non-thumbnail renditions.
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
