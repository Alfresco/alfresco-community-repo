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
package org.alfresco.repo.content.transform.magick;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.service.cmr.repository.TransformationSourceOptions;

/**
 * Image transformation options
 * 
 * @author Roy Wetherall
 *
 * @deprecated The transformations code is being moved out of the codebase and replaced by the new async RenditionService2 or other external libraries.
 */
@Deprecated
@AlfrescoPublicApi
public class ImageTransformationOptions extends TransformationOptions
{
    public static final String OPT_COMMAND_OPTIONS = "commandOptions";
    public static final String OPT_IMAGE_RESIZE_OPTIONS = "imageResizeOptions";
    public static final String OPT_IMAGE_AUTO_ORIENTATION = "imageAutoOrient";

    /** Command string options, provided for backward compatibility */
    private String commandOptions = "";

    /** Image resize options */
    private ImageResizeOptions resizeOptions;

    private boolean autoOrient = true;

    /**
     * Set the command string options
     * 
     * @deprecated will be removed in a future release and be replaced by individual options that can be checked.
     *
     * @param commandOptions
     *            the command string options
     */
    @Deprecated
    public void setCommandOptions(String commandOptions)
    {
        this.commandOptions = commandOptions;
    }

    /**
     * Get the command string options
     * 
     * @deprecated will be removed in a future release and be replaced by individual options that can be checked.
     *
     * @return String the command string options
     */
    @Deprecated
    public String getCommandOptions()
    {
        return commandOptions;
    }

    /**
     * Set the image resize options
     * 
     * @param resizeOptions
     *            image resize options
     */
    public void setResizeOptions(ImageResizeOptions resizeOptions)
    {
        this.resizeOptions = resizeOptions;
    }

    /**
     * Get the image resize options
     * 
     * @return ImageResizeOptions image resize options
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
                .append(", resizeOptions=").append(this.resizeOptions)
                .append(", autoOrient=").append(this.autoOrient).append("]");
        if (getSourceOptionsList() != null)
        {
            builder.append(", sourceOptions={ ");
            int i = 0;
            for (TransformationSourceOptions sourceOptions : getSourceOptionsList())
            {
                builder.append((i != 0) ? " , " : "");
                builder.append(sourceOptions.getClass().getSimpleName())
                        .append(sourceOptions.toString());
                i++;
            }
            builder.append("} ");
        }
        builder.append("]");
        return builder.toString();
    }

    @Override
    public String toStringAll()
    {
        return super.toStringAll() + "ImageTransformationOptions{" +
                "commandOptions='" + commandOptions + '\'' +
                ", resizeOptions=" + resizeOptions +
                ", autoOrient=" + autoOrient +
                '}';
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
        props.put(OPT_IMAGE_AUTO_ORIENTATION, autoOrient);
        return props;
    }

    /**
     * @return Will the image be automatically oriented(rotated) based on the EXIF "Orientation" data. Defaults to TRUE
     */
    public boolean isAutoOrient()
    {
        return this.autoOrient;
    }

    /**
     * @param autoOrient
     *            automatically orient (rotate) based on the EXIF "Orientation" data
     */
    public void setAutoOrient(boolean autoOrient)
    {
        this.autoOrient = autoOrient;
    }

    @Override
    public void copyFrom(TransformationOptions origOptions)
    {
        super.copyFrom(origOptions);
        if (origOptions != null)
        {
            if (origOptions instanceof ImageTransformationOptions)
            {
                // Clone ImageTransformationOptions
                this.setCommandOptions(((ImageTransformationOptions) origOptions).getCommandOptions());
                this.setResizeOptions(((ImageTransformationOptions) origOptions).getResizeOptions());
                this.setAutoOrient(((ImageTransformationOptions) origOptions).isAutoOrient());
            }
        }
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        if (!super.equals(o))
        {
            return false;
        }
        ImageTransformationOptions that = (ImageTransformationOptions) o;
        return autoOrient == that.autoOrient &&
                Objects.equals(commandOptions, that.commandOptions) &&
                Objects.equals(resizeOptions, that.resizeOptions);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(super.hashCode(), commandOptions, resizeOptions, autoOrient);
    }
}
