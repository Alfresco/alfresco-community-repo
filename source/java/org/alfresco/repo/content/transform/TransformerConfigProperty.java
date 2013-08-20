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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.repository.MimetypeService;

/**
 * Provides access to a single transformer configuration property depending on the
 * transformer and source and target mimetypes, falling back to defaults.
 *  
 * @author Alan Davis
 */
public class TransformerConfigProperty  extends TransformerPropertyNameExtractor
{
    private Map<String, DoubleMap<String, String, String>> values;

    public TransformerConfigProperty(TransformerProperties transformerProperties,
            MimetypeService mimetypeService, String propertySuffix, String defaultValue)
    {
        setValues(transformerProperties, mimetypeService, propertySuffix, defaultValue);
    }

    /**
     * Sets the transformer values created from system properties.  
     */
    private void setValues(TransformerProperties transformerProperties, MimetypeService mimetypeService,
            String suffix, String defaultValue)
    {
        values = new HashMap<String, DoubleMap<String, String, String>>();

        // Gets all the transformer, source and target combinations in properties that define
        // this value.
        Map<TransformerSourceTargetSuffixKey, TransformerSourceTargetSuffixValue> properties =
                getTransformerSourceTargetValuesMap(Collections.singletonList(suffix), true, true, false, transformerProperties, mimetypeService);

        // Add the system wide default if it does not exist, as we always need this one
        TransformerSourceTargetSuffixValue transformerSourceTargetValue = 
                new TransformerSourceTargetSuffixValue(DEFAULT_TRANSFORMER, ANY, ANY, suffix, null, defaultValue, mimetypeService);
        TransformerSourceTargetSuffixKey key = transformerSourceTargetValue.key();
        if (!properties.containsKey(key))
        {
            properties.put(key, transformerSourceTargetValue);
        }
        
        // Populate the transformer values
        for (TransformerSourceTargetSuffixValue property: properties.values())
        {
            DoubleMap<String, String, String> mimetypeValues = values.get(property.transformerName);
            if (mimetypeValues == null)
            {
                mimetypeValues = new DoubleMap<String, String, String>(ANY, ANY);
                values.put(property.transformerName, mimetypeValues);
            }
            mimetypeValues.put(property.sourceMimetype, property.targetMimetype, property.value);
        }
    }
    
    private String getString(ContentTransformer transformer, String sourceMimetype,
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

        DoubleMap<String, String, String> mimetypeLimits = values.get(name);
        
        String value = (mimetypeLimits == null) ? null : mimetypeLimits.get(sourceMimetype, targetMimetype);

        if (value == null && transformer != null)
        {
            // System wide 'default' limits should exist, but individual transformer values might not.
            value = getString(null, sourceMimetype, targetMimetype);
        }
        
        return value;
    }

    public long getLong(ContentTransformer transformer, String sourceMimetype, String targetMimetype)
        throws NumberFormatException
    {
        return Long.parseLong(getString(transformer, sourceMimetype, targetMimetype));
    }
    
    public int getInt(ContentTransformer transformer, String sourceMimetype, String targetMimetype)
        throws NumberFormatException
    {
        return Integer.parseInt(getString(transformer, sourceMimetype, targetMimetype));
    }
}
