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
import static org.alfresco.repo.content.transform.TransformerConfig.SUPPORTED;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.TransformationOptions;

/**
 * Provides access to the lists of supported and unsupported mimetype transformations
 * defined via properties for all transformers.
 * 
 * @author Alan Davis
 */
public class TransformerConfigSupported extends TransformerPropertyNameExtractor
{
    // Holds configured (entries only exist if configured rather than for all possible combinations)
    // of supported and unsupported mimetypes transformations for a transformer.
    // SourceMimetype and targetMimetype may be 'ANY' values to act as wild cards.
    private Map<String, SupportedAndUnsupportedTransformations> supported;

    public TransformerConfigSupported(TransformerProperties transformerProperties, MimetypeService mimetypeService)
    {
        setSupported(transformerProperties, mimetypeService);
    }
    
    /**
     * Sets the supported/unsupported mimetype transformations created from system properties.  
     */
    private void setSupported(TransformerProperties transformerProperties, MimetypeService mimetypeService)
    {
        supported = new HashMap<String, SupportedAndUnsupportedTransformations>();

        // Gets all the supported and unsupported transformer, source and target combinations
        Collection<TransformerSourceTargetSuffixValue> properties =
                getTransformerSourceTargetValues(Collections.singletonList(SUPPORTED),
                false, false, transformerProperties, mimetypeService);
        
        // Populate the transformer values
        for (TransformerSourceTargetSuffixValue property: properties)
        {
            SupportedAndUnsupportedTransformations supportedBytransformer = this.supported.get(property.transformerName);
            if (supportedBytransformer == null)
            {
                supportedBytransformer = new SupportedAndUnsupportedTransformations();
                this.supported.put(property.transformerName, supportedBytransformer);
            }
            boolean supported = property.value == null || property.value.equalsIgnoreCase("true");
            supportedBytransformer.put(property.sourceMimetype, property.targetMimetype, supported);
        }
    }

    /**
     * See {@link TransformerConfig#isSupportedTransformation(ContentTransformer, String, String, TransformationOptions)}.
     */
    public boolean isSupportedTransformation(ContentTransformer transformer, String sourceMimetype,
            String targetMimetype, TransformationOptions options)
    {
        if (sourceMimetype == null)
        {
            sourceMimetype = ANY;
        }
        
        if (targetMimetype == null)
        {
            targetMimetype = ANY;
        }
        
        boolean isSupported = true;
        String name = transformer.getName();
        SupportedAndUnsupportedTransformations supportedBytransformer = supported.get(name);
        if (supportedBytransformer != null)
        {
            isSupported = supportedBytransformer.isSupported(sourceMimetype, targetMimetype);
        }
        return isSupported;
    }
    
    private class SupportedAndUnsupportedTransformations
    {
        DoubleMap<String, String, Boolean> supportedTransformations;
        boolean supportedSet = false;
        
        SupportedAndUnsupportedTransformations()
        {
        }
        
        public void put(String sourceMimetype, String targetMimetype, boolean supported)
        {
            if (supportedTransformations == null)
            {
                supportedTransformations = new DoubleMap<String, String, Boolean>(ANY, ANY);
                if (supported)
                {
                    supportedSet = true;
                }
            }
            supportedTransformations.put(sourceMimetype, targetMimetype, supported);
        }

        boolean isSupported(String sourceMimetype, String targetMimetype)
        {
            // To be backward compatible, the default (ANY to ANY) transformation
            // needs to be true if only unsupported values are set or neither
            // unsupported nor supported values are set. If supported values are
            // set the default is false.
            boolean isSupported = !supportedSet;
            
            if (supportedTransformations != null)
            {
                Boolean sup = supportedTransformations.get(sourceMimetype, targetMimetype);
                if (sup != null)
                {
                    isSupported = sup;
                }
            }
            return isSupported;
        }
    }
}
