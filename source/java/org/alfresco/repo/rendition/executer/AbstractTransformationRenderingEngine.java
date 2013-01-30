/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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

import java.util.Collection;

import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.content.transform.ContentTransformer;
import org.alfresco.repo.content.transform.TransformerDebug;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.rendition.RenditionServiceException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NoTransformerException;
import org.alfresco.service.cmr.repository.TransformationOptionLimits;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.service.cmr.repository.TransformationSourceOptions;
import org.alfresco.service.cmr.repository.TransformationSourceOptions.TransformationSourceOptionsSerializer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Nick Smith
 */
public abstract class AbstractTransformationRenderingEngine extends AbstractRenderingEngine
{
    private static Log logger = LogFactory.getLog(AbstractTransformationRenderingEngine.class);

    /**
     * This optional {@link Long} parameter specifies the timeout for reading
     * the source before an exception is thrown.
     */
    public static final String PARAM_TIMEOUT_MS = TransformationOptionLimits.OPT_TIMEOUT_MS;

    /**
     * This optional {@link Long} parameter specifies how timeout for reading
     * the source before EOF is returned.
     */
    public static final String PARAM_READ_LIMIT_TIME_MS = TransformationOptionLimits.OPT_READ_LIMIT_TIME_MS;

    /**
     * This optional {@link Long} parameter specifies the maximum number of kbytes of
     * the source may be read. An exception is thrown before any are read if larger.
     */
    public static final String PARAM_MAX_SOURCE_SIZE_K_BYTES = TransformationOptionLimits.OPT_MAX_SOURCE_SIZE_K_BYTES;

    /**
     * This optional {@link Long} parameter specifies how many kbytes of
     * the source to read in order to create an image.
     */
    public static final String PARAM_READ_LIMIT_K_BYTES = TransformationOptionLimits.OPT_READ_LIMIT_K_BYTES;

    /**
     * This optional {@link Integer} parameter specifies the maximum number of pages of
     * the source that may be read. An exception is thrown before any are read if larger.
     */
    public static final String PARAM_MAX_PAGES = TransformationOptionLimits.OPT_MAX_PAGES;

    /**
     * This optional {@link Integer} parameter specifies how many source
     * pages should be read in order to create an image.
     */
    public static final String PARAM_PAGE_LIMIT = TransformationOptionLimits.OPT_PAGE_LIMIT;

    /* Error messages */
    private static final String TRANSFORMER_NOT_EXISTS_MESSAGE_PATTERN = "Transformer for '%s' source mime type and '%s' target mime type was not found. Operation can't be performed";
    private static final String NOT_TRANSFORMABLE_MESSAGE_PATTERN = "Content not transformable for '%s' source mime type and '%s' target mime type. Operation can't be performed";
    private static final String TRANSFORMING_ERROR_MESSAGE = "Some error occurred during document transforming. Error message: ";
    
    private Collection<TransformationSourceOptionsSerializer> sourceOptionsSerializers;

    public Collection<TransformationSourceOptionsSerializer> getSourceOptionsSerializers()
    {
        return sourceOptionsSerializers;
    }

    public void setSourceOptionsSerializers(Collection<TransformationSourceOptionsSerializer> sourceOptionsSerializers)
    {
        this.sourceOptionsSerializers = sourceOptionsSerializers;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.rendition.executer.AbstractRenderingEngine#render(org.alfresco.repo.rendition.executer.AbstractRenderingEngine.RenderingContext)
     */
    @Override
    protected void render(RenderingContext context)
    {
        ContentReader contentReader = context.makeContentReader();
        // There will have been an exception if there is no content data so contentReader is not null.
        String sourceUrl = contentReader.getContentUrl();
        String sourceMimeType = contentReader.getMimetype();
        String targetMimeType = getTargetMimeType(context);

        TransformationOptions options = getTransformOptions(context);

        // Log the following getTransform() as trace so we can see the wood for the trees
        ContentTransformer transformer;
        boolean orig = TransformerDebug.setDebugOutput(false);
        try
        {
            transformer = this.contentService.getTransformer(sourceUrl, sourceMimeType, contentReader.getSize(), targetMimeType, options);
        }
        finally
        {
            TransformerDebug.setDebugOutput(orig);
        }
        
        // Actually perform the rendition.
        if (null == transformer)
        {
            // There's no transformer available for the requested rendition!
            throw new RenditionServiceException(String.format(TRANSFORMER_NOT_EXISTS_MESSAGE_PATTERN, sourceMimeType,
                        targetMimeType));
        }

        if (transformer.isTransformable(sourceMimeType, contentReader.getSize(), targetMimeType, options))
        {
        	//ALF-15715: Use temporary write to avoid operating on the real node for fear of row locking while long transforms are in progress
            ContentWriter tempContentWriter = contentService.getTempWriter();
            tempContentWriter.setMimetype(targetMimeType);
            try
            {
                contentService.transform(contentReader, tempContentWriter, options);
                //Copy content from temp writer to real writer
                ContentWriter writer = context.makeContentWriter();
                writer.putContent(tempContentWriter.getReader().getContentInputStream());
            }
            catch (NoTransformerException ntx)
            {
                {
                    logger.debug("No transformer found to execute rule: \n" + "   reader: " + contentReader + "\n"
                                + "   writer: " + tempContentWriter + "\n" + "   action: " + this);
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

    protected TransformationOptions getTransformOptionsImpl(TransformationOptions options, RenderingContext context)
    {
        Long timeoutMs = context.getCheckedParam(PARAM_TIMEOUT_MS, Long.class);
        if (timeoutMs != null)
        {
            options.setTimeoutMs(timeoutMs);
        }
        
        Long readLimitTimeMs = context.getCheckedParam(PARAM_READ_LIMIT_TIME_MS, Long.class);
        if (readLimitTimeMs != null)
        {
            options.setReadLimitTimeMs(readLimitTimeMs);
        }
        
        Long maxSourceSizeKBytes = context.getCheckedParam(PARAM_MAX_SOURCE_SIZE_K_BYTES, Long.class);
        if (maxSourceSizeKBytes != null)
        {
            options.setMaxSourceSizeKBytes(maxSourceSizeKBytes);
        }
        
        Long readLimitKBytes = context.getCheckedParam(PARAM_READ_LIMIT_K_BYTES, Long.class);
        if (readLimitKBytes != null)
        {
            options.setReadLimitKBytes(readLimitKBytes);
        }
        
        Integer maxPages = context.getCheckedParam(PARAM_MAX_PAGES, Integer.class);
        if (maxPages != null)
        {
            options.setMaxPages(maxPages);
        }
        
        Integer pageLimit = context.getCheckedParam(PARAM_PAGE_LIMIT, Integer.class);
        if (pageLimit != null)
        {
            options.setPageLimit(pageLimit);
        }
        
        if (getSourceOptionsSerializers() != null)
        {
            for (TransformationSourceOptionsSerializer sourceSerializer : getSourceOptionsSerializers())
            {
                TransformationSourceOptions sourceOptions = sourceSerializer.deserialize(context);
                if (sourceOptions != null)
                {
                    options.addSourceOptions(sourceOptions);
                }
            }
        }

        return options;
    }

    /*
     * @seeorg.alfresco.repo.rendition.executer.AbstractRenderingEngine#getParameterDefinitions()
     */
    protected Collection<ParameterDefinition> getParameterDefinitions()
    {
        Collection<ParameterDefinition> paramList = super.getParameterDefinitions();
        
        paramList.add(new ParameterDefinitionImpl(PARAM_TIMEOUT_MS, DataTypeDefinition.LONG, false,
                getParamDisplayLabel(PARAM_TIMEOUT_MS)));
        paramList.add(new ParameterDefinitionImpl(PARAM_READ_LIMIT_TIME_MS, DataTypeDefinition.LONG, false,
                getParamDisplayLabel(PARAM_READ_LIMIT_TIME_MS)));
        paramList.add(new ParameterDefinitionImpl(PARAM_MAX_SOURCE_SIZE_K_BYTES, DataTypeDefinition.LONG, false,
                getParamDisplayLabel(PARAM_MAX_SOURCE_SIZE_K_BYTES)));
        paramList.add(new ParameterDefinitionImpl(PARAM_READ_LIMIT_K_BYTES, DataTypeDefinition.LONG, false,
                getParamDisplayLabel(PARAM_READ_LIMIT_K_BYTES)));
        paramList.add(new ParameterDefinitionImpl(PARAM_MAX_PAGES, DataTypeDefinition.INT, false,
                getParamDisplayLabel(PARAM_MAX_PAGES)));
        paramList.add(new ParameterDefinitionImpl(PARAM_PAGE_LIMIT, DataTypeDefinition.INT, false,
                getParamDisplayLabel(PARAM_PAGE_LIMIT)));
        
        return paramList;
    }
}
