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

package org.alfresco.repo.rendition.executer;

import java.util.Collection;

import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.content.transform.magick.ImageCropOptions;
import org.alfresco.repo.content.transform.magick.ImageResizeOptions;
import org.alfresco.repo.content.transform.magick.ImageTransformationOptions;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.rendition.RenditionService;
import org.alfresco.service.cmr.repository.TransformationOptions;

/**
 * This class is the implementation of the {@link RenditionService}'s
 * "imageRenderingEngine" rendering engine. This action renders a piece of
 * content in the same MIME type as its source node, having been rescaled as
 * requested.
 * 
 * @author Neil McErlean
 * @since 3.3
 */
public class ImageRenderingEngine extends AbstractTransformationRenderingEngine
{
    // TODO This rendering engine should only take input of MIME type image/*
    // However, we'll defer the addition of an EngineInputFilter until after
    // Sprint 3.

    public static final String NAME = "imageRenderingEngine";

    // Resize params
    public static final String PARAM_RESIZE_WIDTH = "xsize";
    public static final String PARAM_RESIZE_HEIGHT = "ysize";
    public static final String PARAM_IS_PERCENT_RESIZE = "isAbsolute";
    public static final String PARAM_MAINTAIN_ASPECT_RATIO = "maintainAspectRatio";
    public static final String PARAM_RESIZE_TO_THUMBNAIL = "resizeToThumbnail";

    // Crop params
    public static final String PARAM_CROP_WIDTH = "crop_width";
    public static final String PARAM_CROP_HEIGHT = "crop_height";
    public static final String PARAM_CROP_X_OFFSET = "crop_x";
    public static final String PARAM_CROP_Y_OFFSET = "crop_y";
    public static final String PARAM_CROP_GRAVITY = "crop_gravity";
    public static final String PARAM_IS_PERCENT_CROP = "percent_crop";

    public static final String PARAM_COMMAND_OPTIONS = "commandOptions";

    /*
     * @seeorg.alfresco.repo.rendition.executer.ReformatRenderingEngine#
     * getTransformOptions
     * (org.alfresco.repo.rendition.executer.AbstractRenderingEngine
     * .RenderingContext)
     */
    @Override
    protected TransformationOptions getTransformOptions(RenderingContext context)
    {
        String commandOptions = context.getCheckedParam(PARAM_COMMAND_OPTIONS, String.class);
        ImageResizeOptions imageResizeOptions = getImageResizeOptions(context);
        ImageCropOptions cropOptions = getImageCropOptions(context);

        ImageTransformationOptions imageTransformationOptions = new ImageTransformationOptions();
        imageTransformationOptions.setResizeOptions(imageResizeOptions);
        imageTransformationOptions.setCropOptions(cropOptions);
        if (commandOptions != null)
        {
            imageTransformationOptions.setCommandOptions(commandOptions);
        }
        return imageTransformationOptions;
    }

    /*
     * @seeorg.alfresco.repo.rendition.executer.ReformatRenderingEngine#
     * getTargetMimeType
     * (org.alfresco.repo.rendition.executer.AbstractRenderingEngine
     * .RenderingContext)
     */
    @Override
    protected String getTargetMimeType(RenderingContext context)
    {
        String sourceMimeType = context.makeContentReader().getMimetype();
        return context.getParamWithDefault(PARAM_MIME_TYPE, sourceMimeType);
    }

    private ImageResizeOptions getImageResizeOptions(RenderingContext context)
    {
        int newHeight = context.getIntegerParam(PARAM_RESIZE_WIDTH, -1);
        int newWidth = context.getIntegerParam(PARAM_RESIZE_HEIGHT, -1);
        if (newHeight == -1 && newWidth == -1)
        {
            return null; // Image is not being resized!
        }
        boolean isPercentResize = context.getParamWithDefault(PARAM_IS_PERCENT_RESIZE, false);
        boolean maintainAspectRatio = context.getParamWithDefault(PARAM_MAINTAIN_ASPECT_RATIO, false);

        ImageResizeOptions imageResizeOptions = new ImageResizeOptions();
        imageResizeOptions.setMaintainAspectRatio(maintainAspectRatio);
        imageResizeOptions.setWidth(newHeight);
        imageResizeOptions.setHeight(newWidth);
        imageResizeOptions.setPercentResize(isPercentResize);
        return imageResizeOptions;
    }

    private ImageCropOptions getImageCropOptions(RenderingContext context)
    {
        int newWidth = context.getIntegerParam(PARAM_CROP_WIDTH, -1);
        int newHeight = context.getIntegerParam(PARAM_CROP_HEIGHT, -1);
        if (newHeight == -1 && newWidth == -1)
        {
            return null;
        }

        int xOffset = context.getIntegerParam(PARAM_CROP_X_OFFSET, 0);
        int yOffset = context.getIntegerParam(PARAM_CROP_Y_OFFSET, 0);

        boolean isPercentCrop = context.getParamWithDefault(PARAM_IS_PERCENT_CROP, false);
        String gravity = context.getCheckedParam(PARAM_CROP_GRAVITY, String.class);

        ImageCropOptions cropOptions = new ImageCropOptions();
        cropOptions.setGravity(gravity);
        cropOptions.setHeight(newHeight);
        cropOptions.setPercentageCrop(isPercentCrop);
        cropOptions.setWidth(newWidth);
        cropOptions.setXOffset(xOffset);
        cropOptions.setYOffset(yOffset);
        return cropOptions;
    }
    
    @Override
    protected void checkParameterValues(Action action)
    {
        // Some numerical parameters should not be zero or negative.
        checkNumericalParameterIsPositive(action, PARAM_RESIZE_WIDTH);
        checkNumericalParameterIsPositive(action, PARAM_RESIZE_HEIGHT);
        checkNumericalParameterIsPositive(action, PARAM_CROP_HEIGHT);
        checkNumericalParameterIsPositive(action, PARAM_CROP_WIDTH);
        
        // Target mime type should only be an image MIME type
        String mimeTypeParam = (String)action.getParameterValue(PARAM_MIME_TYPE);
        if (mimeTypeParam != null && !mimeTypeParam.startsWith("image"))
        {
            StringBuilder msg = new StringBuilder();
            msg.append("Parameter ").append(PARAM_MIME_TYPE)
                .append(" had illegal non-image MIME type: ").append(mimeTypeParam);
            throw new IllegalArgumentException(msg.toString());
        }
    }

    /**
     * This method checks that if the specified parameter is non-null, that it has a
     * positive numerical value. That is it is non-zero and positive.
     * 
     * @param action
     * @param numericalParamName must be an instance of java.lang.Number or null.
     */
    private void checkNumericalParameterIsPositive(Action action, String numericalParamName)
    {
        Number param = (Number)action.getParameterValue(numericalParamName);
        if (param != null && param.longValue() <= 0)
        {
            StringBuilder msg = new StringBuilder();
            msg.append("Parameter ").append(numericalParamName)
                .append(" had illegal non-positive value: ").append(param.intValue());
            throw new IllegalArgumentException(msg.toString());
        }
    }


    /*
     * @seeorg.alfresco.repo.rendition.executer.AbstractRenderingEngine#
     * getParameterDefinitions()
     */
    @Override
    protected Collection<ParameterDefinition> getParameterDefinitions()
    {
        Collection<ParameterDefinition> paramList = super.getParameterDefinitions();
        
        //Resize Params
        paramList.add(new ParameterDefinitionImpl(PARAM_RESIZE_WIDTH, DataTypeDefinition.INT, false,
                    getParamDisplayLabel(PARAM_RESIZE_WIDTH)));
        paramList.add(new ParameterDefinitionImpl(PARAM_RESIZE_HEIGHT, DataTypeDefinition.INT, false,
                    getParamDisplayLabel(PARAM_RESIZE_HEIGHT)));
        paramList.add(new ParameterDefinitionImpl(PARAM_IS_PERCENT_RESIZE, DataTypeDefinition.BOOLEAN, false,
                    getParamDisplayLabel(PARAM_IS_PERCENT_RESIZE)));
        paramList.add(new ParameterDefinitionImpl(PARAM_MAINTAIN_ASPECT_RATIO, DataTypeDefinition.BOOLEAN, false,
                    getParamDisplayLabel(PARAM_MAINTAIN_ASPECT_RATIO)));
        paramList.add(new ParameterDefinitionImpl(PARAM_RESIZE_TO_THUMBNAIL, DataTypeDefinition.BOOLEAN, false,
                    getParamDisplayLabel(PARAM_RESIZE_TO_THUMBNAIL)));
        
        //Crop Params
        paramList.add(new ParameterDefinitionImpl(PARAM_CROP_GRAVITY, DataTypeDefinition.TEXT, false,
                    getParamDisplayLabel(PARAM_CROP_GRAVITY)));
        paramList.add(new ParameterDefinitionImpl(PARAM_CROP_HEIGHT, DataTypeDefinition.INT, false,
                    getParamDisplayLabel(PARAM_CROP_HEIGHT)));
        paramList.add(new ParameterDefinitionImpl(PARAM_CROP_WIDTH, DataTypeDefinition.INT, false,
                    getParamDisplayLabel(PARAM_CROP_WIDTH)));
        paramList.add(new ParameterDefinitionImpl(PARAM_CROP_X_OFFSET, DataTypeDefinition.INT, false,
                    getParamDisplayLabel(PARAM_CROP_X_OFFSET)));
        paramList.add(new ParameterDefinitionImpl(PARAM_CROP_Y_OFFSET, DataTypeDefinition.INT, false,
                    getParamDisplayLabel(PARAM_CROP_Y_OFFSET)));
        paramList.add(new ParameterDefinitionImpl(PARAM_IS_PERCENT_CROP, DataTypeDefinition.BOOLEAN, false,
                    getParamDisplayLabel(PARAM_IS_PERCENT_CROP)));
        
        paramList.add(new ParameterDefinitionImpl(PARAM_COMMAND_OPTIONS, DataTypeDefinition.TEXT, false,
                    getParamDisplayLabel(PARAM_COMMAND_OPTIONS)));
        return paramList;
    }
}