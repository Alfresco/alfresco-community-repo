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
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.transform.magick.ImageResizeOptions;
import org.alfresco.repo.content.transform.magick.ImageTransformationOptions;
import org.alfresco.repo.content.transform.swf.SWFTransformationOptions;
import org.alfresco.repo.rendition.executer.AbstractRenderingEngine;
import org.alfresco.repo.rendition.executer.ImageRenderingEngine;
import org.alfresco.repo.rendition.executer.ReformatRenderingEngine;
import org.alfresco.service.cmr.rendition.RenditionDefinition;
import org.alfresco.service.cmr.rendition.RenditionService;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.service.cmr.thumbnail.ThumbnailParentAssociationDetails;
import org.alfresco.service.cmr.thumbnail.ThumbnailService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * A helper class to convert {@link ThumbnailDefinition thumbnail definition} and
 * {@link TransformationOptions transformationOptions} (thumbnail-specific
 * classes) to rendition-specific parameters and vice versa.
 * 
 * The Thumbnail Service exposes parameters as simple data types on its various method
 * signatures. See for example ThumbnailDefinition.createThumbnail(...) or updateThumbnail(...).
 * The RenditionService replaces this approach with one based on the ActionService where
 * parameters are added as a Map on the Action/RenditionDefinition object.
 * 
 * @see ThumbnailService#createThumbnail(org.alfresco.service.cmr.repository.NodeRef, QName, String, TransformationOptions, String)
 * @see ThumbnailService#createThumbnail(org.alfresco.service.cmr.repository.NodeRef, QName, String, TransformationOptions, String, ThumbnailParentAssociationDetails)
 * @see ThumbnailService#updateThumbnail(org.alfresco.service.cmr.repository.NodeRef, TransformationOptions)
 * @see RenditionDefinition
 * 
 * @author Neil McErlean
 */
public class ThumbnailRenditionConvertor
{
    private RenditionService renditionService;
    
    public void setRenditionService(RenditionService renditionService)
    {
        this.renditionService = renditionService;
    }
    
    /** Given the specified {@link ThumbnailDefinition thumbnailDefinition} and
     * {@link ThumbnailParentAssociationDetails assocDetails},
     * create and return an equivalent {@link RenditionDefinition} object.
     * 
     * @param thumbnailDefinition
     * @param assocDetails
     * @return
     */
    public RenditionDefinition convert(ThumbnailDefinition thumbnailDefinition, ThumbnailParentAssociationDetails assocDetails)
    {
        // We must always have a valid name for a thumbnail definition
        if (thumbnailDefinition == null || thumbnailDefinition.getName() == null
                || thumbnailDefinition.getName().trim().length() == 0)
        {
            throw new IllegalArgumentException("Thumbnail Definition and Name must be non-null and non-empty.");
        }
        
        TransformationOptions transformationOptions = thumbnailDefinition.getTransformationOptions();
        Map<String, Serializable> parameters = this.convert(transformationOptions, assocDetails);
        
        // Extract parameters defined directly within the ThumbnailDefinition object.
        putParameterIfNotNull(AbstractRenderingEngine.PARAM_MIME_TYPE, thumbnailDefinition.getMimetype(), parameters);
        putParameterIfNotNull(AbstractRenderingEngine.PARAM_PLACEHOLDER_RESOURCE_PATH, thumbnailDefinition.getPlaceHolderResourcePath(), parameters);
        putParameterIfNotNull(AbstractRenderingEngine.PARAM_RUN_AS, thumbnailDefinition.getRunAs(), parameters);
        
        QName namespacedRenditionName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, thumbnailDefinition.getName());
        
        // The built-in RenditionDefinitions are all non-composites.
        // They are either "imageRenderingEngine" or "reformat"
        boolean isImageThumbnail = isImageBasedRendition(thumbnailDefinition);
        
        String renderingEngineName = isImageThumbnail ? ImageRenderingEngine.NAME : ReformatRenderingEngine.NAME;
        
        RenditionDefinition renditionDef = renditionService.createRenditionDefinition(namespacedRenditionName, renderingEngineName);
        for (String paramName : parameters.keySet())
        {
            renditionDef.setParameterValue(paramName, parameters.get(paramName));
        }
        
        return renditionDef;
    }
    
    /**
     * This method examines the various data values on the thumbnail definition and
     * works out if it is an 'image' rendition or a 'reformat' rendition
     * @param thumbnailDefinition
     * @return <code>true</code> for an image-based RenditionDefinition, else <code>false</code>
     */
    private boolean isImageBasedRendition(ThumbnailDefinition thumbnailDefinition)
    {
        final TransformationOptions transformationOptions = thumbnailDefinition.getTransformationOptions();
        
        return transformationOptions != null && transformationOptions instanceof ImageTransformationOptions;
    }
    
    /** Given the specified {@link TransformationOptions transformationOptions} and
     * {@link ThumbnailParentAssociationDetails assocDetails},
     * create and return a parameter Map which contains the equivalent {@link RenditionDefinition}
     * configuration.
     * 
     * @param transformationOptions
     * @param assocDetails
     * @return
     */
    public Map<String, Serializable> convert(TransformationOptions transformationOptions, ThumbnailParentAssociationDetails assocDetails)
    {
        Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        
        // All TransformationOptions-based renditions are considered to be "thumbnails".
        // Therefore they should be created with a node type of cm:thumbnail
        parameters.put(RenditionService.PARAM_RENDITION_NODETYPE, ContentModel.TYPE_THUMBNAIL);

        // parameters common to all transformations
        
        putParameterIfNotNull(AbstractRenderingEngine.PARAM_SOURCE_CONTENT_PROPERTY, transformationOptions.getSourceContentProperty(), parameters);
        putParameterIfNotNull(AbstractRenderingEngine.PARAM_TARGET_CONTENT_PROPERTY, transformationOptions.getTargetContentProperty(), parameters);
        putParameterIfNotNull(RenditionService.PARAM_DESTINATION_NODE, transformationOptions.getTargetNodeRef(), parameters);

//        putParameterIfNotNull(ImageRenderingEngine.PARAM_ASSOC_NAME, assocDetails.getAssociationName(), parameters);
//        putParameterIfNotNull(ImageRenderingEngine.PARAM_ASSOC_TYPE, assocDetails.getAssociationType(), parameters);

        if (transformationOptions instanceof SWFTransformationOptions)
        {
            SWFTransformationOptions swfTransformationOptions = (SWFTransformationOptions)transformationOptions;
            putParameterIfNotNull(ReformatRenderingEngine.PARAM_FLASH_VERSION, swfTransformationOptions.getFlashVersion(), parameters);
        }
        else if (transformationOptions instanceof ImageTransformationOptions)
        {
            ImageTransformationOptions imTransformationOptions = (ImageTransformationOptions)transformationOptions;
            putParameterIfNotNull(ImageRenderingEngine.PARAM_COMMAND_OPTIONS, imTransformationOptions.getCommandOptions(), parameters);
            putParameterIfNotNull(ImageRenderingEngine.PARAM_AUTO_ORIENTATION, imTransformationOptions.isAutoOrient(), parameters);
            
            ImageResizeOptions imgResizeOptions = imTransformationOptions.getResizeOptions();
            if (imgResizeOptions != null)
            {
                int width = imgResizeOptions.getWidth();
                parameters.put(ImageRenderingEngine.PARAM_RESIZE_WIDTH, width);
                
                int height = imgResizeOptions.getHeight();
                parameters.put(ImageRenderingEngine.PARAM_RESIZE_HEIGHT, height);

                boolean maintainAspectRatio = imgResizeOptions.isMaintainAspectRatio();
                parameters.put(ImageRenderingEngine.PARAM_MAINTAIN_ASPECT_RATIO, maintainAspectRatio);

                boolean percentResize = imgResizeOptions.isPercentResize();
                parameters.put(ImageRenderingEngine.PARAM_IS_PERCENT_RESIZE, percentResize);

                boolean resizeToThumbnail = imgResizeOptions.isResizeToThumbnail();
                parameters.put(ImageRenderingEngine.PARAM_RESIZE_TO_THUMBNAIL, resizeToThumbnail);
                
                boolean allowEnlargement = imgResizeOptions.getAllowEnlargement();
                parameters.put(ImageRenderingEngine.PARAM_ALLOW_ENLARGEMENT, allowEnlargement);
            }
        }
        // TODO Handle RuntimeExecutableTransformationOptions
        return parameters;
    }
    
    private void putParameterIfNotNull(String paramName, Serializable paramValue, Map<String, Serializable> params)
    {
        if (paramValue != null)
        {
            params.put(paramName, paramValue);
        }
    }
    
    public ThumbnailDefinition convert(RenditionDefinition renditionDefinition)
    {
        ThumbnailDefinition thDefn = new ThumbnailDefinition();
        
        Map<String, Serializable> params = renditionDefinition.getParameterValues();
        
        //parameters common to all the built-in thumbnail definitions
        Serializable mimeTypeParam = params.get(AbstractRenderingEngine.PARAM_MIME_TYPE);
        thDefn.setMimetype((String) mimeTypeParam);
        thDefn.setName(renditionDefinition.getRenditionName().getLocalName());
        
        Serializable placeHolderResourcePathParam = params.get(AbstractRenderingEngine.PARAM_PLACEHOLDER_RESOURCE_PATH);
        if (placeHolderResourcePathParam != null)
        {
            thDefn.setPlaceHolderResourcePath((String)placeHolderResourcePathParam);
        }
        
        TransformationOptions transformationOptions = null;
        Serializable flashVersion = renditionDefinition.getParameterValue(ReformatRenderingEngine.PARAM_FLASH_VERSION);
        if (flashVersion != null)
        {
            // Thumbnails based on SWFTransformationOptions
            transformationOptions = new SWFTransformationOptions();
            SWFTransformationOptions swfTranOpts = (SWFTransformationOptions)transformationOptions;
            swfTranOpts.setFlashVersion((String)flashVersion);
        }
        else
        {
            // Thumbnails based on ImageTransformationOptions
            transformationOptions = new ImageTransformationOptions();
            ImageTransformationOptions imgTrOpts = (ImageTransformationOptions)transformationOptions;

            ImageResizeOptions resizeOptions = new ImageResizeOptions();
            Serializable xsize = renditionDefinition.getParameterValue(ImageRenderingEngine.PARAM_RESIZE_WIDTH);
            if (xsize != null)
            {
                resizeOptions.setWidth(((Long) xsize).intValue());
            }
            
            Serializable ysize = renditionDefinition.getParameterValue(ImageRenderingEngine.PARAM_RESIZE_HEIGHT);
            if (ysize != null)
            {
                resizeOptions.setHeight(((Long) ysize).intValue());
            }

            Serializable maintainAspectRatio = renditionDefinition.getParameterValue(ImageRenderingEngine.PARAM_MAINTAIN_ASPECT_RATIO);
            if (maintainAspectRatio != null)
            {
                resizeOptions.setMaintainAspectRatio((Boolean) maintainAspectRatio);
            }

            Serializable resizeToThumbnail = renditionDefinition.getParameterValue(ImageRenderingEngine.PARAM_RESIZE_TO_THUMBNAIL);
            if (resizeToThumbnail != null)
            {
                resizeOptions.setResizeToThumbnail((Boolean) resizeToThumbnail);
            }

            Serializable allowEnlargement = renditionDefinition.getParameterValue(ImageRenderingEngine.PARAM_ALLOW_ENLARGEMENT);
            if (allowEnlargement != null)
            {
                resizeOptions.setAllowEnlargement((Boolean) allowEnlargement);
            }

            imgTrOpts.setResizeOptions(resizeOptions);
        }
        
        thDefn.setTransformationOptions(transformationOptions);

        return thDefn;
    }
}
