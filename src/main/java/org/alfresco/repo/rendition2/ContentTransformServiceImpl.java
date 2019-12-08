/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2019 Alfresco Software Limited
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
package org.alfresco.repo.rendition2;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.filestore.FileContentWriter;
import org.alfresco.repo.content.transform.ContentTransformer;
import org.alfresco.repo.content.transform.ContentTransformerRegistry;
import org.alfresco.repo.content.transform.LegacyTransformerDebug;
import org.alfresco.repo.content.transform.UnimportantTransformException;
import org.alfresco.repo.content.transform.UnsupportedTransformationException;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NoTransformerException;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.util.PropertyCheck;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains deprecated code originally from {@link org.alfresco.repo.content.ContentServiceImpl} that is used to perform
 * Legacy transforms.
 *
 * @author adavis
 */
@Deprecated
public abstract class ContentTransformServiceImpl implements InitializingBean
{
    protected static Log logger = LogFactory.getLog(LegacyTransformClient.class);

    private MimetypeService mimetypeService;
    private ContentTransformerRegistry transformerRegistry;
    private LegacyTransformerDebug transformerDebug;

    private boolean transformerFailover = true;

    public void setMimetypeService(MimetypeService mimetypeService)
    {
        this.mimetypeService = mimetypeService;
    }

    public void setTransformerRegistry(ContentTransformerRegistry transformerRegistry)
    {
        this.transformerRegistry = transformerRegistry;
    }

    public void setTransformerDebug(LegacyTransformerDebug transformerDebug)
    {
        this.transformerDebug = transformerDebug;
    }

    public void setTransformerFailover(boolean transformerFailover)
    {
        this.transformerFailover = transformerFailover;
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        PropertyCheck.mandatory(this, "mimetypeService", mimetypeService);
        PropertyCheck.mandatory(this, "transformerRegistry", transformerRegistry);
        PropertyCheck.mandatory(this, "transformerDebug", transformerDebug);
    }

    @Deprecated
    public void transform(ContentReader reader, ContentWriter writer, TransformationOptions options)
            throws NoTransformerException, ContentIOException
    {
        // check that source and target mimetypes are available
        if (reader == null)
        {
            throw new AlfrescoRuntimeException("The content reader must be set");
        }
        String sourceMimetype = reader.getMimetype();
        if (sourceMimetype == null)
        {
            throw new AlfrescoRuntimeException("The content reader mimetype must be set: " + reader);
        }
        String targetMimetype = writer.getMimetype();
        if (targetMimetype == null)
        {
            throw new AlfrescoRuntimeException("The content writer mimetype must be set: " + writer);
        }

        long sourceSize = reader.getSize();
        try
        {
            // look for a transformer
            transformerDebug.pushAvailable(reader.getContentUrl(), sourceMimetype, targetMimetype, options);
            List<ContentTransformer> transformers = getActiveTransformers(sourceMimetype, sourceSize, targetMimetype, options);
            transformerDebug.availableTransformers(transformers, sourceSize, options, "ContentService.transform(...)");

            int count = transformers.size();
            if (count == 0)
            {
                throw new NoTransformerException(sourceMimetype, targetMimetype);
            }

            if (count == 1 || !transformerFailover)
            {
                ContentTransformer transformer = transformers.size() == 0 ? null : transformers.get(0);
                transformer.transform(reader, writer, options);
            }
            else
            {
                failoverTransformers(reader, writer, options, targetMimetype, transformers);
            }
        }
        finally
        {
            if (transformerDebug.isEnabled())
            {
                transformerDebug.popAvailable();
                debugTransformations(sourceMimetype, targetMimetype, sourceSize, options);
            }
        }
    }

    private void failoverTransformers(ContentReader reader, ContentWriter writer,
                                      TransformationOptions options, String targetMimetype,
                                      List<ContentTransformer> transformers)
    {
        List<AlfrescoRuntimeException> exceptions = null;
        boolean done = false;
        try
        {
            // Try the best transformer and then the next if it fails
            // and so on down the list
            char c = 'a';
            String outputFileExt = mimetypeService.getExtension(targetMimetype);
            for (ContentTransformer transformer : transformers)
            {
                ContentWriter currentWriter = writer;
                File tempFile = null;
                try
                {
                    // We can't know in advance which of the
                    // available transformer will work - if any.
                    // We can't write into the ContentWriter stream.
                    // So make a temporary file writer with the
                    // current transformer name.
                    tempFile = TempFileProvider.createTempFile(
                            "FailoverTransformer_intermediate_"
                                    + transformer.getClass().getSimpleName() + "_", "."
                                    + outputFileExt);
                    currentWriter = new FileContentWriter(tempFile);
                    currentWriter.setMimetype(targetMimetype);
                    currentWriter.setEncoding(writer.getEncoding());

                    if (c != 'a' && transformerDebug.isEnabled())
                    {
                        transformerDebug.debug("");
                        transformerDebug.debug("Try " + c + ")");
                    }
                    c++;

                    transformer.transform(reader, currentWriter, options);

                    if (tempFile != null)
                    {
                        writer.putContent(tempFile);
                    }

                    // No need to close input or output streams
                    // (according
                    // to comment in FailoverContentTransformer)
                    done = true;
                    return;
                }
                catch (Exception e)
                {
                    if (exceptions == null)
                    {
                        exceptions = new ArrayList<AlfrescoRuntimeException>();
                    }
                    if (!(e instanceof AlfrescoRuntimeException))
                    {
                        e = new AlfrescoRuntimeException(e.getMessage(), e);
                    }
                    exceptions.add((AlfrescoRuntimeException)e);

                    // Set a new reader to refresh the input stream.
                    reader = reader.getReader();
                }
            }
            // Throw the exception from the first transformer. The
            // others are consumed.
            if (exceptions != null)
            {
                throw exceptions.get(0);
            }
        }
        finally
        {
            // Log exceptions that we have consumed. We may have thrown the first one if
            // none of the transformers worked.
            if (exceptions != null)
            {
                boolean first = true;
                for (Exception e : exceptions)
                {
                    Throwable rootCause = (e instanceof AlfrescoRuntimeException) ? ((AlfrescoRuntimeException)e).getRootCause() : null;
                    String message = (rootCause == null ? null : rootCause.getMessage());
                    if (done)
                    {
                        message = "Transformer succeeded after previous transformer failed"+ (message == null ? "" : ": "+message);
                        if (rootCause instanceof UnsupportedTransformationException ||
                                rootCause instanceof UnimportantTransformException)
                        {
                            logger.debug(message);
                        }
                        else
                        {
                            logger.warn(message, e);
                        }
                    }
                    else if (!first) // The first exception is logged later
                    {
                        message = "Transformer exception"+ (message == null ? "" : ": "+message);
                        if (rootCause instanceof UnsupportedTransformationException ||
                                rootCause instanceof UnimportantTransformException)
                        {
                            logger.debug(message);
                        }
                        else
                        {
                            logger.error(message, e);
                        }
                        first = false;
                    }
                }
            }
        }
    }

    @Deprecated
    public ContentTransformer getTransformer(String sourceUrl, String sourceMimetype, long sourceSize, String targetMimetype, TransformationOptions options)
    {
        List<ContentTransformer> transformers = null;
        try
        {
            transformerDebug.pushAvailable(sourceUrl, sourceMimetype, targetMimetype, options);
            List<ContentTransformer> activeTransformers = getActiveTransformers(sourceMimetype, sourceSize, targetMimetype, options);
            transformerDebug.availableTransformers(activeTransformers, sourceSize, options, "ContentService.getTransformer(...)");
            transformers = activeTransformers.isEmpty() ? null : activeTransformers;
        }
        finally
        {
            transformerDebug.popAvailable();
        }
        return (transformers == null) ? null : transformers.get(0);
    }

    private void debugTransformations(String sourceMimetype, String targetMimetype,
                                      long sourceSize, TransformationOptions transformOptions)
    {
        // check the file name
        if (MimetypeMap.MIMETYPE_TEXT_PLAIN.equals(sourceMimetype) &&
                MimetypeMap.MIMETYPE_IMAGE_PNG.equals(targetMimetype))
        {
            String fileName = transformerDebug.getFileName(transformOptions, true, 0);
            if (fileName != null && fileName.contains("debugTransformers.txt"))
            {
                transformerDebug.transformationsByTransformer(null, false, false, null);
                transformerDebug.transformationsByExtension(null, null, false, false, false, null);
            }
        }
    }

    @Deprecated
    public List<ContentTransformer> getTransformers(String sourceUrl, String sourceMimetype, long sourceSize, String targetMimetype, TransformationOptions options)
    {
        try
        {
            // look for a transformer
            transformerDebug.pushAvailable(sourceUrl, sourceMimetype, targetMimetype, options);
            List<ContentTransformer> transformers = getActiveTransformers(sourceMimetype, sourceSize, targetMimetype, options);
            transformerDebug.availableTransformers(transformers, sourceSize, options, "ContentService.getTransformer(...)");
            return transformers.isEmpty() ? null : transformers;
        }
        finally
        {
            transformerDebug.popAvailable();
        }
    }

    @Deprecated
    public long getMaxSourceSizeBytes(String sourceMimetype, String targetMimetype, TransformationOptions options)
    {
        try
        {
            long maxSourceSize = 0;
            transformerDebug.pushAvailable(null, sourceMimetype, targetMimetype, options);
            List<ContentTransformer> transformers = getActiveTransformers(sourceMimetype, -1, targetMimetype, options);
            for (ContentTransformer transformer: transformers)
            {
                long maxSourceSizeKBytes = transformer.getMaxSourceSizeKBytes(sourceMimetype, targetMimetype, options);
                if (maxSourceSize >= 0)
                {
                    if (maxSourceSizeKBytes < 0)
                    {
                        maxSourceSize = -1;
                    }
                    else if (maxSourceSizeKBytes > 0 && maxSourceSize < maxSourceSizeKBytes)
                    {
                        maxSourceSize = maxSourceSizeKBytes;
                    }
                }
                // if maxSourceSizeKBytes == 0 this implies the transformation is disabled
            }
            if (transformerDebug.isEnabled())
            {
                transformerDebug.availableTransformers(transformers, -1, options,
                        "ContentService.getMaxSourceSizeBytes() = "+transformerDebug.fileSize(maxSourceSize*1024));
            }
            return (maxSourceSize > 0) ? maxSourceSize * 1024 : maxSourceSize;
        }
        finally
        {
            transformerDebug.popAvailable();
        }
    }

    @Deprecated
    public List<ContentTransformer> getActiveTransformers(String sourceMimetype, long sourceSize, String targetMimetype, TransformationOptions options)
    {
        return transformerRegistry.getActiveTransformers(sourceMimetype, sourceSize, targetMimetype, options);
    }
}
