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
package org.alfresco.repo.content.transform;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.transform.client.model.config.SupportedSourceAndTarget;
import org.alfresco.transform.client.model.config.TransformOption;
import org.alfresco.transform.client.model.config.Transformer;
import org.alfresco.transform.client.registry.AbstractTransformRegistry;
import org.alfresco.transform.client.registry.CombinedConfig;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Based on the logic of the legacy BinaryPassThrough and String Transformers.
 * Streams the source content to target when the respective mimetypes are identical, or transforms to "text/plain" from
 * any mimetype starting "text/", or "application/x-javascript", or "application/dita+xml". There must be no transform
 * options, but in case of the text transform the source and target encodings may be change.
 *
 * Supported transforms are added by {@link CombinedConfig#addPassThroughTransformer(
 * org.alfresco.service.cmr.repository.MimetypeService, AbstractTransformRegistry)}.
 *
 * @author adavis
 */
public class LocalPassThroughTransform extends AbstractLocalTransform
{
    public static final String NAME = "PassThrough";

    public LocalPassThroughTransform(String name, TransformerDebug transformerDebug,
                                     MimetypeService mimetypeService, boolean strictMimeTypeCheck,
                                     Map<String, Set<String>> strictMimetypeExceptions,
                                     boolean retryTransformOnDifferentMimeType,
                                     Set<TransformOption> transformsTransformOptions,
                                     LocalTransformServiceRegistry localTransformServiceRegistry)
    {
        super(name, transformerDebug, mimetypeService, strictMimeTypeCheck, strictMimetypeExceptions,
                retryTransformOnDifferentMimeType, transformsTransformOptions, localTransformServiceRegistry);
    }

    public static Transformer getConfig(List<String> mimetypes)
    {
        Set<SupportedSourceAndTarget> supportedSourceAndTargetList = new HashSet();
        for (String mimetype: mimetypes)
        {
            supportedSourceAndTargetList.add(SupportedSourceAndTarget.builder()
                    .withSourceMediaType(mimetype)
                    .withTargetMediaType(mimetype)
                    .withPriority(20)
                    .build());
            if (isToText(mimetype, mimetype))
            {
                supportedSourceAndTargetList.add(SupportedSourceAndTarget.builder()
                        .withSourceMediaType(mimetype)
                        .withTargetMediaType(MimetypeMap.MIMETYPE_TEXT_PLAIN)
                        .withPriority(20)
                        .build());
            }
        }
        return Transformer.builder().withTransformerName(LocalPassThroughTransform.NAME).
                withSupportedSourceAndTargetList(supportedSourceAndTargetList).build();
    }

    private static boolean isToText(String sourceMimetype, String targetMimetype)
    {
        return (targetMimetype.equals(MimetypeMap.MIMETYPE_TEXT_PLAIN) &&
                sourceMimetype.startsWith(MimetypeMap.PREFIX_TEXT)) ||
                sourceMimetype.equals(MimetypeMap.MIMETYPE_JAVASCRIPT) ||
                sourceMimetype.equals(MimetypeMap.MIMETYPE_DITA);
    }

    @Override
    public boolean isAvailable()
    {
        return true;
    }

    @Override
    protected void transformImpl(ContentReader reader, ContentWriter writer, Map<String, String> transformOptions,
                                 String sourceMimetype, String targetMimetype, String sourceExtension,
                                 String targetExtension, String renditionName, NodeRef sourceNodeRef)
            throws UnsupportedTransformationException, ContentIOException
    {
        if (isToText(sourceMimetype, targetMimetype))
        {
            // Set the encodings if specified.
            String sourceEncoding = reader.getEncoding();
            try (Reader charReader = sourceEncoding == null
                    ? new InputStreamReader(reader.getContentInputStream())
                    : new InputStreamReader(reader.getContentInputStream(), sourceEncoding))
            {
                String targetEncoding = writer.getEncoding();
                try (Writer charWriter = targetEncoding == null
                    ? new OutputStreamWriter(writer.getContentOutputStream())
                    : new OutputStreamWriter(writer.getContentOutputStream(), targetEncoding))
                {
                    char[] buffer = new char[8192];
                    int readCount = 0;
                    while (readCount > -1)
                    {
                        // write the last read count number of bytes
                        charWriter.write(buffer, 0, readCount);
                        // fill the buffer again
                        readCount = charReader.read(buffer);
                    }
                }
            }
            catch (IOException e)
            {
                log.error(e);
            }
        }
        else // simple pass through
        {
            writer.putContent(reader.getContentInputStream());
        }
    }
}
