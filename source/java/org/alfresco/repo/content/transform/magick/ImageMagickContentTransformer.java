/*
 * Copyright (C) 2005 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.repo.content.transform.magick;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.util.exec.RuntimeExec;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Executes a statement to implement 
 * 
 * @author Derek Hulley
 */
public class ImageMagickContentTransformer extends AbstractImageMagickContentTransformer
{
    /** the command options, such as <b>--resize</b>, etc. */
    public static final String KEY_OPTIONS = "options";
    /** source variable name */
    public static final String VAR_OPTIONS = "options";
    /** source variable name */
    public static final String VAR_SOURCE = "source";
    /** target variable name */
    public static final String VAR_TARGET = "target";
    
    private static final Log logger = LogFactory.getLog(ImageMagickContentTransformer.class);

    /** the system command executer */
    private RuntimeExec executer;
    
    public ImageMagickContentTransformer()
    {
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
     * Checks for the JMagick and ImageMagick dependencies, using the common
     * {@link #transformInternal(File, File) transformation method} to check
     * that the sample image can be converted. 
     */
    public void init()
    {
        if (executer == null)
        {
            throw new AlfrescoRuntimeException("System runtime executer not set");
        }
        super.init();
    }
    
    /**
     * Transform the image content from the source file to the target file
     */
    protected void transformInternal(File sourceFile, File targetFile, Map<String, Object> options) throws Exception
    {
        Map<String, String> properties = new HashMap<String, String>(5);
        // set properties
        properties.put(KEY_OPTIONS, (String) options.get(KEY_OPTIONS));
        properties.put(VAR_SOURCE, sourceFile.getAbsolutePath());
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
}
