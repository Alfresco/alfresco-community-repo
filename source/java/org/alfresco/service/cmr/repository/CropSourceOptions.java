/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */

package org.alfresco.service.cmr.repository;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.repo.content.MimetypeMap;

/**
 * DTO used to store options for ImageMagick cropping.
 * 
 * @author Nick Smith, Ray Gauss II
 */
public class CropSourceOptions extends AbstractTransformationSourceOptions
{
    private int height = -1;
    private int width = -1;
    private int xOffset = 0;
    private int yOffset = 0;
    private boolean isPercentageCrop = false;
    private String gravity = null;
    
    @Override
    public boolean isApplicableForMimetype(String sourceMimetype)
    {
        return ((sourceMimetype != null && 
                sourceMimetype.startsWith(MimetypeMap.PREFIX_IMAGE)) ||
                super.isApplicableForMimetype(sourceMimetype));
    }

    /**
     * Gets the height of the cropped image. By default this is in pixels but if
     * <code>isPercentageCrop</code> is set to true then it changes to
     * percentage.
     * 
     * @return the height
     */
    public int getHeight()
    {
        return this.height;
    }

    /**
     * Sets the height of the cropped image. By default this is in pixels but if
     * <code>isPercentageCrop</code> is set to true then it changes to
     * percentage.
     * 
     * @param height the height to set
     */
    public void setHeight(int height)
    {
        this.height = height;
    }

    /**
     * Sets the width of the cropped image. By default this is in pixels but if
     * <code>isPercentageCrop</code> is set to true then it changes to
     * percentage.
     * 
     * @return the width
     */
    public int getWidth()
    {
        return this.width;
    }

    /**
     * Sets the width of the cropped image. By default this is in pixels but if
     * <code>isPercentageCrop</code> is set to true then it changes to
     * percentage.
     * 
     * @param width the width to set
     */
    public void setWidth(int width)
    {
        this.width = width;
    }

    /**
     * Gets the horizontal offset. By default this starts fromt he top-left
     * corner of the image and moves right, however the <code>gravity</code>
     * property can change this.
     * 
     * @return the xOffset
     */
    public int getXOffset()
    {
        return this.xOffset;
    }

    /**
     * Sets the horizontal offset. By default this starts fromt he top-left
     * corner of the image and moves right, however the <code>gravity</code>
     * property can change this.
     * 
     * @param xOffset the xOffset to set
     */
    public void setXOffset(int xOffset)
    {
        this.xOffset = xOffset;
    }

    /**
     * Gets the vertical offset. By default this starts fromt he top-left corner
     * of the image and moves down, however the <code>gravity</code> property
     * can change this.
     * 
     * @return the yOffset
     */
    public int getYOffset()
    {
        return this.yOffset;
    }

    /**
     * Sets the vertical offset. By default this starts fromt he top-left corner
     * of the image and moves down, however the <code>gravity</code> property
     * can change this.
     * 
     * @param yOffset the yOffset to set
     */
    public void setYOffset(int yOffset)
    {
        this.yOffset = yOffset;
    }

    /**
     * @return the isPercentageCrop
     */
    public boolean isPercentageCrop()
    {
        return this.isPercentageCrop;
    }

    /**
     * @param isPercentageCrop the isPercentageCrop to set
     */
    public void setPercentageCrop(boolean isPercentageCrop)
    {
        this.isPercentageCrop = isPercentageCrop;
    }

    /**
     * Sets the 'gravity' which determines how the offset is applied. It affects
     * both the origin of offset and the direction(s).
     * 
     * @param gravity the gravity to set
     */
    public void setGravity(String gravity)
    {
        this.gravity = gravity;
    }

    /**
     * Gets the 'gravity' which determines how the offset is applied. It affects
     * both the origin of offset and the direction(s).
     * 
     * @return the gravity
     */
    public String getGravity()
    {
        return this.gravity;
    }
    
    @Override
    public TransformationSourceOptionsSerializer getSerializer()
    {
        return CropSourceOptions.createSerializerInstance();
    }
    
    /**
     * Creates an instance of the options serializer
     * 
     * @return the options serializer
     */
    public static TransformationSourceOptionsSerializer createSerializerInstance()
    {
        return (new CropSourceOptions()).new CropSourceOptionsSerializer();
    }
    
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("CropSourceOptions [height=").append(this.height).append(", width=").append(this.width)
                    .append(", xOffset=").append(this.xOffset).append(", yOffset=").append(this.yOffset)
                    .append(", isPercentageCrop=").append(this.isPercentageCrop).append(", gravity=")
                    .append(this.gravity).append("]");
        return builder.toString();
    }
    
    /**
     * Serializer for crop source options
     */
    public class CropSourceOptionsSerializer implements TransformationSourceOptionsSerializer
    {
        
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

        @Override
        public TransformationSourceOptions deserialize(SerializedTransformationOptionsAccessor serializedOptions)
        {
            int newWidth = serializedOptions.getIntegerParam(PARAM_CROP_WIDTH, -1);
            int newHeight = serializedOptions.getIntegerParam(PARAM_CROP_HEIGHT, -1);
            if (newHeight == -1 && newWidth == -1)
            {
                return null;
            }

            int xOffset = serializedOptions.getIntegerParam(PARAM_CROP_X_OFFSET, 0);
            int yOffset = serializedOptions.getIntegerParam(PARAM_CROP_Y_OFFSET, 0);

            boolean isPercentCrop = serializedOptions.getParamWithDefault(PARAM_IS_PERCENT_CROP, false);
            String gravity = serializedOptions.getCheckedParam(PARAM_CROP_GRAVITY, String.class);

            CropSourceOptions cropOptions = new CropSourceOptions();
            cropOptions.setGravity(gravity);
            cropOptions.setHeight(newHeight);
            cropOptions.setPercentageCrop(isPercentCrop);
            cropOptions.setWidth(newWidth);
            cropOptions.setXOffset(xOffset);
            cropOptions.setYOffset(yOffset);
            return cropOptions;
        }
        
        @Override
        public void serialize(TransformationSourceOptions sourceOptions, 
                Map<String, Serializable> parameters)
        {
            if (parameters == null || sourceOptions == null)
                return;
            CropSourceOptions cropSourceOptions = (CropSourceOptions) sourceOptions;
            parameters.put(PARAM_CROP_WIDTH, cropSourceOptions.getWidth());
            parameters.put(PARAM_CROP_HEIGHT, cropSourceOptions.getHeight());
            parameters.put(PARAM_CROP_X_OFFSET, cropSourceOptions.getXOffset());
            parameters.put(PARAM_CROP_Y_OFFSET, cropSourceOptions.getYOffset());
            parameters.put(PARAM_CROP_GRAVITY, cropSourceOptions.getGravity());
            parameters.put(PARAM_IS_PERCENT_CROP, cropSourceOptions.isPercentageCrop());
        }
        
    }
    
}
