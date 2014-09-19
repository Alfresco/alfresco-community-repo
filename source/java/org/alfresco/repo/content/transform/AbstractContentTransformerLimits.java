/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.repo.content.transform;

import static org.alfresco.service.cmr.repository.TransformationOptionLimits.OPT_MAX_PAGES;
import static org.alfresco.service.cmr.repository.TransformationOptionLimits.OPT_MAX_SOURCE_SIZE_K_BYTES;
import static org.alfresco.service.cmr.repository.TransformationOptionLimits.OPT_PAGE_LIMIT;
import static org.alfresco.service.cmr.repository.TransformationOptionLimits.OPT_READ_LIMIT_K_BYTES;
import static org.alfresco.service.cmr.repository.TransformationOptionLimits.OPT_READ_LIMIT_TIME_MS;
import static org.alfresco.service.cmr.repository.TransformationOptionLimits.OPT_TIMEOUT_MS;

import java.util.Map;
import java.util.Map.Entry;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.repo.content.AbstractContentReader;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.TransformationOptionLimits;
import org.alfresco.service.cmr.repository.TransformationOptions;

/**
 * Provides transformation limits for {@link org.alfresco.repo.content.transform.ContentTransformer}
 * implementations.
 * <p>
 * This class maintains the limits and provides methods that combine limits:
 * a) for the transformer as a whole
 * b) for specific combinations if source and target mimetypes
 * c) for the {@link TransformationOptions} provided for a specific transform.
 * 
 * @author Alan Davis
 */
@AlfrescoPublicApi
public abstract class AbstractContentTransformerLimits extends ContentTransformerHelper implements ContentTransformer
{
    /** Indicates if 'page' limits are supported. */
    private boolean pageLimitsSupported;
    
    /** For debug **/
    protected TransformerDebug transformerDebug;

    /**
     * Indicates if 'page' limits are supported.
     * @return false by default.
     */
    protected boolean isPageLimitSupported(String sourceMimetype, String targetMimetype, TransformationOptions options)
    {
        return pageLimitsSupported;
    }
    
    /**
     * Indicates if 'page' limits are supported.
     */
    public void setPageLimitsSupported(boolean pageLimitsSupported)
    {
        this.pageLimitsSupported = pageLimitsSupported;
    }

    /**
     * Helper setter of the transformer debug. 
     * @param transformerDebug
     */
    public void setTransformerDebug(TransformerDebug transformerDebug)
    {
        this.transformerDebug = transformerDebug;
    }

    public boolean isTransformable(String sourceMimetype, String targetMimetype, TransformationOptions options)
    {
        throw new IllegalStateException("Method should no longer be called. Override isTransformableMimetype in subclass.");
    }
    
    /**
     * {@inheritDoc}<p>
     * 
     * Implementation calls the deprecated overloaded method without the sourceSize parameter
     * and then {@link #isTransformableSize(String, long, String, TransformationOptions)}.
     */
    @Override
    public boolean isTransformable(String sourceMimetype, long sourceSize, String targetMimetype, TransformationOptions options)
    {
        // To make TransformerDebug output clearer, check the mimetypes and then the sizes.
        // If not done, 'unavailable' transformers due to size might be reported even
        // though they cannot transform the source to the target mimetype.

        return
            isSupportedTransformation(sourceMimetype, targetMimetype, options) &&
            isTransformableMimetype(sourceMimetype, targetMimetype, options) &&
            isTransformableSize(sourceMimetype, sourceSize, targetMimetype, options);
    }

    /**
     * Indicates if this transformer is able to transform the given source mimetype 
     * to the target mimetype. If overridden, consider also overriding
     * {@link ContentTransformerHelper#getComments(boolean)}.
     */
    @Override
    public boolean isTransformableMimetype(String sourceMimetype, String targetMimetype, TransformationOptions options)
    {
        return isTransformable(sourceMimetype, targetMimetype, options);
    }

    /**
     * Indicates if this transformer is able to transform the given {@code sourceSize}.
     * The {@code maxSourceSizeKBytes} property may indicate that only small source files
     * may be transformed.
     * @param sourceSize size in bytes of the source. If negative, the source size is unknown.
     * @return {@code true} if the source is transformable.
     */
    @Override
    public boolean isTransformableSize(String sourceMimetype, long sourceSize, String targetMimetype, TransformationOptions options)
    {
        boolean sizeOkay = true;
        if (sourceSize >= 0)
        {
            // if maxSourceSizeKBytes == 0 this implies the transformation is disabled
            long maxSourceSizeKBytes = getMaxSourceSizeKBytes(sourceMimetype, targetMimetype, options);
            sizeOkay = maxSourceSizeKBytes < 0 || (maxSourceSizeKBytes > 0 && sourceSize <= maxSourceSizeKBytes*1024);
            if (!sizeOkay && transformerDebug.isEnabled())
            {
                transformerDebug.unavailableTransformer(this, sourceMimetype, targetMimetype, maxSourceSizeKBytes);
            }
        }
        return sizeOkay;
    }

    /**
     * Returns the maximum source size (in KBytes) allowed given the supplied values.
     * @return 0 if the the transformation is disabled, -1 if there is no limit, otherwise the size in KBytes.
     */
    @Override
    public long getMaxSourceSizeKBytes(String sourceMimetype, String targetMimetype, TransformationOptions options)
    {
        long maxSourceSizeKBytes = -1;
        
        // The maxSourceSizeKbytes value is ignored if this transformer is able to use
        // page limits and the limits include a pageLimit. Normally used in the creation
        // of icons. Note the readLimitKBytes value is not checked as the combined limits
        // only have the max or limit KBytes value set (the smaller value is returned).
        TransformationOptionLimits limits = getLimits(sourceMimetype, targetMimetype, options);
        if (!isPageLimitSupported(sourceMimetype, targetMimetype, options) || limits.getPageLimit() <= 0)
        {
            maxSourceSizeKBytes = limits.getMaxSourceSizeKBytes();
        }
        
        return maxSourceSizeKBytes;
    }

    /**
     * @deprecated use @link {@link #getLimits(String, String, TransformationOptions)#getTimeoutMs()}
     *             which allows the limits to be selected based on mimetype and use.
     */
    protected long getTimeoutMs()
    {
        return getLimits().getTimeoutMs();
    }

    /**
     * @deprecated transformation limits are now set with global properties rather than spring configuration.
     */
    public void setTimeoutMs(long timeoutMs)
    {
        deprecatedSetter(null, null, OPT_TIMEOUT_MS+'='+timeoutMs);
    }
    
    /**
     * @deprecated use @link {@link #getLimits(String, String, TransformationOptions)#getReadLimitTimeMs()}
     *             which allows the limits to be selected based on mimetype and use.
     */
    protected long getReadLimitTimeMs()
    {
        return getLimits().getReadLimitTimeMs();
    }

    /**
     * @deprecated transformation limits are now set with global properties rather than spring configuration.
     */
    public void setReadLimitTimeMs(long readLimitTimeMs)
    {
        deprecatedSetter(null, null, OPT_READ_LIMIT_TIME_MS+'='+readLimitTimeMs);
    }

    /**
     * @deprecated use @link {@link #getLimits(String, String, TransformationOptions)#getMaxSourceSizeKBytes()}
     *             which allows the limits to be selected based on mimetype and use.
     */
    protected long getMaxSourceSizeKBytes()
    {
        return getLimits().getMaxSourceSizeKBytes();
    }

    /**
     * @deprecated transformation limits are now set with global properties rather than spring configuration.
     */
    public void setMaxSourceSizeKBytes(long maxSourceSizeKBytes)
    {
        deprecatedSetter(null, null, OPT_MAX_SOURCE_SIZE_K_BYTES+'='+maxSourceSizeKBytes);
    }

    /**
     * @deprecated use @link {@link #getLimits(String, String, TransformationOptions)#getReadLimitKBytes()}
     *             which allows the limits to be selected based on mimetype and use.
     */
    protected long getReadLimitKBytes()
    {
        return getLimits().getReadLimitKBytes();
    }

    /**
     * @deprecated transformation limits are now set with global properties rather than spring configuration.
     */
    public void setReadLimitKBytes(long readLimitKBytes)
    {
        deprecatedSetter(null, null, OPT_READ_LIMIT_K_BYTES+'='+readLimitKBytes);
    }

    /**
     * @deprecated use @link {@link #getLimits(String, String, TransformationOptions)#getMaxPages()}
     *             which allows the limits to be selected based on mimetype and use.
     */
    protected int getMaxPages()
    {
        return getLimits().getMaxPages();
    }

    /**
     * @deprecated transformation limits are now set with global properties rather than spring configuration.
     */
    public void setMaxPages(int maxPages)
    {
        deprecatedSetter(null, null, OPT_MAX_PAGES+'='+maxPages);
    }

    /**
     * @deprecated use @link {@link #getLimits(String, String, TransformationOptions)#getPageLimit()}
     *             which allows the limits to be selected based on mimetype and use.
     */
    protected int getPageLimit()
    {
        return getLimits().getPageLimit();
    }

    /**
     * @deprecated transformation limits are now set with global properties rather than spring configuration.
     */
    public void setPageLimit(int pageLimit)
    {
        deprecatedSetter(null, null, OPT_PAGE_LIMIT+'='+pageLimit);
    }

    /**
     * @deprecated use @link {@link #getLimits(String, String, TransformationOptions, String)} which allows the
     *             limits to be selected based on mimetype and use.
     */
    protected TransformationOptionLimits getLimits()
    {
        return transformerConfig.getLimits(this, null, null, null);
    }
    
    /**
     * @deprecated transformation limits are now set with global properties rather than spring configuration.
     */
    public void setLimits(TransformationOptionLimits limits)
    {
        deprecatedLimitsSetter(null, null, limits);
    }

    /**
     * @deprecated transformation limits are now set with global properties rather than spring configuration.
     */
    public void setMimetypeLimits(Map<String, Map<String, TransformationOptionLimits>> mimetypeLimits)
    {
        for (Entry<String, Map<String, TransformationOptionLimits>> source: mimetypeLimits.entrySet())
        {
            String sourceMimetype = source.getKey();
            for (Entry<String, TransformationOptionLimits> target: source.getValue().entrySet())
            {
                String targetMimetype = target.getKey();
                TransformationOptionLimits limits = target.getValue();
                deprecatedLimitsSetter(sourceMimetype, targetMimetype, limits);
            }
        }
    }

    private void deprecatedLimitsSetter(String sourceMimetype, String targetMimetype, TransformationOptionLimits limits)
    {
        if (limits.supported())
        {
            // Ignore limit pairs that are not specified
            for (String limit: new String[] {
                    limits.getTimePair().toString(OPT_TIMEOUT_MS, OPT_READ_LIMIT_TIME_MS),
                    limits.getKBytesPair().toString(OPT_MAX_SOURCE_SIZE_K_BYTES, OPT_READ_LIMIT_K_BYTES),
                    limits.getPagesPair().toString(OPT_MAX_PAGES, OPT_PAGE_LIMIT)
                })
            {
                if (limit != null)
                {
                    deprecatedSetter(sourceMimetype, targetMimetype, '.'+limit);
                }
            }
        }
        else
        {
            deprecatedSetter(sourceMimetype, targetMimetype, TransformerConfig.SUPPORTED+"=false");
        }
    }

    /**
     * Returns max and limit values for time, size and pages for a specified source and
     * target mimetypes, combined with this Transformer's general limits and optionally
     * the supplied transformation option's limits.
     */
    protected TransformationOptionLimits getLimits(ContentReader reader, ContentWriter writer,
            TransformationOptions options)
    {
        return (reader == null || writer == null)
            ? transformerConfig.getLimits(this, null, null, options.getUse()).combine(options.getLimits())
            : getLimits(reader.getMimetype(), writer.getMimetype(), options);
    }

    /**
     * Returns max and limit values for time, size and pages for a specified source and
     * target mimetypes, combined with this Transformer's general limits and optionally
     * the supplied transformation option's limits.
     */
    protected TransformationOptionLimits getLimits(String sourceMimetype, String targetMimetype,
            TransformationOptions options)
    {
        TransformationOptionLimits limits = transformerConfig.getLimits(this, sourceMimetype, targetMimetype, (options == null ? null : options.getUse()));
        return (options == null) ? limits : limits.combine(options.getLimits());
    }

    /**
     * Pass on any limits to the reader. Will only do so if the reader is an
     * {@link AbstractContentReader}.
     * @param reader passed to {@link #transform(ContentReader, ContentWriter, TransformationOptions).
     * @param writer passed to {@link #transform(ContentReader, ContentWriter, TransformationOptions).
     * @param options passed to {@link #transform(ContentReader, ContentWriter, TransformationOptions).
     */
    protected void setReaderLimits(ContentReader reader, ContentWriter writer,
            TransformationOptions options)
    {
        if (reader instanceof AbstractContentReader)
        {
            AbstractContentReader abstractContentReader = (AbstractContentReader)reader;
            TransformationOptionLimits limits = getLimits(reader, writer, options);
            abstractContentReader.setLimits(limits);
            abstractContentReader.setTransformerDebug(transformerDebug);
        }
    }
}