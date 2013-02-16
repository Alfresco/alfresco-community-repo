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
import static org.alfresco.repo.content.transform.TransformerConfig.CONTENT;
import static org.alfresco.repo.content.transform.TransformerConfig.DEFAULT_TRANSFORMER;
import static org.alfresco.repo.content.transform.TransformerConfig.LIMIT_SUFFIXES;
import static org.alfresco.repo.content.transform.TransformerConfig.MIMETYPES_SEPARATOR;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.management.subsystems.ChildApplicationContextFactory;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.TransformationOptionLimits;
import org.alfresco.util.Triple;

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
        Set<Triple<String, String, String>> transformerNamesAndExt =
                getTransformerNamesAndExt(MIMETYPES_SEPARATOR, LIMIT_SUFFIXES, true, subsystem, mimetypeService);

        // Add the system wide default just in case it is not included, as we always need this one
        transformerNamesAndExt.add(new Triple<String,String,String>(DEFAULT_TRANSFORMER, ANY, ANY));
        
        // Populate the transformer limits
        for (Triple<String, String, String> triple: transformerNamesAndExt)
        {
            String transformerName = triple.getFirst();
            String sourceExt = triple.getSecond();
            String targetExt = triple.getThird();
            String sourceMimetype = ANY.equals(sourceExt) ? ANY : mimetypeService.getMimetype(sourceExt);
            String targetMimetype = ANY.equals(targetExt) ? ANY : mimetypeService.getMimetype(targetExt);

            TransformationOptionLimits limits = newTransformationOptionLimits(transformerName, sourceExt, targetExt, subsystem);
            
            DoubleMap<String, String, TransformationOptionLimits> mimetypeLimits =
                    this.limits.get(transformerName);
            if (mimetypeLimits == null)
            {
                mimetypeLimits = new DoubleMap<String, String, TransformationOptionLimits>(ANY, ANY);
                this.limits.put(transformerName, mimetypeLimits);
            }
            mimetypeLimits.put(sourceMimetype, targetMimetype, limits);
        }
    }
    
    /**
     * Returns a TransformationOptionLimits object using property values.
     * @param transformerName
     * @param sourceExt is null for overall transformer options rather than for a specific mimetype pair
     * @param targetExt is null for overall transformer options rather than for a specific mimetype pair
     * @return a TransformationOptionLimits object or null if not created
     */
    private TransformationOptionLimits newTransformationOptionLimits(String transformerName,
            String sourceExt, String targetExt, ChildApplicationContextFactory subsystem)
    {
        TransformationOptionLimits limits = new TransformationOptionLimits();
        
        // The overall values can be defined in two ways 
        if (ANY.equals(sourceExt) && ANY.equals(targetExt))
        {
            setTransformationOptionsFromProperties(limits, transformerName, null, null, subsystem);
        }
        setTransformationOptionsFromProperties(limits, transformerName, sourceExt, targetExt, subsystem);

        return limits;
    }

    private void setTransformationOptionsFromProperties(TransformationOptionLimits limits,
            String transformerName, String sourceExt, String targetExt, ChildApplicationContextFactory subsystem)
    {
        String propertyNameRoot = CONTENT+transformerName+
                (sourceExt == null ? "" : MIMETYPES_SEPARATOR+sourceExt+'.'+targetExt);
        int i = 0;
        for (String suffix: LIMIT_SUFFIXES)
        {
            String value = subsystem.getProperty(propertyNameRoot+suffix);
            if (value != null)
            {
                long l = Long.parseLong(value);
                switch (i)
                {
                case 0:
                    limits.setMaxSourceSizeKBytes(l);
                    break;
                case 1:
                    limits.setTimeoutMs(l);
                    break;
                case 2:
                    limits.setMaxPages((int)l);
                    break;
                case 3:
                    limits.setReadLimitKBytes(l);
                    break;
                case 4:
                    limits.setReadLimitTimeMs(l);
                    break;
                case 5:
                    limits.setPageLimit((int)l);
                    break;
                }
            }
            i++;
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
