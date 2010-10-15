/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */

package org.alfresco.repo.rendition.executer;

import org.alfresco.repo.content.transform.ContentTransformer;
import org.alfresco.service.cmr.rendition.RenditionServiceException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NoTransformerException;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Nick Smith
 */
public abstract class AbstractTransformationRenderingEngine extends AbstractRenderingEngine
{
    private static Log logger = LogFactory.getLog(AbstractTransformationRenderingEngine.class);

    /* Error messages */
    private static final String TRANSFORMER_NOT_EXISTS_MESSAGE_PATTERN = "Transformer for '%s' source mime type and '%s' target mime type was not found. Operation can't be performed";
    private static final String NOT_TRANSFORMABLE_MESSAGE_PATTERN = "Content not transformable for '%s' source mime type and '%s' target mime type. Operation can't be performed";
    private static final String TRANSFORMING_ERROR_MESSAGE = "Some error occurred during document transforming. Error message: ";

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.rendition.executer.AbstractRenderingEngine#render(org.alfresco.repo.rendition.executer.AbstractRenderingEngine.RenderingContext)
     */
    @Override
    protected void render(RenderingContext context)
    {
        ContentReader contentReader = context.makeContentReader();
        String sourceMimeType = contentReader.getMimetype();
        String targetMimeType = getTargetMimeType(context);

        TransformationOptions options = getTransformOptions(context);

        ContentTransformer transformer = this.contentService.getTransformer(sourceMimeType, targetMimeType, options);

        // Actually perform the rendition.
        if (null == transformer)
        {
            // There's no transformer available for the requested rendition!
            throw new RenditionServiceException(String.format(TRANSFORMER_NOT_EXISTS_MESSAGE_PATTERN, sourceMimeType,
                        targetMimeType));
        }

        if (transformer.isTransformable(sourceMimeType, targetMimeType, options))
        {
            ContentWriter contentWriter = context.makeContentWriter();
            try
            {
                contentService.transform(contentReader, contentWriter, options);
            }
            catch (NoTransformerException ntx)
            {
                {
                    logger.debug("No transformer found to execute rule: \n" + "   reader: " + contentReader + "\n"
                                + "   writer: " + contentWriter + "\n" + "   action: " + this);
                }
                throw new RenditionServiceException(TRANSFORMING_ERROR_MESSAGE + ntx.getMessage(), ntx);
            }
        }
        else
        {
            throw new RenditionServiceException(String.format(NOT_TRANSFORMABLE_MESSAGE_PATTERN, sourceMimeType, targetMimeType));
        }
    }

    protected abstract TransformationOptions getTransformOptions(RenderingContext context);
}
