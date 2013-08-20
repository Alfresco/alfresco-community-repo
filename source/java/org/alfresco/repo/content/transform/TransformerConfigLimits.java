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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.TransformationOptionLimits;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Provides access to transformer limits defined via properties.
 * 
 * @author Alan Davis
 */
public class TransformerConfigLimits extends TransformerPropertyNameExtractor
{
    private static Log logger = LogFactory.getLog(TransformerConfigLimits.class);
    
    // Map using use, transformer, source mimetype and target mimetype to a set of limits.
    // Entries higher up the hierarchy are added so that values may be defaulted down.
    // Entries are added lower down the hierarchy when a search takes place.
    private Map<String, Map<String, DoubleMap<String, String, TransformationOptionLimits>>> limitsMap;

    // The 'use' value that had properties defined, including null for the default.
    private Set<String> uses;

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
        uses = new HashSet<String>();

        // Gets all the transformer, source and target combinations in properties that define limits.
        Map<TransformerSourceTargetSuffixKey, TransformerSourceTargetSuffixValue> allUseMap =
                getTransformerSourceTargetValuesMap(LIMIT_SUFFIXES, true, true, true, transformerProperties, mimetypeService);

        // Get the set 'use' values.
        uses.add(ANY);
        for (TransformerSourceTargetSuffixValue property: allUseMap.values())
        {
            String propertyUse = property.use == null ? ANY : property.use;
            uses.add(propertyUse);
        }

        // Populate the limitsMap for each 'use'.
        for (String use: uses)
        {
            // Add the system wide default just in case it is not included, as we always need this one
            TransformationOptionLimits limits = getOrCreateTransformerOptionLimits(DEFAULT_TRANSFORMER, ANY, ANY, use);

            Collection<TransformerSourceTargetSuffixValue> properties = getPropertiesForUse(use, allUseMap);
            for (int pass=0; pass<=3; pass++)
            {
                for (TransformerSourceTargetSuffixValue property: properties)
                {
                    int origLevel = getLevel(property.transformerName, property.sourceMimetype);
                    if (pass == origLevel)
                    {
                        String transformerName = (property.transformerName == null)
                                ? DEFAULT_TRANSFORMER : property.transformerName;
                        limits = getOrCreateTransformerOptionLimits(transformerName,
                                property.sourceMimetype, property.targetMimetype, use);
                        setTransformationLimitsFromProperties(limits, property.value, property.suffix);
                        debug("V", transformerName, property.sourceMimetype, property.targetMimetype, use, limits);
                    }
                }
            }
        }
    }

    // Returns the 'effective' properties for the given 'use'. These will be made up from the
    // properties defined for that use plus default properties that don't have a matching use
    // property.
    private Collection<TransformerSourceTargetSuffixValue> getPropertiesForUse(String use,
            Map<TransformerSourceTargetSuffixKey, TransformerSourceTargetSuffixValue> allUseMap)
    {
        Collection<TransformerSourceTargetSuffixValue> properties = new ArrayList<TransformerSourceTargetSuffixValue>();

        for (TransformerSourceTargetSuffixValue property: allUseMap.values())
        {
            String propertyUse = property.use == null ? ANY : property.use;
            if (propertyUse.equals(use))
            {
                properties.add(property);
            }
            else if (propertyUse.equals(ANY) &&
                    getProperty(property.transformerName, property.sourceExt, property.targetExt,
                            property.suffix, use, allUseMap) == null)
            {
                properties.add(property);
            }
        }

        return properties;
    }

    /**
     * Returns the TransformationOptionLimits for the use, transformer and mimetype combination,
     * creating and adding one if not already included.
     */
    private TransformationOptionLimits getOrCreateTransformerOptionLimits(String transformerName,
            String sourceMimetype, String targetMimetype, String use)
    {
        Map<String, DoubleMap<String, String, TransformationOptionLimits>> transformerLimits = limitsMap.get(use);
        if (transformerLimits == null)
        {
            transformerLimits = new ConcurrentHashMap<String, DoubleMap<String, String, TransformationOptionLimits>>();
            limitsMap.put(use, transformerLimits);
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

            limits = newTransformationOptionLimits(transformerName, sourceMimetype, targetMimetype, limits, use);
            mimetypeLimits.put(sourceMimetype, targetMimetype, limits);
        }
        else
        {
            debug("G", transformerName, sourceMimetype, targetMimetype, use, limits);
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
            String use)
    {
        int origLevel = getLevel(transformerName, sourceMimetype);

        TransformationOptionLimits limits = new TransformationOptionLimits();
        for (int level=0; level<origLevel; level++)
        {
              TransformationOptionLimits defaultLimits =
                  level < 2
                  ? level == 0
                    ? getOrCreateTransformerOptionLimits(DEFAULT_TRANSFORMER, ANY, ANY, use) // 0
                    : getOrCreateTransformerOptionLimits(DEFAULT_TRANSFORMER, sourceMimetype, targetMimetype, use) // 1
                  : level == 2
                    ? getOrCreateTransformerOptionLimits(transformerName, ANY, ANY, use) // 2
                    : getOrCreateTransformerOptionLimits(transformerName, sourceMimetype, targetMimetype, use); // 3

             defaultLimits.defaultTo(limits);
        }

        if (wildCardLimits != null)
        {
            wildCardLimits.defaultTo(limits);
        }

        debug("N", transformerName, sourceMimetype, targetMimetype, use, limits);
        
        return limits;
    }
    
    private int getLevel(String transformerName, String sourceMimetype)
    {
        boolean defaultMimetypes = sourceMimetype == null || sourceMimetype.equals(ANY);
        int level = transformerName == null || DEFAULT_TRANSFORMER.equals(transformerName)
             ? defaultMimetypes ? 0 : 1
             : defaultMimetypes ? 2 : 3;
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
    
    private void debug(String msg, String transformerName, String sourceMimetype, String targetMimetype, String use, TransformationOptionLimits limits)
    {
        if (logger.isDebugEnabled())
        {
            StringBuilder sb = new StringBuilder("");
            if (msg != null)
            {
                int x = getLevel(transformerName, sourceMimetype);
                sb.append(x);
                sb.append(' ');
                sb.append(msg);
                for (; x>-1; x--)
                {
                    sb.append(' ');
                }
                sb.append(transformerName);
                sb.append('.');
                sb.append(sourceMimetype);
                sb.append('.');
                sb.append(targetMimetype);
                sb.append('.');
                sb.append(use);
                sb.append('=');
                sb.append(limits.getMaxSourceSizeKBytes());
            }
            String line = sb.toString();
//          System.err.println(line);
            logger.debug(line);
        }
    }

    /**
     * See {@link TransformerConfig#getLimits(ContentTransformer, String, String, String)}.
     */
    public TransformationOptionLimits getLimits(ContentTransformer transformer, String sourceMimetype,
            String targetMimetype, String use)
    {
        String transformerName = (transformer == null) ? DEFAULT_TRANSFORMER : transformer.getName();
        
        if (sourceMimetype == null)
        {
            sourceMimetype = ANY;
        }
        
        if (targetMimetype == null)
        {
            targetMimetype = ANY;
        }

        if (use == null)
        {
            use = ANY;
        }

        debug(null, transformerName, sourceMimetype, targetMimetype, use, null);
        
        String searchUse = uses.contains(use) ? use : ANY;
        TransformationOptionLimits limits = getOrCreateTransformerOptionLimits(transformerName, sourceMimetype, targetMimetype, searchUse);
        
        debug("S", transformerName, sourceMimetype, targetMimetype, use, limits);
        
        return limits;
    }
}
