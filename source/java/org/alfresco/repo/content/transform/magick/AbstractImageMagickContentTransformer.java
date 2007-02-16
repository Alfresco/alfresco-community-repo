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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.content.transform.magick;

import java.io.File;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.filestore.FileContentWriter;
import org.alfresco.repo.content.transform.AbstractContentTransformer;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Abstract helper for transformations based on <b>ImageMagick</b>
 * 
 * @author Derek Hulley
 */
public abstract class AbstractImageMagickContentTransformer extends AbstractContentTransformer
{
    /** the prefix for mimetypes supported by the transformer */
    public static final String MIMETYPE_IMAGE_PREFIX = "image/";
    
    private static final Log logger = LogFactory.getLog(AbstractImageMagickContentTransformer.class);
    
    private boolean available;
    
    public AbstractImageMagickContentTransformer()
    {
        this.available = false;
    }
    
    /**
     * @return Returns true if the transformer is functioning otherwise false
     */
    public boolean isAvailable()
    {
        return available;
    }

    /**
     * Make the transformer available
     * @param available
     */
    protected void setAvailable(boolean available)
    {
        this.available = available;
    }

    /**
     * Checks for the JMagick and ImageMagick dependencies, using the common
     * {@link #transformInternal(File, File) transformation method} to check
     * that the sample image can be converted.
     * <p>
     * If initialization is successful, then autoregistration takes place.
     */
    public void init()
    {
        if (getMimetypeService() == null)
        {
            throw new AlfrescoRuntimeException("MimetypeMap not present");
        }
        try
        {
            // load, into memory the sample gif
            String resourcePath = "org/alfresco/repo/content/transform/magick/alfresco.gif";
            InputStream imageStream = getClass().getClassLoader().getResourceAsStream(resourcePath);
            if (imageStream == null)
            {
                throw new AlfrescoRuntimeException("Sample image not found: " + resourcePath);
            }
            // dump to a temp file
            File inputFile = TempFileProvider.createTempFile(
                    getClass().getSimpleName() + "_init_source_",
                    ".gif");
            FileContentWriter writer = new FileContentWriter(inputFile);
            writer.putContent(imageStream);
            
            // create the output file
            File outputFile = TempFileProvider.createTempFile(
                    getClass().getSimpleName() + "_init_target_",
                    ".png");
            
            // execute it
            Map<String, Object> options = Collections.emptyMap();
            transformInternal(inputFile, outputFile, options);
            
            // check that the file exists
            if (!outputFile.exists())
            {
                throw new Exception("Image conversion failed: \n" +
                        "   from: " + inputFile + "\n" +
                        "   to: " + outputFile);
            }
            // we can be sure that it works
            setAvailable(true);
            
            // register
            super.register();
        }
        catch (Throwable e)
        {
            logger.error(
                    getClass().getSimpleName() + " not available: " +
                    (e.getMessage() != null ? e.getMessage() : ""));
            // debug so that we can trace the issue if required
            logger.debug(e);
        }
    }
    
    /**
     * Some image formats are not supported by ImageMagick, or at least appear not to work.
     * 
     * @param mimetype the mimetype to check
     * @return Returns true if ImageMagic can handle the given image format
     */
    public static boolean isSupported(String mimetype)
    {
        if (!mimetype.startsWith(MIMETYPE_IMAGE_PREFIX))
        {
            return false;   // not an image
        }
        else if (mimetype.equals(MimetypeMap.MIMETYPE_IMAGE_RGB))
        {
            return false;   // rgb extension doesn't work
        }
        else if (mimetype.equals(MimetypeMap.MIMETYPE_IMAGE_SVG))
        {
            return false;   // svg extension doesn't work
        }
        else
        {
            return true;
        }
    }
    
    /**
     * Supports image to image conversion, but only if the JMagick library and required
     * libraries are available.
     */
    public double getReliability(String sourceMimetype, String targetMimetype)
    {
        if (!available)
        {
            return 0.0;
        }
        if (!AbstractImageMagickContentTransformer.isSupported(sourceMimetype) ||
                !AbstractImageMagickContentTransformer.isSupported(targetMimetype))
        {
            // only support IMAGE -> IMAGE (excl. RGB)
            return 0.0;
        }
        else
        {
            return 1.0;
        }
    }
    
    /**
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
        
        // create required temp files
        File sourceFile = TempFileProvider.createTempFile(
                getClass().getSimpleName() + "_source_",
                "." + sourceExtension);
        File targetFile = TempFileProvider.createTempFile(
                getClass().getSimpleName() + "_target_",
                "." + targetExtension);
        
        // pull reader file into source temp file
        reader.getContent(sourceFile);
        
        // transform the source temp file to the target temp file
        transformInternal(sourceFile, targetFile, options);
        
        // check that the file was created
        if (!targetFile.exists())
        {
            throw new ContentIOException("JMagick transformation failed to write output file");
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
     * Transform the image content from the source file to the target file
     * 
     * @param sourceFile the source of the transformation
     * @param targetFile the target of the transformation
     * @param options the transformation options supported by ImageMagick
     * @throws Exception
     */
    protected abstract void transformInternal(
            File sourceFile,
            File targetFile,
            Map<String, Object> options) throws Exception;
}
