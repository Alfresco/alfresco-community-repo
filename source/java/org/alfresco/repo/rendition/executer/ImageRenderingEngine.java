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
    public static final String NAME = "imageRenderingEngine";

    // Resize params
    /**
     * This optional {@link Integer} or {@link Float} parameter specifies the
     * width of the image after resizing. This may be expressed as pixels or it
     * may represent a percentage of the original image width, depending on the
     * value of the PARAM_IS_PERCENT_RESIZE parameter. <br>
     * If no value is specified for this parameter then the width of the image
     * will be unchanged. <br>
     * If an image is being cropped and resized then the cropping happens first,
     * followed by resizing of the cropped image.
     */
    public static final String PARAM_RESIZE_WIDTH = "xsize";

    /**
     * This optional {@link Integer} or {@link Float} parameter specifies the
     * height of the image after resizing. This may be expressed as pixels or it
     * may represent a percentage of the original image width, depending on the
     * value of the PARAM_IS_PERCENT_RESIZE parameter. <br>
     * If no value is specified for this parameter then the height of the image
     * will be unchanged.<br>
     * If an image is being cropped and resized then the cropping happens
     * first, followed by resizing of the cropped image.
     */
    public static final String PARAM_RESIZE_HEIGHT = "ysize";

    /**
     * This optional {@link Boolean} flag parameter specifies how the
     * PARAM_RESIZE_HEIGHT and PARAM_RESIZE_WIDTH parameters are interpreted. If
     * this parameter is set to <code>true</code> then the rendition height and
     * width are represented as a percentage of the original image height and
     * width. If this parameter is set to <code>false</code> then the rendition
     * height and width are represented as pixels. This parameter defaults to
     * <code>false</code>.
     */
    public static final String PARAM_IS_PERCENT_RESIZE = "isAbsolute";

    /**
     * This optional {@link Boolean} flag parameter determines whether the
     * rendered image maintains its original aspect ratio or is stretched to fit
     * the specified height and width. <br>
     * If this parameter is <code>true</code> then the rendered image will
     * always maintain its aspect ratio and will be resized to best fit within
     * the given width and height. For example if an image starts at 100x200
     * pixels and it is resized to 50x50 pixels then the rendered image will
     * actually be 25x50 pixels. <br>
     * If this parameter is <code>false</code> then the image will be stretched
     * or compressed to fit the given height and width, regardless of the
     * original aspect ratio. <br>
     * This parameter defaults to <code>true</code>
     */
    public static final String PARAM_MAINTAIN_ASPECT_RATIO = "maintainAspectRatio";

    /**
     * This optional {@link Boolean} flag parameter specifies a mode for
     * dramatically shrinking large images in a performant way.<br>
     * If set to <code>true</code> the rendering process will be more performant
     * for large images but the rendered image will be of lower quality. <br>
     * If set to <code>false</code> the rendering process will take longer but
     * the resulting image will usually be of better quality.
     */
    public static final String PARAM_RESIZE_TO_THUMBNAIL = "resizeToThumbnail";
    
    /**
     * This optional {@link Boolean} flag parameter specifies whether image resizing
     * should produce an enlarged image, based on the resizing parameters and the size
     * of the original image. If true (the default), images may be enlarged.
     * If false, resize operations that would enlarge the image will instead produce a copy
     * of the original image at the same size.
     * @since 4.0
     */
    public static final String PARAM_ALLOW_ENLARGEMENT = "allowEnlargement";

    // Crop params
    /**
     * This optional {@link Integer} or {@link Float} parameter specifies the
     * width of the image after cropping. This may be expressed as pixels or it
     * may represent a percentage of the original image width, depending on the
     * value of the PARAM_IS_PERCENT_CROP parameter. <br>
     * If no value is specified for this parameter then the width of the image
     * will be unchanged. <br>
     * If an image is being cropped and resized then the cropping happens first,
     * followed by resizing of the cropped image.
     */
    public static final String PARAM_CROP_WIDTH = "crop_width";
    
    /**
     * This optional {@link Integer} or {@link Float} parameter specifies the
     * height of the image after cropping. This may be expressed as pixels or it
     * may represent a percentage of the original image width, depending on the
     * value of the PARAM_IS_PERCENT_CROP parameter. <br>
     * If no value is specified for this parameter then the width of the image
     * will be unchanged. <br>
     * If an image is being cropped and resized then the cropping happens first,
     * followed by resizing of the cropped image.
     */
    public static final String PARAM_CROP_HEIGHT = "crop_height";

    /**
     * This optional {@link Integer} parameter specifies the horizontal position
     * of the start point of the area to be cropped. By default this parameter
     * sets the distance, in pixels, from the left-hand edge of the image to the
     * start position of the crop area. By default a positive value will shift
     * the start-position to the right, while a negative value will shift the
     * start position to the left. Setting the PARAM_CROP_GRAVITY parameter may
     * change this, however.<br>
     * If this parameter is not set it is assumed to be 0.
     */
    public static final String PARAM_CROP_X_OFFSET = "crop_x";

    /**
     * This optional {@link Integer} parameter specifies the vertical position
     * of the start point of the area to be cropped. By default this parameter
     * sets the distance, in pixels, from the top edge of the image to the start
     * position of the crop area. By default a positive value will shift the
     * start-position downwards, while a negative value will shift the start
     * position upwards. Setting the PARAM_CROP_GRAVITY parameter may change
     * this, however.<br>
     * If this parameter is not set it is assumed to be 0.
     */
    public static final String PARAM_CROP_Y_OFFSET = "crop_y";

    /**
     * This optional {@link String} parameter determines the 'zero' position
     * from which offsets are measured and also determines the direction of
     * offsets. The allowed values of gravity are the four cardinal points
     * (North, East, etc.), the four ordinal points (NorhtWest, SouthEast, etc)
     * and Center. By default NorthWest gravity is used.
     * <p>
     * 
     * If an ordinal gravity is set then the point from which offsets originate
     * will be the appropriate corner. For example, NorthWest gravity would
     * originate at teh top-left corner while SouthWest origin would originate
     * at the bottom-left corner. Cardinal gravity sets the origin at the center
     * of the appropriate edge. Center origin sets the origin at the center of
     * the image.
     * <p>
     * 
     * Gravity also affects the direction of offsets and how the offset position
     * relates to the cropped image. For example, NorthWest gravity sets
     * positive horizontal offset direction to right, positive vertical
     * direction to down and sets the cropped image origin to the top-left
     * corner. Northerly gavities set the positive vertical direction to down.
     * Southerly gavities set teh positive vertical direction to up. Easterly
     * gavities set teh positive horizontal positive direction to left. Westerly
     * gavities set teh positive horizontal positive direction to right.
     * <p>
     * Some gravity values do not specify a horizontal or a vertical direction
     * explicitly. For example North does not specify a horizontal direction,
     * while Center does not specify either horizontal or vertical direction. In
     * thse cases the positive horizontal offset direction is always right and
     * the positive vertical offset direction is always down.
     * <p>
     * 
     * The gravity also affects how the cropped image relates to the offset
     * position. For example, NorthWest gravity causes the top-left corner of
     * the cropped area to be the offset position, while NorthEast gravity would
     * set the top-right corner of the cropped are to the offset position. When
     * a direction is not explicitly specified then the center of the cropped
     * area is placed at the offset position. For example, with North gravity
     * the horizontal position is unspecified so the cropped area would be
     * horizontally centered on the offset position, but the top edge of the
     * cropped area would be at the offset position. For Center gravity the
     * cropped area will be centered over the offset position both horizontally
     * and vertically.
     */
    public static final String PARAM_CROP_GRAVITY = "crop_gravity";
    
    /**
     * This optional {@link Boolean} flag parameter specifies how the
     * PARAM_CROP_HEIGHT and PARAM_CROP_WIDTH parameters are interpreted. If
     * this parameter is set to <code>true</code> then the cropped image height and
     * width are represented as a percentage of the original image height and
     * width. If this parameter is set to <code>false</code> then the rendition
     * height and width are represented as pixels. This parameter defaults to
     * <code>false</code>.
     */
    public static final String PARAM_IS_PERCENT_CROP = "percent_crop";

    /**
     * This optional {@link String} parameter specifies any additional
     * ImageMagick commands, that the user wishes to add. These commands are
     * appended after the various crop and resize options.
     */
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
        boolean allowEnlargement = context.getParamWithDefault(PARAM_ALLOW_ENLARGEMENT, true);

        ImageResizeOptions imageResizeOptions = new ImageResizeOptions();
        imageResizeOptions.setMaintainAspectRatio(maintainAspectRatio);
        imageResizeOptions.setWidth(newHeight);
        imageResizeOptions.setHeight(newWidth);
        imageResizeOptions.setPercentResize(isPercentResize);
        imageResizeOptions.setAllowEnlargement(allowEnlargement);
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
        paramList.add(new ParameterDefinitionImpl(PARAM_ALLOW_ENLARGEMENT, DataTypeDefinition.BOOLEAN, false,
                getParamDisplayLabel(PARAM_ALLOW_ENLARGEMENT)));
        
        
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