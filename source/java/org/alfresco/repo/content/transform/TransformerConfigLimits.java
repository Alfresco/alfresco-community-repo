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

import static org.alfresco.repo.content.transform.TransformerConfig.ANY;
import static org.alfresco.repo.content.transform.TransformerConfig.DEFAULT_TRANSFORMER;
import static org.alfresco.repo.content.transform.TransformerConfig.LIMIT_SUFFIXES;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.management.subsystems.ChildApplicationContextFactory;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.TransformationOptionLimits;

/**
 * Provides access to transformer limits defined via properties.
 * 
 * @author Alan Davis
 */
public class TransformerConfigLimits extends TransformerPropertyNameExtractor
{
    // Holds configured (entries only exist if configured rather than for all possible combinations)
    // limits for for transformer, sourceMimeType and targetMimetype combination.
    // A null transformer is the system wide value. SourceMimeType and targetMimetype may be 'ANY'
    // values to act as wild cards.
    private Map<String, DoubleMap<String, String, TransformationOptionLimits>> limits;

    public TransformerConfigLimits(ChildApplicationContextFactory subsystem, MimetypeService mimetypeService)
    {
        setLimits(subsystem, mimetypeService);
    }

    /**
     * Sets the transformer limits created from system properties.  
     */
    private void setLimits(ChildApplicationContextFactory subsystem, MimetypeService mimetypeService)
    {
        limits = new HashMap<String, DoubleMap<String, String, TransformationOptionLimits>>();

        // Gets all the transformer, source and target combinations in properties that define limits.
        Collection<TransformerSourceTargetValue> transformerNamesAndExt =
                getTransformerSourceTargetValues(LIMIT_SUFFIXES, true, subsystem, mimetypeService);

        // Add the system wide default just in case it is not included, as we always need this one
        TransformationOptionLimits options = getOrCreateTransformerOptionLimits(DEFAULT_TRANSFORMER, ANY, ANY);
        
        // Populate the transformer limits
        for (TransformerSourceTargetValue property: transformerNamesAndExt)
        {
            options = getOrCreateTransformerOptionLimits(property.transformerName,
                    property.sourceMimetype, property.targetMimetype);
            setTransformationOptionsFromProperties(options, property.transformerName, property.sourceExt, property.targetExt,
                    property.value, property.suffix);
        }
    }

    /**
     * Returns the TransformationOptionLimits for the transformer and mimetype combination,
     * creating and adding one if not already included.
     */
    private TransformationOptionLimits getOrCreateTransformerOptionLimits(String transformerName,
            String sourceMimetype, String targetMimetype)
    {
        DoubleMap<String, String, TransformationOptionLimits> mimetypeLimits;
        mimetypeLimits = limits.get(transformerName);
        if (mimetypeLimits == null)
        {
            mimetypeLimits = new DoubleMap<String, String, TransformationOptionLimits>(ANY, ANY);
            limits.put(transformerName, mimetypeLimits);
        }
        
        TransformationOptionLimits options = mimetypeLimits.get(sourceMimetype, targetMimetype);
        if (options == null)
        {
            options = new TransformationOptionLimits();
            mimetypeLimits.put(sourceMimetype, targetMimetype, options);
        }
        return options;
    }
    
    private void setTransformationOptionsFromProperties(TransformationOptionLimits options,
            String transformerName, String sourceExt, String targetExt, String value, String suffix)
    {
        long l = Long.parseLong(value);
        if (suffix == TransformerConfig.MAX_SOURCE_SIZE_K_BYTES)
        {
            options.setMaxSourceSizeKBytes(l);
        }
        else if (suffix == TransformerConfig.TIMEOUT_MS)
        {
            options.setTimeoutMs(l);
        }
        else if (suffix == TransformerConfig.MAX_PAGES)
        {
            options.setMaxPages((int)l);
        }
        else if (suffix == TransformerConfig.READ_LIMIT_K_BYTES)
        {
            options.setReadLimitKBytes(l);
        }
        else if (suffix == TransformerConfig.READ_LIMIT_TIME_MS)
        {
            options.setReadLimitTimeMs(l);
        }
        else // if (suffix == TransformerConfig.PAGE_LIMIT)
        {
            options.setPageLimit((int)l);
        }
    }
    
    /**
     * See {@link TransformerConfig#getLimits(ContentTransformer, String, String)}.
     */
    public TransformationOptionLimits getLimits(ContentTransformer transformer, String sourceMimetype,
            String targetMimetype)
    {
        if (sourceMimetype == null)
        {
            sourceMimetype = ANY;
        }
        
        if (targetMimetype == null)
        {
            targetMimetype = ANY;
        }
        
        String name = (transformer == null) ? DEFAULT_TRANSFORMER : transformer.getName();

        DoubleMap<String, String, TransformationOptionLimits> transformerLimits = limits.get(name);
        
        TransformationOptionLimits limits = (transformerLimits == null) ? null : transformerLimits.get(sourceMimetype, targetMimetype);
        
        // Individual transformer limits might not exist.
        TransformationOptionLimits transformerWideLimits = (transformerLimits == null) ? null : transformerLimits.get(ANY, ANY);
        limits = (limits == null) ? transformerWideLimits : transformerWideLimits == null ? limits : transformerWideLimits.combine(limits);
        
        // If a non recursive call
        if (transformer != null)
        {
            // System wide 'default' limits should exist.
            TransformationOptionLimits systemWideLimits = getLimits(null, sourceMimetype, targetMimetype);
            limits = (limits == null) ? systemWideLimits : systemWideLimits.combine(limits);
        }
        
        return limits;
    }
}
