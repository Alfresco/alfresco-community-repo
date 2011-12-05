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

import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.repository.TransformationOptions;

/**
 * Image transformation options
 * 
 * @author Roy Wetherall
 */
public class ImageTransformationOptions extends TransformationOptions
{
    public static final String OPT_COMMAND_OPTIONS = "commandOptions";
    public static final String OPT_IMAGE_RESIZE_OPTIONS = "imageResizeOptions";
    public static final String OPT_IMAGE_CROP_OPTIONS = "imageCropOptions";
    public static final String OPT_IMAGE_AUTO_ORIENTATION = "imageAutoOrient";
    
    /** Command string options, provided for backward compatibility */
    private String commandOptions = "";
    
    /** Image resize options */
    private ImageResizeOptions resizeOptions;
    
    /** Image crop options */
    private ImageCropOptions cropOptions;
    
    private boolean autoOrient = true;
    /**
     * Set the command string options
     * 
     * @param commandOptions    the command string options
     */
    public void setCommandOptions(String commandOptions)
    {
        this.commandOptions = commandOptions;
    }
    
    /**
     * Get the command string options
     * 
     * @return  String  the command string options
     */
    public String getCommandOptions()
    {
        return commandOptions;
    }
    
    /**
     * Set the image resize options
     * 
     * @param resizeOptions image resize options
     */
    public void setResizeOptions(ImageResizeOptions resizeOptions)
    {
        this.resizeOptions = resizeOptions;
    }
    
    /**
     * Get the image resize options
     * 
     * @return  ImageResizeOptions  image resize options
     */
    public ImageResizeOptions getResizeOptions()
    {
        return resizeOptions;
    }
    
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("ImageTransformationOptions [commandOptions=").append(this.commandOptions)
                    .append(", resizeOptions=").append(this.resizeOptions).append(", cropOptions=")
                    .append(this.cropOptions).append(", autoOrient=").append(this.autoOrient).append("]");
        return builder.toString();
    }
    
    /**
     * Overrides the base class implementation to add our options
     */
    @Override
    public Map<String, Object> toMap()
    {
        Map<String, Object> baseProps = super.toMap();
        Map<String, Object> props = new HashMap<String, Object>(baseProps);
        props.put(OPT_COMMAND_OPTIONS, commandOptions);
        props.put(OPT_IMAGE_RESIZE_OPTIONS, resizeOptions);
        props.put(OPT_IMAGE_CROP_OPTIONS, cropOptions);
        props.put(OPT_IMAGE_AUTO_ORIENTATION, autoOrient);
        return props;
    }

    
    /**
     * @param cropOptions the cropOptions to set
     */
    public void setCropOptions(ImageCropOptions cropOptions)
    {
        this.cropOptions = cropOptions;
    }
    
    /**
     * @return the cropOptions
     */
    public ImageCropOptions getCropOptions()
    {
        return this.cropOptions;
    }

    /**
     * @return Will the image be automatically oriented(rotated) based on the EXIF "Orientation" data.
     * Defaults to TRUE
     */    
    public boolean isAutoOrient()
    {
        return this.autoOrient;
    }

    /**
     * @param autoOrient automatically orient (rotate) based on the EXIF "Orientation" data
     */ 
    public void setAutoOrient(boolean autoOrient)
    {
        this.autoOrient = autoOrient;
    }
}
