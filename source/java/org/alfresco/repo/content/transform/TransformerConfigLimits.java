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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.TransformationOptionLimits;

/**
 * Provides access to transformer limits defined via properties.
 * 
 * @author Alan Davis
 */
public class TransformerConfigLimits extends TransformerPropertyNameExtractor
{
    // Initially only holds configured (entries only exist if configured rather than for
    // all possible combinations) limits for each use, transformer, sourceMimeType and targetMimetype
    // combination. These initial entries are added to as other combinations are requested.
    // A null transformer is the system wide value. SourceMimeType and targetMimetype may be 'ANY'
    // values to act as wild cards.
    private Map<String, Map<String, DoubleMap<String, String, TransformationOptionLimits>>> limitsMap;

    public TransformerConfigLimits(TransformerProperties transformerProperties, MimetypeService mimetypeService)
    {
        setLimits(transformerProperties, mimetypeService);
    }

    /**
     * Sets the transformer limits created from system properties.  
     */
    private void setLimits(TransformerProperties transformerProperties, MimetypeService mimetypeService)
    {
        limitsMap = new ConcurrentHashMap<String, Map<String, DoubleMap<String, String, TransformationOptionLimits>>>();

        // Gets all the transformer, source and target combinations in properties that define limits.
        Map<TransformerSourceTargetSuffixKey, TransformerSourceTargetSuffixValue>
                transformerSourceTargetSuffixValues =
                        getTransformerSourceTargetValuesMap(LIMIT_SUFFIXES, true, true, transformerProperties, mimetypeService);
        Collection<TransformerSourceTargetSuffixValue> properties =
                transformerSourceTargetSuffixValues.values();

        // Add the system wide default just in case it is not included, as we always need this one
        TransformationOptionLimits limits = getOrCreateTransformerOptionLimits(DEFAULT_TRANSFORMER, ANY, ANY, null);

        // Populate the transformer limits. Done in several passes so that values may be defaulted
        // from one level to the next.
        for (int pass=0; pass<=7; pass++)
        {
            for (TransformerSourceTargetSuffixValue property: properties)
            {
                int origLevel = getLevel(property.transformerName, property.sourceMimetype, property.use);
                if (pass == origLevel)
                {
                    String transformerName = (property.transformerName == null)
                            ? DEFAULT_TRANSFORMER : property.transformerName;
                    limits = getOrCreateTransformerOptionLimits(transformerName,
                            property.sourceMimetype, property.targetMimetype, property.use);
                    setTransformationLimitsFromProperties(limits, property.value, property.suffix);
                }
            }
        }
    }

    /**
     * Returns the TransformationOptionLimits for the use, transformer and mimetype combination,
     * creating and adding one if not already included.
     */
    private TransformationOptionLimits getOrCreateTransformerOptionLimits(String transformerName,
            String sourceMimetype, String targetMimetype, String use)
    {
        use = use == null ? ANY : use; 
        return getOrCreateTransformerOptionLimitsInternal(transformerName, sourceMimetype, targetMimetype, use, use);
    }
    
    private TransformationOptionLimits getOrCreateTransformerOptionLimitsInternal(String transformerName,
            String sourceMimetype, String targetMimetype, String origUse, String use)
    {
        Map<String, DoubleMap<String, String, TransformationOptionLimits>> transformerLimits = limitsMap.get(origUse);
        if (transformerLimits == null)
        {
            transformerLimits = new ConcurrentHashMap<String, DoubleMap<String, String, TransformationOptionLimits>>();
            limitsMap.put(origUse, transformerLimits);
        }
        
        DoubleMap<String, String, TransformationOptionLimits> mimetypeLimits = transformerLimits.get(transformerName);
        if (mimetypeLimits == null)
        {
            mimetypeLimits = new DoubleMap<String, String, TransformationOptionLimits>(ANY, ANY);
            transformerLimits.put(transformerName, mimetypeLimits);
        }
        
        TransformationOptionLimits limits = mimetypeLimits.getNoWildcards(sourceMimetype, targetMimetype);
        if (limits == null)
        {
            // Try the wildcard version, and use any match as the basis for a new entry
            limits = mimetypeLimits.get(sourceMimetype, targetMimetype);

            limits = newTransformationOptionLimits(transformerName, sourceMimetype, targetMimetype, limits, origUse, use);
            mimetypeLimits.put(sourceMimetype, targetMimetype, limits);
        }
        return limits;
    }
    
    /**
     * Creates a new TransformationOptionLimits for the use, transformer and mimetype combination,
     * defaulting values from lower levels.
     * @param wildCardLimits if not null this is a limit found using a wildcard so should
     *        form the basis of the new object.
     */
    private TransformationOptionLimits newTransformationOptionLimits(String transformerName,
            String sourceMimetype, String targetMimetype, TransformationOptionLimits wildCardLimits,
            String origUse, String use)
    {
        int origLevel = getLevel(transformerName, sourceMimetype, use);

        TransformationOptionLimits limits = new TransformationOptionLimits();
        if (wildCardLimits != null)
        {
            wildCardLimits.defaultTo(limits);
        }

        int inc = (origLevel+1) % 2 + 1; // step = 1 if use is set otherwise 2
        for (int level=0; level<origLevel; level += inc)
        {
              TransformationOptionLimits defaultLimits =
                    level < 4
                    ? level < 2
                      ? level == 0
                        ? getOrCreateTransformerOptionLimitsInternal(DEFAULT_TRANSFORMER, ANY, ANY, origUse, ANY) // 0
                        : getOrCreateTransformerOptionLimitsInternal(DEFAULT_TRANSFORMER, ANY, ANY, origUse, use) // 1
                      : level == 2
                        ? getOrCreateTransformerOptionLimitsInternal(DEFAULT_TRANSFORMER, sourceMimetype, targetMimetype, origUse, ANY) // 2
                        : getOrCreateTransformerOptionLimitsInternal(DEFAULT_TRANSFORMER, sourceMimetype, targetMimetype, origUse, use) // 3
                    : level < 6
                      ? level == 4
                        ? getOrCreateTransformerOptionLimitsInternal(transformerName, ANY, ANY, origUse, ANY) // 4
                        : getOrCreateTransformerOptionLimitsInternal(transformerName, ANY, ANY, origUse, use) // 5
                      : getOrCreateTransformerOptionLimitsInternal(transformerName, sourceMimetype, targetMimetype, origUse, ANY); // 6

             defaultLimits.defaultTo(limits);
        }
        return limits;
    }

    private int getLevel(String transformerName, String sourceMimetype, String use)
    {
        boolean defaultUse = use == null || use.equals(ANY);
        boolean defaultMimetypes = sourceMimetype == null || sourceMimetype.equals(ANY);
        int level = transformerName == null || DEFAULT_TRANSFORMER.equals(transformerName)
                ? defaultMimetypes
                ? defaultUse ? 0 : 1
                : defaultUse ? 2 : 3
                : defaultMimetypes
                ? defaultUse ? 4 : 5
                : defaultUse ? 6 : 7;
        return level;
    }
    
    private void setTransformationLimitsFromProperties(TransformationOptionLimits limits,
            String value, String suffix)
    {
        long l = Long.parseLong(value);
        if (suffix == TransformerConfig.MAX_SOURCE_SIZE_K_BYTES)
        {
            limits.setReadLimitKBytes(-1);
            limits.setMaxSourceSizeKBytes(l);
        }
        else if (suffix == TransformerConfig.TIMEOUT_MS)
        {
            limits.setReadLimitTimeMs(-1);
            limits.setTimeoutMs(l);
        }
        else if (suffix == TransformerConfig.MAX_PAGES)
        {
            limits.setPageLimit(-1);
            limits.setMaxPages((int)l);
        }
        else if (suffix == TransformerConfig.READ_LIMIT_K_BYTES)
        {
            limits.setMaxSourceSizeKBytes(-1);
            limits.setReadLimitKBytes(l);
        }
        else if (suffix == TransformerConfig.READ_LIMIT_TIME_MS)
        {
            limits.setTimeoutMs(-1);
            limits.setReadLimitTimeMs(l);
        }
        else // if (suffix == TransformerConfig.PAGE_LIMIT)
        {
            limits.setMaxPages(-1);
            limits.setPageLimit((int)l);
        }
    }
    
    /**
     * See {@link TransformerConfig#getLimits(ContentTransformer, String, String, String)}.
     */
    public TransformationOptionLimits getLimits(ContentTransformer transformer, String sourceMimetype,
            String targetMimetype, String use)
    {
        if (sourceMimetype == null)
        {
            sourceMimetype = ANY;
        }
        
        if (targetMimetype == null)
        {
            targetMimetype = ANY;
        }
        
        String transformerName = (transformer == null) ? DEFAULT_TRANSFORMER : transformer.getName();

        return getOrCreateTransformerOptionLimits(transformerName, sourceMimetype, targetMimetype, use);
    }
}
