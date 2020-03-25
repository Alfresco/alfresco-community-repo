/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
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

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.util.Pair;
import org.alfresco.util.TempFileProvider;
import org.alfresco.util.VersionNumber;
import org.alfresco.util.exec.RuntimeExec;
import org.alfresco.util.exec.RuntimeExec.ExecutionResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static org.alfresco.repo.rendition2.RenditionDefinition2.ALLOW_ENLARGEMENT;
import static org.alfresco.repo.rendition2.RenditionDefinition2.ALPHA_REMOVE;
import static org.alfresco.repo.rendition2.RenditionDefinition2.AUTO_ORIENT;
import static org.alfresco.repo.rendition2.RenditionDefinition2.CROP_GRAVITY;
import static org.alfresco.repo.rendition2.RenditionDefinition2.CROP_HEIGHT;
import static org.alfresco.repo.rendition2.RenditionDefinition2.CROP_PERCENTAGE;
import static org.alfresco.repo.rendition2.RenditionDefinition2.CROP_WIDTH;
import static org.alfresco.repo.rendition2.RenditionDefinition2.CROP_X_OFFSET;
import static org.alfresco.repo.rendition2.RenditionDefinition2.CROP_Y_OFFSET;
import static org.alfresco.repo.rendition2.RenditionDefinition2.END_PAGE;
import static org.alfresco.repo.rendition2.RenditionDefinition2.MAINTAIN_ASPECT_RATIO;
import static org.alfresco.repo.rendition2.RenditionDefinition2.RESIZE_HEIGHT;
import static org.alfresco.repo.rendition2.RenditionDefinition2.RESIZE_PERCENTAGE;
import static org.alfresco.repo.rendition2.RenditionDefinition2.RESIZE_WIDTH;
import static org.alfresco.repo.rendition2.RenditionDefinition2.START_PAGE;
import static org.alfresco.repo.rendition2.RenditionDefinition2.THUMBNAIL;

/**
 * Executes a statement to implement 
 *
 * @author Derek Hulley
 *
 * @deprecated The transformations code is being moved out of the codebase and replaced by the new async RenditionService2 or other external libraries.
 */
@Deprecated
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

    private boolean enabled = true;

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
        executer.setProcessProperty(
                "MAGICK_TMPDIR", TempFileProvider.getTempDir().getAbsolutePath());
        this.executer = executer;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
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
     * {@link #transformInternal(File, String, File, String, TransformationOptions) transformation method} to check
     * that the sample image can be converted. 
     */
    @Override
    public void afterPropertiesSet()
    {
        if (enabled)
        {
            if (!remoteTransformerClientConfigured() && executer == null)
            {
                throw new AlfrescoRuntimeException("System runtime executer not set");
            }
            super.afterPropertiesSet();
            if (!remoteTransformerClientConfigured())
            {
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
            else
            {
                Pair<Boolean, String> result = remoteTransformerClient.check(logger);
                Boolean isAvailable = result.getFirst();
                if (isAvailable != null && isAvailable)
                {
                    setAvailable(true);
                    versionString = result.getSecond().trim();
                    logger.debug("Using legacy ImageMagick: " + versionString);
                }
                else
                {
                    setAvailable(false);
                    versionString = "unknown";
                    String message = "Legacy remote ImageMagick is not available for transformations. " + result.getSecond();
                    if (isAvailable == null)
                    {
                        logger.debug(message);
                    }
                    else
                    {
                        logger.error(message);
                    }
                }
            }
        }
    }
    
    /**
     * Transform the image content from the source file to the target file
     */
    @Override
    protected void transformInternal(File sourceFile, String sourceMimetype, 
            File targetFile, String targetMimetype, TransformationOptions options) throws Exception
    {
        Map<String, String> properties = new HashMap<String, String>(5);
        // set properties
        if (options instanceof ImageTransformationOptions)
        {
            ImageTransformationOptions imageOptions = (ImageTransformationOptions)options;
            CropSourceOptions cropOptions = imageOptions.getSourceOptions(CropSourceOptions.class);
            ImageResizeOptions resizeOptions = imageOptions.getResizeOptions();
            String commandOptions = imageOptions.getCommandOptions();
            if (commandOptions == null)
            {
                commandOptions = "";
            }
            // MNT-10882 :  JPEG File Format, does not save the alpha (transparency) channel.
            if (MimetypeMap.MIMETYPE_IMAGE_JPEG.equalsIgnoreCase(targetMimetype) && isAlphaOptionSupported())
            {
                commandOptions += " -alpha remove";
            }
            if (imageOptions.isAutoOrient())
            {
                commandOptions = commandOptions + " -auto-orient"; 
            }
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
        properties.put(VAR_SOURCE, sourceFile.getAbsolutePath() + 
                getSourcePageRange(options, sourceMimetype, targetMimetype));
        properties.put(VAR_TARGET, targetFile.getAbsolutePath());
        
        // execute the statement
        long timeoutMs = options.getTimeoutMs();
        RuntimeExec.ExecutionResult result = executer.execute(properties, timeoutMs);
        if (result.getExitValue() != 0 && result.getStdErr() != null && result.getStdErr().length() > 0)
        {
            throw new ContentIOException("Failed to perform ImageMagick transformation: \n" + result);
        }

        // success
        if (logger.isDebugEnabled())
        {
            logger.debug("ImageMagick executed successfully: \n" + executer);
        }
    }

    protected void transformRemote(ContentReader reader, ContentWriter writer, TransformationOptions options,
                                   String sourceMimetype, String targetMimetype,
                                   String sourceExtension, String targetExtension) throws IllegalAccessException
    {
        String[] args = getTEngineArgs(options, sourceMimetype, targetMimetype, versionString);
        long timeoutMs = options.getTimeoutMs();
        remoteTransformerClient.request(reader, writer, sourceMimetype, sourceExtension, targetExtension,
                timeoutMs, logger, args);
    }

    // Not to be called directly. Refactored to make it easier to test TransformationOptionsConverter.
    public static String[] getTEngineArgs(TransformationOptions options, String sourceMimetype, String targetMimetype,
                                          String versionString)
    {
        String startPage = null;
        String endPage = null;

        String alphaRemove = null;
        String autoOrient = null;

        String cropGravity = null;
        String cropWidth = null;
        String cropHeight = null;
        String cropPercentage = null;
        String cropXOffset = null;
        String cropYOffset = null;

        String thumbnail = null;
        String resizeWidth = null;
        String resizeHeight = null;
        String resizePercentage = null;
        String allowEnlargement = null;
        String maintainAspectRatio = null;

        String commandOptions = null;


        if (options instanceof ImageTransformationOptions)
        {
            ImageTransformationOptions imageOptions = (ImageTransformationOptions)options;
            CropSourceOptions cropOptions = imageOptions.getSourceOptions(CropSourceOptions.class);
            ImageResizeOptions resizeOptions = imageOptions.getResizeOptions();
            commandOptions = imageOptions.getCommandOptions();
            commandOptions = commandOptions == null || "".equals(commandOptions.trim()) ? null : commandOptions.trim();

            // MNT-10882 :  JPEG File Format, does not save the alpha (transparency) channel.
            if (MimetypeMap.MIMETYPE_IMAGE_JPEG.equalsIgnoreCase(targetMimetype) && isAlphaOptionSupported(versionString))
            {
                alphaRemove = Boolean.TRUE.toString();
            }
            if (imageOptions.isAutoOrient())
            {
                autoOrient = Boolean.TRUE.toString();
            }
            if (cropOptions != null)
            {
                cropGravity = cropOptions.getGravity();
                int width = cropOptions.getWidth();
                if (width > -1)
                {
                    cropWidth = Integer.toString(width);
                }

                int height = cropOptions.getHeight();
                if (height > -1)
                {
                    cropHeight = Integer.toString(height);
                }

                if (cropOptions.isPercentageCrop())
                {
                    cropPercentage = Boolean.TRUE.toString();
                }
                cropXOffset = Integer.toString(cropOptions.getXOffset());
                cropYOffset = Integer.toString(cropOptions.getYOffset());
            }
            if (resizeOptions != null)
            {
                if (resizeOptions.isResizeToThumbnail())
                {
                    thumbnail = Boolean.TRUE.toString();
                }
                if (resizeOptions.getWidth() > -1)
                {
                    resizeWidth = Integer.toString(resizeOptions.getWidth());
                }
                if (resizeOptions.getHeight() > -1)
                {
                    resizeHeight = Integer.toString(resizeOptions.getHeight());
                }
                if (resizeOptions.isPercentResize() == true)
                {
                    resizePercentage = Boolean.TRUE.toString();
                }
                if (resizeOptions.getAllowEnlargement())
                {
                    allowEnlargement = Boolean.TRUE.toString();
                }
                if (resizeOptions.isMaintainAspectRatio())
                {
                    maintainAspectRatio = Boolean.TRUE.toString();
                }
            }
        }

        // Page range
        if (options instanceof ImageTransformationOptions)
        {
            ImageTransformationOptions imageOptions = (ImageTransformationOptions) options;
            PagedSourceOptions pagedSourceOptions = imageOptions.getSourceOptions(PagedSourceOptions.class);
            if (pagedSourceOptions != null)
            {
                if (pagedSourceOptions.getStartPageNumber() != null)
                {
                    startPage = Integer.toString(pagedSourceOptions.getStartPageNumber() - 1);
                }
                if (pagedSourceOptions.getEndPageNumber() != null)
                {
                    endPage   = Integer.toString(pagedSourceOptions.getEndPageNumber() - 1);
                }
            }
        }
        else
        {
            if (options.getPageLimit() == 1 || isSingleSourcePageRangeRequired(sourceMimetype, targetMimetype))
            {
                startPage = "0";
            }
        }

        return new String[] {
                START_PAGE, startPage,
                END_PAGE, endPage,

                ALPHA_REMOVE, alphaRemove,
                AUTO_ORIENT, autoOrient,

                CROP_GRAVITY, cropGravity,
                CROP_WIDTH, cropWidth,
                CROP_HEIGHT, cropHeight,
                CROP_PERCENTAGE, cropPercentage,
                CROP_X_OFFSET, cropXOffset,
                CROP_Y_OFFSET, cropYOffset,

                THUMBNAIL, thumbnail,
                RESIZE_WIDTH, resizeWidth,
                RESIZE_HEIGHT, resizeHeight,
                RESIZE_PERCENTAGE, resizePercentage,
                ALLOW_ENLARGEMENT, allowEnlargement,
                MAINTAIN_ASPECT_RATIO, maintainAspectRatio,

                // Parameter not to be taken forward into the Transform Service version
                "commandOptions", commandOptions};
    }

    protected String getImageMagickVersionNumber()
    {
        return getImageMagickVersionNumber(versionString);
    }

    private static String getImageMagickVersionNumber(String versionString)
    {
        Pattern verisonNumPattern = Pattern.compile("Version: ImageMagick ((\\d|\\.)+)(-.*){0,1}");
        try
        {
            Matcher versionNumMatcher = verisonNumPattern.matcher(versionString);
            if (versionNumMatcher.find())
            {
                return versionNumMatcher.group(1);
            }
        }
        catch (Throwable e)
        {
            logger.info("Could not determine version of ImageMagick: " + e.getMessage());
        }
        // version isn't extracted
        return "";
    }
    
    /*
     * MNT-10882 : Transparent PNG->JPG Transform Produces Ugly JPG Rendition
     */
    protected boolean isAlphaOptionSupported()
    {
        return isAlphaOptionSupported(versionString);
    }

    private static boolean isAlphaOptionSupported(String versionString)
    {
        // the "-alpha" option was only introduced in ImageMagick v6.7.5 and will fail in older versions.
        String ALPHA_PROP_SUPPORTED_VERSION = "6.7.5";

        try
        {
           VersionNumber supportedVersion = new VersionNumber(ALPHA_PROP_SUPPORTED_VERSION);
           VersionNumber checkedVersion = new VersionNumber(getImageMagickVersionNumber(versionString));
        
           return supportedVersion.compareTo(checkedVersion) > 0 ? false : true;
        }
        catch (Exception e)
        {
            logger.warn("Could not extract version of ImageMagick. Alpha-compatibility will be disabled: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Gets the imagemagick command string for the image crop options provided
     * 
     * @param cropOptions    image resize options
     * @return String               the imagemagick command options
     */
    private String getImageCropCommandOptions(CropSourceOptions cropOptions)
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
        
        // These are ImageMagick options. See http://www.imagemagick.org/script/command-line-processing.php#geometry for details.
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
        // ALF-7308. Disallow the enlargement of small images e.g. within imgpreview thumbnail.
        if (!imageResizeOptions.getAllowEnlargement())
        {
            builder.append(">");
        }
        
        if (imageResizeOptions.isMaintainAspectRatio() == false)
        {
            builder.append("!");
        }
        
        return builder.toString();
    }
    
    /**
     * Determines whether or not a single page range is required for the given source and target mimetypes.
     * 
     * @param sourceMimetype
     * @param targetMimetype
     * @return whether or not a page range must be specified for the transformer to read the target files
     */
    private static boolean isSingleSourcePageRangeRequired(String sourceMimetype, String targetMimetype)
    {
        // Need a page source if we're transforming from PDF or TIFF to an image other than TIFF
        // or from PSD
        return ((sourceMimetype.equals(MimetypeMap.MIMETYPE_PDF) || 
                sourceMimetype.equals(MimetypeMap.MIMETYPE_IMAGE_TIFF)) && 
                ((!targetMimetype.equals(MimetypeMap.MIMETYPE_IMAGE_TIFF) 
                && targetMimetype.contains(MIMETYPE_IMAGE_PREFIX)) ||
                targetMimetype.equals(MimetypeMap.MIMETYPE_APPLICATION_PHOTOSHOP) ||
                targetMimetype.equals(MimetypeMap.MIMETYPE_APPLICATION_EPS)) ||
                sourceMimetype.equals(MimetypeMap.MIMETYPE_APPLICATION_PHOTOSHOP));
    }
    
    /**
     * Gets the page range from the source to use in the command line.
     * 
     * @param options the transformation options
     * @param sourceMimetype the source mimetype
     * @param targetMimetype the target mimetype
     * @return the source page range for the command line
     */
    private String getSourcePageRange(TransformationOptions options, String sourceMimetype, String targetMimetype)
    {
        // Check for PagedContentSourceOptions in the options
        if (options instanceof ImageTransformationOptions)
        {
            ImageTransformationOptions imageOptions = (ImageTransformationOptions) options;
            PagedSourceOptions pagedSourceOptions = imageOptions.getSourceOptions(PagedSourceOptions.class);
            if (pagedSourceOptions != null)
            {
                if (pagedSourceOptions.getStartPageNumber() != null && 
                        pagedSourceOptions.getEndPageNumber() != null)
                {
                    if (pagedSourceOptions.getStartPageNumber().equals(pagedSourceOptions.getEndPageNumber()))
                    {
                        return "[" + (pagedSourceOptions.getStartPageNumber() - 1) + "]";
                    }
                    else
                    {
                        if (isSingleSourcePageRangeRequired(sourceMimetype, targetMimetype))
                        {
                            throw new AlfrescoRuntimeException(
                                    "A single page is required for targets of type " + targetMimetype);
                        }
                        return "[" + (pagedSourceOptions.getStartPageNumber() - 1) + 
                                "-" + (pagedSourceOptions.getEndPageNumber() - 1) + "]";
                    }
                }
                else
                {
                    // TODO specified start to end of doc and start of doc to specified end not yet supported
                    // Just grab a single page specified by either start or end
                    if (pagedSourceOptions.getStartPageNumber() != null)
                        return "[" + (pagedSourceOptions.getStartPageNumber() - 1) + "]";
                    if (pagedSourceOptions.getEndPageNumber() != null)
                        return "[" + (pagedSourceOptions.getEndPageNumber() - 1) + "]";
                }
            }
        }
        if (options.getPageLimit() == 1 || isSingleSourcePageRangeRequired(sourceMimetype, targetMimetype))
        {
            return "[0]";
        }
        else
        {
            return "";
        }
    }
}
