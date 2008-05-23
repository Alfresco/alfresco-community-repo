/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.content.transform.swf;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.transform.AbstractContentTransformer2;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.util.TempFileProvider;
import org.alfresco.util.exec.RuntimeExec;
import org.alfresco.util.exec.RuntimeExec.ExecutionResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * PDF to SWF content transformer
 * 
 * @author Roy Wetherall
 */
public class PDFToSWFContentTransformer extends AbstractContentTransformer2
{
    /** Logger */
    private static final Log logger = LogFactory.getLog(PDFToSWFContentTransformer.class);
    
    /** Used to indicate whether the transformaer in available or not */
    private boolean available = false;
    
    /** Check and transform command */
    private RuntimeExec checkCommand;
    private RuntimeExec transformCommand;
    
    /** Default flash version to convert to */
    private static final String DEFAULT_FLASH_VERSION = "9";
    
    /** Check command string */
    private static final String PDF2SWF_CHECK = "pdf2swf -V";
    
    /** Transformation command string */
    private static final String PDF2SWF_COMMAND = "pdf2swf -t ${flashVersion} \"${source}\" -o \"${target}\"";
    private static final String VAR_SOURCE = "source";
    private static final String VAR_TARGET = "target";
    private static final String VAR_FLASH_VERSION = "flashVersion";
    
    /**
     * Get the check command for the PDF2SWF tool
     * 
     * @return  RuntimeExec check command
     */
    private RuntimeExec getCheckCommand()
    {
        if (this.checkCommand == null)
        {
            this.checkCommand = createCommand(PDF2SWF_CHECK);
        }
        
        return this.checkCommand;
    }
    
    /**
     * Get the transform command for the PDF2SWF tool
     * 
     * @return  RuntimeExec transform command
     */
    private RuntimeExec getTransformCommand()
    {
        if (this.transformCommand == null)
        {
            this.transformCommand = createCommand(PDF2SWF_COMMAND);
        }
        
        return this.transformCommand;
    }
    
    /**
     * Helper to create a runtime exec object for a given command string
     * 
     * @param commandString     command string
     * @return RuntimeExec      runtime exec command
     */
    private RuntimeExec createCommand(String commandString)
    {
        // Create command
        RuntimeExec result = new RuntimeExec();
        
        // Set the command string
        Map<String, String> commandsByOS = new HashMap<String, String>(1);
        commandsByOS.put(".*", commandString);
        result.setCommandMap(commandsByOS);
        
        // Set the error code
        result.setErrorCodes("1");
        
        return result;
    }
    
    /**
     * @see org.alfresco.repo.content.transform.AbstractContentTransformer2#register()
     */
    @Override
    public void register()
    {
        ExecutionResult result = getCheckCommand().execute();
        // check the return code
        this.available = result.getSuccess();
        if (this.available == false)
        {
            logger.error("Failed to start SWF2PDF transformer: \n" + result);
        }
        else
        {
            // no check - just assume it is available
            this.available = true;
        }
        
        // call the base class to make sure that it gets registered
        super.register();
    }
    
    /**
     * @see org.alfresco.repo.content.transform.AbstractContentTransformer2#transformInternal(org.alfresco.service.cmr.repository.ContentReader, org.alfresco.service.cmr.repository.ContentWriter, org.alfresco.service.cmr.repository.TransformationOptions)
     */
    @Override
    protected void transformInternal(ContentReader reader, ContentWriter writer, TransformationOptions options)
        throws Exception
    {
        // get mimetypes
        String sourceMimetype = getMimetype(reader);
        String targetMimetype = getMimetype(writer);
        
        // get the extensions to use
        String sourceExtension = getMimetypeService().getExtension(sourceMimetype);
        String targetExtension = getMimetypeService().getExtension(targetMimetype);
        if (sourceExtension == null || targetExtension == null)
        {
            throw new AlfrescoRuntimeException("Unknown extensions for mimetypes: \n" +
                    "   source mimetype: " + sourceMimetype + "\n" +
                    "   source extension: " + sourceExtension + "\n" +
                    "   target mimetype: " + targetMimetype + "\n" +
                    "   target extension: " + targetExtension);
        }
        
        // create required temp files
        File sourceFile = TempFileProvider.createTempFile(
                getClass().getSimpleName() + "_source_",
                "." + sourceExtension);
        File targetFile = TempFileProvider.createTempFile(
                getClass().getSimpleName() + "_target_",
                "." + targetExtension);
        
        String flashVersion = DEFAULT_FLASH_VERSION;
        Map<String, String> properties = new HashMap<String, String>(5);
        if (options instanceof SWFTransformationOptions)
        {
            SWFTransformationOptions swfOptions = (SWFTransformationOptions)options;
            if (swfOptions.getFlashVersion() != null)
            {
                flashVersion = swfOptions.getFlashVersion();
            }
        }        
        properties.put(VAR_FLASH_VERSION, "-T " + flashVersion);
        
        // add the source and target properties
        properties.put(VAR_SOURCE, sourceFile.getAbsolutePath());
        properties.put(VAR_TARGET, targetFile.getAbsolutePath());
        
        // pull reader file into source temp file
        reader.getContent(sourceFile);

        // execute the transformation command
        ExecutionResult result = null;
        try
        {
            result = getTransformCommand().execute(properties);
        }
        catch (Throwable e)
        {
            throw new ContentIOException("Transformation failed during command execution: \n" + transformCommand, e);
        }
        
        // check
        if (!result.getSuccess())
        {
            throw new ContentIOException("Transformation failed - status indicates an error: \n" + result);
        }
        
        // check that the file was created
        if (!targetFile.exists())
        {
            throw new ContentIOException("Transformation failed - target file doesn't exist: \n" + result);
        }
        // copy the target file back into the repo
        writer.putContent(targetFile);
        
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("PDF2SWF transformation completed: \n" +
                    "   source: " + reader + "\n" +
                    "   target: " + writer + "\n" +
                    "   options: " + options + "\n" +
                    "   result: \n" + result);
        }
    }

    /**
     * @see org.alfresco.repo.content.transform.ContentTransformer#isTransformable(java.lang.String, java.lang.String, org.alfresco.service.cmr.repository.TransformationOptions)
     */
    public boolean isTransformable(String sourceMimetype, String targetMimetype, TransformationOptions options)
    {
        boolean result = false;
        
        if (this.available == true)
        {
            if (MimetypeMap.MIMETYPE_PDF.equals(sourceMimetype) == true &&
                MimetypeMap.MIMETYPE_FLASH.equals(targetMimetype) == true)
            {
                result = true;
            }
        }
        
        return result;
    }
}
