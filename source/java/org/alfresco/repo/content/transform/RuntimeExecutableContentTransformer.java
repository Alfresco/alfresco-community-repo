/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.content.transform;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.util.TempFileProvider;
import org.alfresco.util.exec.RuntimeExec;
import org.alfresco.util.exec.RuntimeExec.ExecutionResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This configurable wrapper is able to execute any command line transformation that
 * accepts an input and an output file on the command line.
 * <p>
 * The following parameters are use:
 * <ul>
 *   <li><b>{@link #VAR_SOURCE target}</b> - full path to the source file</li>
 *   <li><b>{@link #VAR_TARGET source}</b> - full path to the target file</li>
 * </ul>
 * Provided that the command executed ultimately transforms the source file
 * and leaves the result in the target file, the transformation should be
 * successful.
 * <p>
 * <b>NOTE</b>: It is only the contents of the files that can be transformed.
 * Any attempt to modify the source or target file metadata will, at best, have
 * no effect, but may ultimately lead to the transformation failing.  This is
 * because the files provided are both temporary files that reside in a location
 * outside the system's content store.
 * 
 * @see org.alfresco.util.exec.RuntimeExec
 * 
 * @since 1.1
 * @author Derek Hulley
 */
public class RuntimeExecutableContentTransformer extends AbstractContentTransformer
{
    public static final String VAR_SOURCE = "source";
    public static final String VAR_TARGET = "target";

    private static Log logger = LogFactory.getLog(RuntimeExecutableContentTransformer.class);
    
    private boolean available;
    private RuntimeExec checkCommand;
    private RuntimeExec transformCommand;

    public RuntimeExecutableContentTransformer()
    {
    }
    
    /**
     * Set the runtime executer that will be called as part of the initialisation
     * to determine if the transformer is able to function.  This is optional, but allows
     * the transformer registry to detect and avoid using this instance if it is not working.
     * <p>
     * The command will be considered to have failed if the 
     * 
     * @param checkCommand the initialisation check command
     */
    public void setCheckCommand(RuntimeExec checkCommand)
    {
        this.checkCommand = checkCommand;
    }

    /**
     * Set the runtime executer that will called to perform the actual transformation.
     * 
     * @param transformCommand the runtime transform command
     */
    public void setTransformCommand(RuntimeExec transformCommand)
    {
        this.transformCommand = transformCommand;
    }
    
    /**
     * A comma or space separated list of values that, if returned by the executed command,
     * indicate an error value.  This defaults to <b>"1, 2"</b>.
     * 
     * @param erroCodesStr
     */
    public void setErrorCodes(String errCodesStr)
    {
        throw new AlfrescoRuntimeException("content.runtime_exec.property_moved");
    }
    
    /**
     * Executes the check command, if present.  Any errors will result in this component
     * being rendered unusable within the transformer registry, but may still be called
     * directly.
     */
    @Override
    public void register()
    {
        if (transformCommand == null)
        {
            throw new AlfrescoRuntimeException("Mandatory property 'transformCommand' not set");
        }
        
        // execute the command
        if (checkCommand != null)
        {
            ExecutionResult result = checkCommand.execute();
            // check the return code
            available = result.getSuccess();
        }
        else
        {
            // no check - just assume it is available
            available = true;
        }
        // call the base class to make sure that it gets registered
        super.register();
    }

    /**
     * Unless otherwise configured, this component supports all mimetypes.
     * If the {@link #init() initialization} failed, then it returns 0.0.
     * 
     * @return Returns 1.0 if initialization succeeded, otherwise 0.0.
     */
    public double getReliability(String sourceMimetype, String targetMimetype)
    {
        if (!available)
        {
            return 0.0;
        }
        else
        {
            return 1.0;
        }
    }
    
    /**
     * Converts the source and target content to temporary files with the
     * correct extensions for the mimetype that they map to.
     * 
     * @see #transformInternal(File, File)
     */
    protected final void transformInternal(
            ContentReader reader,
            ContentWriter writer,
            Map<String, Object> options) throws Exception
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
        
        // if the source mimetype is the same as the target's then just stream it
        if (sourceMimetype.equals(targetMimetype))
        {
            writer.putContent(reader.getContentInputStream());
            return;
        }
        
        // create required temp files
        File sourceFile = TempFileProvider.createTempFile(
                getClass().getSimpleName() + "_source_",
                "." + sourceExtension);
        File targetFile = TempFileProvider.createTempFile(
                getClass().getSimpleName() + "_target_",
                "." + targetExtension);
        
        Map<String, String> properties = new HashMap<String, String>(5);
        // copy options over
        for (Map.Entry<String, Object> entry : options.entrySet())
        {
            String key = entry.getKey();
            Object value = entry.getValue();
            properties.put(key, (value == null ? null : value.toString()));
        }
        // add the source and target properties
        properties.put(VAR_SOURCE, sourceFile.getAbsolutePath());
        properties.put(VAR_TARGET, targetFile.getAbsolutePath());
        
        // pull reader file into source temp file
        reader.getContent(sourceFile);

        // execute the transformation command
        ExecutionResult result = null;
        try
        {
            result = transformCommand.execute(properties);
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
            logger.debug("Transformation completed: \n" +
                    "   source: " + reader + "\n" +
                    "   target: " + writer + "\n" +
                    "   options: " + options + "\n" +
                    "   result: \n" + result);
        }
    }
}
