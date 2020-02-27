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
 * Transformer that passes a document through a pipeline of transformations to arrive at an target mimetype.
 *
 * Instances are automatically created for transformers identified by alfresco/transform json files and returned from
 * T-Engines which are themselves identified by global properties the match the pattern localTransform.&lt;name>.url.
 * The transforms take place in a separate process (typically a Docker container).
 */
public class LocalPipelineTransform extends AbstractLocalTransform
{
    private final List<IntermediateTransformer> transformers = new ArrayList<>();

    private class IntermediateTransformer
    {
        LocalTransform intermediateTransformer;
        String targetMimetype;
    }

    public LocalPipelineTransform(String name, TransformerDebug transformerDebug,
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

    public void addIntermediateTransformer(LocalTransform intermediateTransformer, String targetMimetype)
    {
        IntermediateTransformer transformer = new IntermediateTransformer();
        transformer.intermediateTransformer = intermediateTransformer;
        transformer.targetMimetype = targetMimetype;
        transformers.add(transformer);
    }

    public LocalTransform getIntermediateTransformer(int i)
    {
        return i >= transformers.size() ? null : transformers.get(i).intermediateTransformer;
    }

    @Override
    protected void transformImpl(ContentReader reader,
                                 ContentWriter writer, Map<String, String> transformOptions,
                                 String sourceMimetype, String targetMimetype,
                                 String sourceExtension, String targetExtension,
                                 String renditionName, NodeRef sourceNodeRef)
    {
        ContentReader currentReader = reader;
        int lastI = transformers.size() - 1;
        for (int i = 0; i <= lastI; i++)
        {
            IntermediateTransformer transformer = transformers.get(i);

            ContentWriter currentWriter;
            if (i == lastI)
            {
                currentWriter = writer;
            }
            else
            {
                // make a temp file writer with the correct extension
                String sourceExt = mimetypeService.getExtension(currentReader.getMimetype());
                String targetExt = mimetypeService.getExtension(transformer.targetMimetype);
                File tempFile = TempFileProvider.createTempFile(
                        "LocalPipelineTransformer_intermediate_" + sourceExt + "_",
                        "." + targetExt);
                currentWriter = new FileContentWriter(tempFile);
                currentWriter.setMimetype(transformer.targetMimetype);
            }

            transformer.intermediateTransformer.transform(currentReader, currentWriter, transformOptions, renditionName, sourceNodeRef);

            // Clear the sourceNodeRef after the first transformation to avoid later transformers thinking the
            // intermediate file is the original node.
            if (i == 0)
            {
                sourceNodeRef = null;
            }

            // Pass the output to the next transformer
            if (i < lastI)
            {
                currentReader = currentWriter.getReader();
            }
        }
    }
}
