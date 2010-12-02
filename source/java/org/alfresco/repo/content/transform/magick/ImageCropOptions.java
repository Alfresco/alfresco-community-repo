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

package org.alfresco.repo.content.transform.magick;

/**
 * DTO used to store options for ImageMagick cropping.
 * 
 * @author Nick Smith
 */
public class ImageCropOptions
{
    private int height = -1;
    private int width = -1;
    private int xOffset = 0;
    private int yOffset = 0;
    private boolean isPercentageCrop = false;
    private String gravity = null;

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
}
