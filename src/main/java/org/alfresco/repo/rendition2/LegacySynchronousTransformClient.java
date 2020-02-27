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

import org.alfresco.repo.content.transform.ContentTransformer;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.TransformationOptions;

import java.util.Map;

/**
 * Request synchronous transforms. Used in refactoring deprecated code, which called Legacy transforms, so that it will
 * first try a Local transform, falling back to Legacy if not available. Transforms take place using transforms
 * available on the local machine (based on {@link org.alfresco.repo.content.transform.AbstractContentTransformer2}).
 *
 * @author adavis
 */
@Deprecated
public class LegacySynchronousTransformClient extends ContentTransformServiceImpl implements SynchronousTransformClient
{
    private static final String TRANSFORM = "Legacy synchronous transform ";

    private TransformationOptionsConverter converter;

    public void setConverter(TransformationOptionsConverter converter)
    {
        this.converter = converter;
    }

    @Override
    public boolean isSupported(String sourceMimetype, long sourceSizeInBytes, String contentUrl, String targetMimetype,
                               Map<String, String> actualOptions, String transformName, NodeRef sourceNodeRef)
    {
        String renditionName = TransformDefinition.convertToRenditionName(transformName);
        TransformationOptions transformationOptions = converter.getTransformationOptions(renditionName, actualOptions);
        transformationOptions.setSourceNodeRef(sourceNodeRef);

        ContentTransformer legacyTransform = getTransformer(contentUrl, sourceMimetype,
                sourceSizeInBytes, targetMimetype, transformationOptions);

        if (logger.isDebugEnabled())
        {
            logger.debug(TRANSFORM + renditionName + " from " + sourceMimetype +
                    (legacyTransform == null ? " is unsupported" : " is supported"));
        }
        return legacyTransform != null;
    }

    @Override
    public void transform(ContentReader reader, ContentWriter writer, Map<String, String> actualOptions,
                          String transformName, NodeRef sourceNodeRef)
    {
        String renditionName = TransformDefinition.convertToRenditionName(transformName);
        try
        {
            TransformationOptions options = converter.getTransformationOptions(renditionName, actualOptions);
            options.setSourceNodeRef(sourceNodeRef);

            if (null == reader || !reader.exists())
            {
                throw new IllegalArgumentException("sourceNodeRef "+sourceNodeRef+" has no content.");
            }

            if (logger.isDebugEnabled())
            {
                logger.debug(TRANSFORM + "requested " + renditionName);
            }

            // We don't obtain the Legacy transformer and then call its transform method as they unlike Local and
            // Transform Service transforms automatically fail over to the next highest priority. This was not done
            // for the newer transforms, as a fail over can always be defined and that makes it simpler to understand
            // what is going on.
            transform(reader, writer, options);

            if (logger.isDebugEnabled())
            {
                logger.debug(TRANSFORM + "created " + renditionName);
            }
        }
        catch (Exception e)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug(TRANSFORM + "failed " + renditionName, e);
            }
            throw e;
        }
    }

    @Override
    public String getName()
    {
        return "Legacy";
    }
}
