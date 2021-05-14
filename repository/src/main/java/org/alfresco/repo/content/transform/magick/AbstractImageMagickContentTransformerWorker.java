/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
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

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.filestore.FileContentWriter;
import org.alfresco.repo.content.transform.ContentTransformerHelper;
import org.alfresco.repo.content.transform.ContentTransformerWorker;
import org.alfresco.repo.content.transform.RemoteTransformerClient;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

import java.io.File;
import java.io.InputStream;

/**
 * Abstract helper for transformations based on <b>ImageMagick</b>
 * 
 * @author Derek Hulley
 *
 * @deprecated The transformations code is being moved out of the codebase and replaced by the new async RenditionService2 or other external libraries.
 */
@Deprecated
public abstract class AbstractImageMagickContentTransformerWorker extends ContentTransformerHelper implements ContentTransformerWorker, InitializingBean
{
    /** the prefix for mimetypes supported by the transformer */
    public static final String MIMETYPE_IMAGE_PREFIX = "image/";
    
    private static final Log logger = LogFactory.getLog(AbstractImageMagickContentTransformerWorker.class);
    
    private boolean available;
    
    public AbstractImageMagickContentTransformerWorker()
    {
        this.available = false;
    }
    
    /**
     * @return Returns true if the transformer is functioning otherwise false
     */
    public boolean isAvailable()
    {
        if (remoteTransformerClientConfigured() && !remoteTransformerClient.isAvailable())
        {
            afterPropertiesSet();
        }

        return available;
    }

    /**
     * Make the transformer available
     * @param available boolean
     */
    protected void setAvailable(boolean available)
    {
        this.available = available;
    }

    protected RemoteTransformerClient remoteTransformerClient;

    /**
     * Sets the optional remote transformer client which will be used in preference to a local command if available.
     *
     * @param remoteTransformerClient may be null;
     */
    public void setRemoteTransformerClient(RemoteTransformerClient remoteTransformerClient)
    {
        this.remoteTransformerClient = remoteTransformerClient;
    }

    public boolean remoteTransformerClientConfigured()
    {
        return remoteTransformerClient != null && remoteTransformerClient.getBaseUrl() != null;
    }

    /**
     * Checks for the JMagick and ImageMagick dependencies, using the common
     * {@link #transformInternal(File, String , File, java.lang.String, TransformationOptions) transformation method} to check
     * that the sample image can be converted.
     * <p>
     * If initialization is successful, then autoregistration takes place.
     */
    public void afterPropertiesSet()
    {
        if (getMimetypeService() == null)
        {
            throw new AlfrescoRuntimeException("MimetypeMap not present");
        }
        if (!remoteTransformerClientConfigured())
        {
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
                transformInternal(
                        inputFile, MimetypeMap.MIMETYPE_IMAGE_GIF,
                        outputFile, MimetypeMap.MIMETYPE_IMAGE_PNG,
                        new TransformationOptions());

                // check that the file exists
                if (!outputFile.exists() || outputFile.length() == 0)
                {
                    throw new Exception("Image conversion failed: \n" +
                            "   from: " + inputFile + "\n" +
                            "   to: " + outputFile);
                }
                // we can be sure that it works
                setAvailable(true);
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
    }
    
    /**
     * Some image formats are not supported by ImageMagick, or at least appear not to work.
     * 
     * @param mimetype the mimetype to check
     * @return Returns true if ImageMagic can handle the given image format
     */
    public static boolean isSupported(String mimetype)
    {
        // There are a few mimetypes in the system that do not start with "image/" but which
        // nevertheless are supported by this transformer.
        if (mimetype.equals(MimetypeMap.MIMETYPE_APPLICATION_EPS))
        {
            return true;
        }
        else if (mimetype.equals(MimetypeMap.MIMETYPE_APPLICATION_PHOTOSHOP))
        {
            return true;
        }
        
        else if (!mimetype.startsWith(MIMETYPE_IMAGE_PREFIX))
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
        else if (mimetype.equals(MimetypeMap.MIMETYPE_IMG_DWG))
        {
            return false;   // dwg extension doesn't work
        }
        else if (mimetype.equals(MimetypeMap.MIMETYPE_IMAGE_DWT))
        {
            return false;   // dwt extension doesn't work
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
    public boolean isTransformable(String sourceMimetype, String targetMimetype, TransformationOptions options)
    {
        if (!isAvailable())
        {
            return false;
        }
        
        // Add limited support (so lots of other transforms are not supported) for PDF to PNG. An ALF-14303 workaround.
        // Will only be used as part of failover transformer.PdfToImage. Note .ai is the same format as .pdf
        if ( (MimetypeMap.MIMETYPE_PDF.equals(sourceMimetype) ||
              MimetypeMap.MIMETYPE_APPLICATION_ILLUSTRATOR.equals(sourceMimetype)) &&
              MimetypeMap.MIMETYPE_IMAGE_PNG.equals(targetMimetype))
        {
            return true;
        }
        
        // Add extra support for tiff to pdf to allow multiple page preview (ALF-7278)
        if (MimetypeMap.MIMETYPE_IMAGE_TIFF.equals(sourceMimetype) &&
            MimetypeMap.MIMETYPE_PDF.equals(targetMimetype))
        {
            return true;
        }
        
        if (!AbstractImageMagickContentTransformerWorker.isSupported(sourceMimetype) ||
                !AbstractImageMagickContentTransformerWorker.isSupported(targetMimetype))
        {
            // only support IMAGE -> IMAGE (excl. RGB)
            return false;
        }
        else
        {
            return true;
        }
    }
    
    @Override
    public String getComments(boolean available)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("# Supports transformations between mimetypes starting with \"image/\", but not\n");
        sb.append("# \"");
        sb.append(MimetypeMap.MIMETYPE_IMAGE_RGB);
        sb.append("\", \"");
        sb.append(MimetypeMap.MIMETYPE_IMAGE_SVG);
        sb.append("\" or \"");
        sb.append(MimetypeMap.MIMETYPE_IMG_DWG);
        sb.append("\".\n");
        sb.append("# eps as source or target.\n");
        sb.append("# pdf or ai to png.\n");
        sb.append("# tiff to pdf.\n");
        return sb.toString();
    }

    /**
     * @see #transformInternal(File, String, File, String, TransformationOptions)
     */
    public final void transform(
            ContentReader reader,
            ContentWriter writer,
            TransformationOptions options) throws Exception
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

        boolean tEngineConfigured = remoteTransformerClientConfigured();
        if (tEngineConfigured)
        {
            transformRemote(reader, writer, options, sourceMimetype, targetMimetype, sourceExtension, targetExtension);
        }
        else
        {
            transformLocal(reader, writer, options, sourceMimetype, targetMimetype, sourceExtension, targetExtension);
        }

        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Transformation completed: \n" +
                    "   source: " + reader + "\n" +
                    "   target: " + writer + "\n" +
                    "   options: " + options);
        }
    }

    private void transformLocal(ContentReader reader, ContentWriter writer, TransformationOptions options, String sourceMimetype, String targetMimetype, String sourceExtension, String targetExtension) throws Exception
    {
        // create required temp files
        File sourceFile = TempFileProvider.createTempFile(
                getClass().getSimpleName() + "_source_",
                "." + sourceExtension);
        File targetFile = TempFileProvider.createTempFile(
                getClass().getSimpleName() + "_target_",
                "." + targetExtension);

        // pull reader file into source temp file
        reader.getContent(sourceFile);

        // For most target mimetypes, it only makes sense to read the first page of the
        // source, as the target is a single page, so set the pageLimit automatically.
        // However for others, such as PDF (see ALF-7278) all pages should be read.
        // transform the source temp file to the target temp file
        transformInternal(sourceFile, sourceMimetype, targetFile, targetMimetype, options);

        // check that the file was created
        if (!targetFile.exists() || targetFile.length() == 0)
        {
            throw new ContentIOException("ImageMagick transformation failed to write output file");
        }
        // upload the output image
        writer.putContent(targetFile);
    }

    protected abstract void transformRemote(ContentReader reader, ContentWriter writer, TransformationOptions options, String sourceMimetype, String targetMimetype, String sourceExtension, String targetExtension) throws IllegalAccessException;

    /**
     * Transform the image content from the source file to the target file
     *
     * @param sourceFile the source of the transformation
     * @param sourceMimetype the mimetype of the source of the transformation
     * @param targetFile the target of the transformation
     * @param targetMimetype the mimetype of the target of the transformation
     * @param options the transformation options supported by ImageMagick
     * @throws Exception
     */
    protected abstract void transformInternal(
            File sourceFile,
            String sourceMimetype,
            File targetFile,
            String targetMimetype,
            TransformationOptions options) throws Exception;
}
