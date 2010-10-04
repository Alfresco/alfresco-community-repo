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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.util.exec.RuntimeExec;
import org.alfresco.util.exec.RuntimeExec.ExecutionResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Executes a statement to implement 
 * 
 * @author Derek Hulley
 */
public class ImageMagickContentTransformerWorker extends AbstractImageMagickContentTransformerWorker
{
    /** options variable name */
    private static final String KEY_OPTIONS = "options";
    /** source variable name */
    private static final String VAR_SOURCE = "source";
    /** target variable name */
    private static final String VAR_TARGET = "target";
    
    private static final Log logger = LogFactory.getLog(ImageMagickContentTransformerWorker.class);

    /** the system command executer */
    private RuntimeExec executer;

    /** the check command executer */
    private RuntimeExec checkCommand;
    
    /** the output from the check command */
    private String versionString;
    
    /**
     * Default constructor
     */
    public ImageMagickContentTransformerWorker()
    {
        // Intentionally empty
    }
    
    /**
     * Set the runtime command executer that must be executed in order to run
     * <b>ImageMagick</b>.  Whether or not this is the full path to the convertCommand
     * or just the convertCommand itself depends the environment setup.
     * <p>
     * The command must contain the variables <code>${source}</code> and
     * <code>${target}</code>, which will be replaced by the names of the file to
     * be transformed and the name of the output file respectively.
     * <pre>
     *    convert ${source} ${target}
     * </pre>
     *  
     * @param executer the system command executer
     */
    public void setExecuter(RuntimeExec executer)
    {
        this.executer = executer;
    }
    

    /**
     * Sets the command that must be executed in order to retrieve version information from the converting executable
     * and thus test that the executable itself is present.
     * 
     * @param checkCommand
     *            command executer to retrieve version information
     */
    public void setCheckCommand(RuntimeExec checkCommand)
    {
        this.checkCommand = checkCommand;
    }
    
    /**
     * Gets the version string captured from the check command.
     * 
     * @return the version string
     */
    public String getVersionString()
    {
        return this.versionString;
    }

    
    /**
     * Checks for the JMagick and ImageMagick dependencies, using the common
     * {@link #transformInternal(File, File) transformation method} to check
     * that the sample image can be converted. 
     */
    @Override
    public void afterPropertiesSet()
    {
        if (executer == null)
        {
            throw new AlfrescoRuntimeException("System runtime executer not set");
        }
        super.afterPropertiesSet();
        if (isAvailable())
        {
            try
            {
                // On some platforms / versions, the -version command seems to return an error code whilst still
                // returning output, so let's not worry about the exit code!
                ExecutionResult result = this.checkCommand.execute();
                this.versionString = result.getStdOut().trim();
            }
            catch (Throwable e)
            {
                setAvailable(false);
                logger.error(getClass().getSimpleName() + " not available: "
                        + (e.getMessage() != null ? e.getMessage() : ""));
                // debug so that we can trace the issue if required
                logger.debug(e);
            }
            
        }
    }
    
    /**
     * Transform the image content from the source file to the target file
     */
    @Override
    protected void transformInternal(File sourceFile, File targetFile, TransformationOptions options) throws Exception
    {
        Map<String, String> properties = new HashMap<String, String>(5);
        // set properties
        if (options instanceof ImageTransformationOptions)
        {
            ImageTransformationOptions imageOptions = (ImageTransformationOptions)options;
            ImageCropOptions cropOptions = imageOptions.getCropOptions();
            ImageResizeOptions resizeOptions = imageOptions.getResizeOptions();
            String commandOptions = imageOptions.getCommandOptions();
            if (cropOptions != null)
            {
                commandOptions = commandOptions + " " + getImageCropCommandOptions(cropOptions);
            }
            if (resizeOptions != null)
            {
                commandOptions = commandOptions + " " + getImageResizeCommandOptions(resizeOptions);
            }
            properties.put(KEY_OPTIONS, commandOptions);
        }
        properties.put(VAR_SOURCE, sourceFile.getAbsolutePath() + "[0]");
        properties.put(VAR_TARGET, targetFile.getAbsolutePath());
        
        // execute the statement
        RuntimeExec.ExecutionResult result = executer.execute(properties);
        if (result.getExitValue() != 0 && result.getStdErr() != null && result.getStdErr().length() > 0)
        {
            throw new ContentIOException("Failed to perform ImageMagick transformation: \n" + result);
        }
        // success
        if (logger.isDebugEnabled())
        {
            logger.debug("ImageMagic executed successfully: \n" + executer);
        }
    }
    
    /**
     * Gets the imagemagick command string for the image crop options provided
     * 
     * @param imageResizeOptions    image resize options
     * @return String               the imagemagick command options
     */
    private String getImageCropCommandOptions(ImageCropOptions cropOptions)
    {
        StringBuilder builder = new StringBuilder(32);
        String gravity = cropOptions.getGravity();
        if(gravity!=null)
        {
            builder.append("-gravity ");
            builder.append(gravity);
            builder.append(" ");
        }
        builder.append("-crop ");
        int width = cropOptions.getWidth();
        if (width > -1)
        {
            builder.append(width);
        }
        
        int height = cropOptions.getHeight();
        if (height > -1)
        {
            builder.append("x");
            builder.append(height);
        }
        
        if (cropOptions.isPercentageCrop())
        {
            builder.append("%");
        }
        appendOffset(builder, cropOptions.getXOffset());
        appendOffset(builder, cropOptions.getYOffset());
        builder.append(" +repage");
        return builder.toString();
    }

    /**
     * @param builder
     * @param xOffset
     */
    private void appendOffset(StringBuilder builder, int xOffset)
    {
        if(xOffset>=0)
        {
            builder.append("+");
        }
        builder.append(xOffset);
    }

    /**
     * Gets the imagemagick command string for the image resize options provided
     * 
     * @param imageResizeOptions    image resize options
     * @return String               the imagemagick command options
     */
    private String getImageResizeCommandOptions(ImageResizeOptions imageResizeOptions)
    {
        StringBuilder builder = new StringBuilder(32);
        
        if (imageResizeOptions.isResizeToThumbnail() == true)
        {
            builder.append("-thumbnail ");
        }
        else
        {
            builder.append("-resize ");
        }
        
        if (imageResizeOptions.getWidth() > -1)
        {
            builder.append(imageResizeOptions.getWidth());
        }
        
        if (imageResizeOptions.getHeight() > -1)
        {
            builder.append("x");
            builder.append(imageResizeOptions.getHeight());
        }
        
        if (imageResizeOptions.isPercentResize() == true)
        {
            builder.append("%");
        }
        
        if (imageResizeOptions.isMaintainAspectRatio() == false)
        {
            builder.append("!");
        }
        
        return builder.toString();
    }
}
