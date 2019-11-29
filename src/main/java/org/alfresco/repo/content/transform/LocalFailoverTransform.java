/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2019 Alfresco Software Limited
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
package org.alfresco.repo.content.transform;

import org.alfresco.repo.content.filestore.FileContentWriter;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.transform.client.model.config.TransformOption;
import org.alfresco.util.TempFileProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Transformer that passes a document to a sequence of transforms until one succeeds.
 *
 * Instances are automatically created for transformers identified by alfresco/transform json files and returned from
 * T-Engines which are themselves identified by global properties the match the pattern localTransform.&lt;name>.url.
 * The transforms take place in a separate process (typically a Docker container).
 */
public class LocalFailoverTransform extends AbstractLocalTransform
{
    private final List<LocalTransform> transformers = new ArrayList<>();

    public LocalFailoverTransform(String name, TransformerDebug transformerDebug,
                                  MimetypeService mimetypeService, boolean strictMimeTypeCheck,
                                  Map<String, Set<String>> strictMimetypeExceptions,
                                  boolean retryTransformOnDifferentMimeType,
                                  Set<TransformOption> transformsTransformOptions,
                                  LocalTransformServiceRegistry localTransformServiceRegistry)
    {
        super(name, transformerDebug, mimetypeService, strictMimeTypeCheck, strictMimetypeExceptions,
                retryTransformOnDifferentMimeType, transformsTransformOptions, localTransformServiceRegistry);
    }

    @Override
    public boolean isAvailable()
    {
        return true;
    }

    public void addStepTransformer(LocalTransform stepTransformer)
    {
        transformers.add(stepTransformer);
    }

    @Override
    protected void transformImpl(ContentReader reader,
                                 ContentWriter writer, Map<String, String> transformOptions,
                                 String sourceMimetype, String targetMimetype,
                                 String sourceExtension, String targetExtension,
                                 String renditionName, NodeRef sourceNodeRef)
    {
        final String targetExt = mimetypeService.getExtension(targetMimetype);

        // We need to keep a reference to thrown exceptions as we're going to catch them and
        // then move on to the next transformer. In the event that they all fail, we will throw
        // the first exception.
        RuntimeException transformationException = null;

        for (int i = 0; i < transformers.size(); i++)
        {
            LocalTransform stepTransformer = transformers.get(i);
            ContentWriter currentWriter = null;
            File tempFile = null;
            try
            {
                if (log.isDebugEnabled())
                {
                    log.debug("Transformation attempt " + (i+1) + " of " + transformers.size() +  ": " + stepTransformer);
                }

                // We can't know in advance which transformer in the sequence will work - if any.
                // Therefore we can't write into the ContentWriter stream.
                // So make a temporary file writer with the current transformer name.
                tempFile = TempFileProvider.createTempFile(
                        "LocalFailoverTransformer_intermediate_" + stepTransformer.getClass().getSimpleName() + "_",
                        "." + targetExt);
                currentWriter = new FileContentWriter(tempFile);
                currentWriter.setMimetype(targetMimetype);
                currentWriter.setEncoding(writer.getEncoding());

                // attempt to transform
                stepTransformer.transform(reader, currentWriter, transformOptions, renditionName, sourceNodeRef);
            }
            catch (UnsupportedTransformationException | ContentIOException are)
            {
                if (transformationException == null)
                {
                    transformationException = are;
                }
                else
                {
                    // Don't log the first exception in full until the end as we're going to throw it and it will
                    // get logged again.
                    log.debug("The below exception is provided for information purposes only.", are);
                }

                // Set a new reader to refresh the input stream.
                reader = reader.getReader();
                // and move to the next transformer
                continue;
            }


            if (transformationException == null)
            {
                log.debug("The following exception (from the first transformer) is provided for " +
                        "information purposes only as a later transformer succeeded.", transformationException);
            }

            // No need to close input or output streams
            // Now we must copy the content from the temporary file into the ContentWriter stream.
            if (tempFile != null)
            {
                writer.putContent(tempFile);
            }

            log.info("Transformation was successful");
            return;
        }

        // At this point we have tried all transformers in the sequence without apparent success.
        if (transformationException != null)
        {
            transformerDebug.debug("          No more transformations to failover to");
            log.debug("All transformations were unsuccessful. Throwing first exception.", transformationException);
            throw transformationException;
        }
    }
}