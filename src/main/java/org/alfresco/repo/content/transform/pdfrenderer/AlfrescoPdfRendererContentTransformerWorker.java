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

package org.alfresco.repo.content.transform.pdfrenderer;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.transform.ContentTransformerHelper;
import org.alfresco.repo.content.transform.ContentTransformerWorker;
import org.alfresco.repo.content.transform.magick.ImageResizeOptions;
import org.alfresco.repo.content.transform.magick.ImageTransformationOptions;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.PagedSourceOptions;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.util.PropertyCheck;
import org.alfresco.util.TempFileProvider;
import org.alfresco.util.exec.RuntimeExec;
import org.alfresco.util.exec.RuntimeExec.ExecutionResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

public class AlfrescoPdfRendererContentTransformerWorker extends ContentTransformerHelper implements ContentTransformerWorker, InitializingBean
{

    /** options variable name */
    private static final String KEY_OPTIONS = "options";
    /** source variable name */
    private static final String VAR_SOURCE = "source";
    /** target variable name */
    private static final String VAR_TARGET = "target";

    private static final Log logger = LogFactory.getLog(AlfrescoPdfRendererContentTransformerWorker.class);

    /** the system command executer */
    private RuntimeExec executer;

    /** the check command executer */
    private RuntimeExec checkCommand;

    /** the output from the check command */
    private String versionString;

    private boolean available;

    public AlfrescoPdfRendererContentTransformerWorker()
    {
        this.available = false;
    }

    /**
     * Set the runtime command executer that must be executed in order to run <b>alfresco-pdf-renderer</b>. Whether or not this is
     * the full path to the command or just the command itself depends the environment setup.
     * <p>
     * The command must contain the variables <code>${source}</code> and <code>${target}</code>, which will be replaced
     * by the names of the file to be transformed and the name of the output file respectively.
     * 
     * <pre>
     *    alfresco-pdf-renderer ${source} ${target}
     * </pre>
     * 
     * @param executer the system command executer
     */
    public void setExecuter(RuntimeExec executer)
    {
        this.executer = executer;
    }

    /**
     * Sets the command that must be executed in order to retrieve version information from the executable
     * and thus test that the executable itself is present.
     * 
     * @param checkCommand
     *            command executer to retrieve version information
     */
    public void setCheckCommand(RuntimeExec checkCommand)
    {
        this.checkCommand = checkCommand;
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        PropertyCheck.mandatory(this, "executer", executer);
        PropertyCheck.mandatory(this, "checkCommand", checkCommand);
        // check availability
        try
        {
            ExecutionResult result = this.checkCommand.execute();
            if(result.getSuccess())
            {
                this.versionString = result.getStdOut().trim();
                setAvailable(true);
                logger.info("Using PDF Renderer: "+this.versionString);
            }
            else
            {
                setAvailable(false);
                logger.error("Alfresco-PDF-Renderer is not available for transformations.\n"+result.toString());
            }
        }
        catch (Throwable e)
        {
            setAvailable(false);
            logger.error(getClass().getSimpleName() + " not available: " + (e.getMessage() != null ? e.getMessage() : ""));
            // debug so that we can trace the issue if required
            logger.debug(e);
        }
    }

    /**
     * @return Returns true if the transformer is functioning otherwise false
     */
    @Override
    public boolean isAvailable()
    {
        return available;
    }

    /**
     * Make the transformer available
     * 
     * @param available boolean
     */
    protected void setAvailable(boolean available)
    {
        this.available = available;
    }

    @Override
    public String getVersionString()
    {
        return this.versionString;
    }

    @Override
    public boolean isTransformable(String sourceMimetype, String targetMimetype, TransformationOptions options)
    {
        if (!available)
        {
            return false;
        }

        // Add limited support (so lots of other transforms are not supported)
        // for PDF to PNG.
        if ((MimetypeMap.MIMETYPE_PDF.equals(sourceMimetype) || MimetypeMap.MIMETYPE_APPLICATION_ILLUSTRATOR.equals(sourceMimetype)) &&
            MimetypeMap.MIMETYPE_IMAGE_PNG.equals(targetMimetype))
        {
            return true;
        }

        return false;
    }

    @Override
    public void transform(ContentReader reader, ContentWriter writer, TransformationOptions options) throws Exception
    {
        // get mimetypes
        String sourceMimetype = getMimetype(reader);
        String targetMimetype = getMimetype(writer);

        // get the extensions to use
        MimetypeService mimetypeService = getMimetypeService();
        String sourceExtension = mimetypeService.getExtension(sourceMimetype);
        String targetExtension = mimetypeService.getExtension(targetMimetype);
        if (sourceExtension == null || targetExtension == null)
        {
            throw new AlfrescoRuntimeException("Unknown extensions for mimetypes: \n" +
                "   source mimetype: " + sourceMimetype + "\n" +
                "   source extension: " + sourceExtension + "\n" +
                "   target mimetype: " + targetMimetype + "\n" +
                "   target extension: " + targetExtension);
        }

        // create required temp files
        File sourceFile = TempFileProvider.createTempFile(getClass().getSimpleName() + "_source_", "." + sourceExtension);
        File targetFile = TempFileProvider.createTempFile(getClass().getSimpleName() + "_target_", "." + targetExtension);

        // pull reader file into source temp file
        reader.getContent(sourceFile);

        transformInternal(sourceFile, sourceMimetype, targetFile, targetMimetype, options);

        // check that the file was created
        if (!targetFile.exists() || targetFile.length() == 0)
        {
            throw new ContentIOException("alfresco-pdf-renderer transformation failed to write output file");
        }
        
        // upload the output image
        writer.putContent(targetFile);
        
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Transformation completed: \n" +
                "   source: " + reader + "\n" +
                "   target: " + writer + "\n" +
                "   options: " + options);
        }

    }

    /**
     * Transform the pdf content from the source file to the target file
     */

    private void transformInternal(File sourceFile, String sourceMimetype, File targetFile, String targetMimetype, TransformationOptions options) throws Exception
    {
        Map<String, String> properties = new HashMap<String, String>(5);
        // set properties
        if (options instanceof ImageTransformationOptions)
        {
            ImageTransformationOptions imageOptions = (ImageTransformationOptions) options;
            ImageResizeOptions resizeOptions = imageOptions.getResizeOptions();
            String commandOptions = imageOptions.getCommandOptions();
            if (commandOptions == null)
            {
                commandOptions = "";
            }
            if(resizeOptions != null)
            {
	            if (resizeOptions.getHeight() > -1)
	            {
	                commandOptions += " --height=" + resizeOptions.getHeight();
	            }
	            if (resizeOptions.getWidth() > -1)
	            {
	                commandOptions += " --width=" + resizeOptions.getHeight();
	            }
	            if (resizeOptions.getAllowEnlargement())
	            {
	                commandOptions += " --allow-enlargement";
	            }
	            if (resizeOptions.isMaintainAspectRatio())
	            {
	                commandOptions += " --maintain-aspect-ratio";
	            }
            }
            commandOptions += " --page=" + getSourcePageRange(imageOptions, sourceMimetype, targetMimetype);

            properties.put(KEY_OPTIONS, commandOptions);
        }

        properties.put(VAR_SOURCE, sourceFile.getAbsolutePath());
        properties.put(VAR_TARGET, targetFile.getAbsolutePath());

        // execute the statement
        long timeoutMs = options.getTimeoutMs();
        RuntimeExec.ExecutionResult result = executer.execute(properties, timeoutMs);
        if (result.getExitValue() != 0 && result.getStdErr() != null && result.getStdErr().length() > 0)
        {
            throw new ContentIOException("Failed to perform alfresco-pdf-renderer transformation: \n" + result);
        }
        // success
        if (logger.isDebugEnabled())
        {
            logger.debug("alfresco-pdf-renderer executed successfully: \n" + executer);
        }
    }

    @Override
    public String getComments(boolean available)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("# Supports transformations between mimetypes");
        sb.append("# pdf or ai to png.\n");
        return sb.toString();
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
        if (options instanceof ImageTransformationOptions)
        {
            ImageTransformationOptions imageOptions = (ImageTransformationOptions) options;
            PagedSourceOptions pagedSourceOptions = imageOptions.getSourceOptions(PagedSourceOptions.class);
            if (pagedSourceOptions != null)
            {
                if (pagedSourceOptions.getStartPageNumber() != null && pagedSourceOptions.getEndPageNumber() != null)
                {
                    if (pagedSourceOptions.getStartPageNumber().equals(pagedSourceOptions.getEndPageNumber()))
                    {
                        return "" + (pagedSourceOptions.getStartPageNumber() - 1);
                    }
                    else
                    {
                        throw new AlfrescoRuntimeException("The alfresco-pdf-renderer can only convert single pages, no page ranges.");
                    }
                }
                else
                {
                    throw new AlfrescoRuntimeException("The alfresco-pdf-renderer can only convert single pages, no page ranges.");
                }
            }
        }
        return "0";
    }

}
