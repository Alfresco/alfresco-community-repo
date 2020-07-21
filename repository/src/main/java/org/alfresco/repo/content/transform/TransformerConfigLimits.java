/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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

import static org.alfresco.repo.content.transform.TransformerConfig.ANY;
import static org.alfresco.repo.content.transform.TransformerConfig.DEFAULT_TRANSFORMER;
import static org.alfresco.repo.content.transform.TransformerConfig.LIMIT_SUFFIXES;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.TransformationOptionLimits;
import org.alfresco.service.cmr.repository.TransformationOptionPair;
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
    
    private static final String NOT_THROWN_MESSAGE = "Both max and limit should not be set.";

    // Map using use, transformer, source mimetype and target mimetype to a set of limits.
    // Entries higher up the hierarchy are added so that values may be defaulted down.
    // Entries are added lower down the hierarchy when a search takes place.
    private Map<String, Map<String, DoubleMap<String, String, TransformationOptionLimits>>> limitsMap;

    // The 'use' value that had properties defined, including ANY for the default.
    private Set<String> uses;

    public TransformerConfigLimits(TransformerProperties transformerProperties, MimetypeService mimetypeService)
    {
        setLimits(transformerProperties, mimetypeService);
    }

    /**
     * Sets the transformer limits created from properties.  
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
            Collection<TransformerSourceTargetSuffixValue> properties = getPropertiesForUse(use, allUseMap);
            
            // Add the system wide default just in case it is not included, as we always need this one
            getOrCreateTransformerOptionLimits(DEFAULT_TRANSFORMER, ANY, ANY, use);
            
            TransformationOptionLimits limits;
            for (int pass=0; pass<=3; pass++)
            {
                for (TransformerSourceTargetSuffixValue property1: properties)
                {
                    int origLevel = getLevel(property1.transformerName, property1.sourceMimetype);
                    if (pass == origLevel)
                    {
                        logger.debug(property1);
                        String transformerName = (property1.transformerName == null)
                                ? DEFAULT_TRANSFORMER : property1.transformerName;
                        limits = getOrCreateTransformerOptionLimits(transformerName,
                                property1.sourceMimetype, property1.targetMimetype, use);
                        setTransformationLimitsFromProperties(limits, property1.value, property1.suffix);
                        debug("V", transformerName, property1.sourceMimetype, property1.targetMimetype, use, limits);
                    }
                }
            }
        }
        logger.debug(this);
    }

    /**
     * Sets the transformer limits for a single use from properties. Method extracted so that
     * it is possible to write a simpler unit test, that changes to the order of the
     * properties. The order changed between Java 6, 7 and 8, resulting in MNT-14295. The original
     * outer method cannot be used as it creates the list from a map (allUseMap) that it also
     * creates and the order of values from that map cannot be controlled from a test.
     */
    void setLimits(String use, Collection<TransformerSourceTargetSuffixValue> properties)
    {
        // Add the system wide default just in case it is not included, as we always need this one
        getOrCreateTransformerOptionLimits(DEFAULT_TRANSFORMER, ANY, ANY, use);

        TransformationOptionLimits limits;
        for (int pass=0; pass<=3; pass++)
        {
            for (TransformerSourceTargetSuffixValue property: properties)
            {
                int origLevel = getLevel(property.transformerName, property.sourceMimetype);
                if (pass == origLevel)
                {
                    logger.debug(property);
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

    /**
     * Returns the 'effective' properties for the given 'use'.
     * 
     * These will be made up from the properties defined for that use plus default properties
     * that don't have a matching use property, as long as there is not a matching use at a
     * higher level.<p>
     * 
     * <li>If there is a system wide property with the use value, all other properties without
     *     the same use value are ignored.</li>
     * <li>If there is a transformer wide property with this use value, all other transformer
     *     wide properties for the same transformer without a use value are ignored.
     * <li>If there is mimetype property with the use value, the default property for
     *     the same combination is ignored.</li>
     * @param use value such as "doclib" or "index"
     * @param allUseMap the complete set of transformer properties that includes blank and all
     *        use values. 
     * @return a set of properties for the specific use.
     */
    private Collection<TransformerSourceTargetSuffixValue> getPropertiesForUse(String use,
            Map<TransformerSourceTargetSuffixKey, TransformerSourceTargetSuffixValue> allUseMap)
    {
        Collection<TransformerSourceTargetSuffixValue> properties = new ArrayList<TransformerSourceTargetSuffixValue>();
        
        boolean systemWideUse = false;
        Set<String> transformerWideUse = new HashSet<>();
        for (TransformerSourceTargetSuffixValue property: allUseMap.values())
        {
            String propertyUse = property.use == null ? ANY : property.use;
            if (propertyUse.equals(use))
            {
                if (DEFAULT_TRANSFORMER.equals(property.transformerName))
                {
                    systemWideUse = true;
                    break;
                }
                
                transformerWideUse.add(property.transformerName);
            }
        }

        for (TransformerSourceTargetSuffixValue property: allUseMap.values())
        {
            String propertyUse = property.use == null ? ANY : property.use;
            if (propertyUse.equals(use))
            {
                properties.add(property);
            }
            else if (!systemWideUse && propertyUse.equals(ANY))
            {
                if (DEFAULT_TRANSFORMER.equals(property.transformerName) ||
                    !transformerWideUse.contains(property.transformerName))
                {
                    // If there is NOT a similar 'use' property... 
                    if (getProperty(property.transformerName, property.sourceExt, property.targetExt,
                                property.suffix, use, allUseMap) == null)
                    {
                        properties.add(property);
                    }
                }
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
    
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        for (Entry<String, Map<String, DoubleMap<String, String, TransformationOptionLimits>>> useEntry: limitsMap.entrySet())
        {
            for (Entry<String, DoubleMap<String, String, TransformationOptionLimits>> transformerEntry: useEntry.getValue().entrySet())
            {
                if (sb.length() > 0)
                {
                    sb.append("\n");
                }
                sb.append(useEntry.getKey()).
                    append(", ").
                    append(transformerEntry.getKey()).
                    append(" =>\n").
                    append(transformerEntry.getValue());
            }
        }
        return sb.toString();
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
        // Start at the most general limits and then override with more specific values so that
        // defaults from the most general get used if there is not something more specific.
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
    
    /**
     * Sets a TransformationOptionLimits value. This may be a size in K bytes,
     * a time in ms or number of pages. It may also be a maximum or limit value.
     * When the maximum and limit are both set and the same, the maximum value
     * wins.
     */
    private void setTransformationLimitsFromProperties(TransformationOptionLimits limits,
            String value, String suffix)
    {
        long newValue = Long.parseLong(value);
        TransformationOptionPair optionPair =
            suffix == TransformerConfig.MAX_SOURCE_SIZE_K_BYTES ||
            suffix == TransformerConfig.READ_LIMIT_K_BYTES
          ? limits.getKBytesPair()
          : suffix == TransformerConfig.TIMEOUT_MS ||
            suffix == TransformerConfig.READ_LIMIT_TIME_MS
          ? limits.getTimePair()
          : limits.getPagesPair();
       
        // If max rather than limit value
        if (suffix == TransformerConfig.MAX_SOURCE_SIZE_K_BYTES ||
            suffix == TransformerConfig.TIMEOUT_MS ||
            suffix == TransformerConfig.MAX_PAGES)
        {
            long limit = optionPair.getLimit();
            if (limit < 0 || limit >= newValue)
            {
                optionPair.setLimit(-1, NOT_THROWN_MESSAGE);
                optionPair.setMax(newValue, NOT_THROWN_MESSAGE);
            }
        }
        else
        {
            long max = optionPair.getMax();
            if (max < 0 || max > newValue)
            {
                optionPair.setMax(-1, NOT_THROWN_MESSAGE);
                optionPair.setLimit(newValue, NOT_THROWN_MESSAGE);
            }
        }
    }
    
    private void debug(String msg, String transformerName, String sourceMimetype,
        String targetMimetype, String use, TransformationOptionLimits limits)
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
                sb.append(limits);
            }
            String line = sb.toString();
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
