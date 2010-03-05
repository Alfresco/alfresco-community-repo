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

/**
 * A helper class to convert {@link TransformationOptions transformationOptions} (a thumbnail-specific
 * class) to rendition-specific parameters and vice versa.
 * 
 * @author Neil McErlean
 */
public class ThumbnailRenditionConvertor
{
    public Map<String, Serializable> convert(TransformationOptions transformationOptions, ThumbnailParentAssociationDetails assocDetails)
    {
        Map<String, Serializable> parameters = new HashMap<String, Serializable>();

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
            }
        }

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
        
        //TODO src/target contentProp & nodeRef
        
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
                // Saved actions with int parameters seem to be coming back as Longs. TODO Investigate
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

            imgTrOpts.setResizeOptions(resizeOptions);
        }
        
        thDefn.setTransformationOptions(transformationOptions);

        return thDefn;
    }
}
