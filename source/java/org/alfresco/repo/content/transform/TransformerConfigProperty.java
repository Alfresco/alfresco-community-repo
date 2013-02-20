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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.management.subsystems.ChildApplicationContextFactory;
import org.alfresco.service.cmr.repository.MimetypeService;

/**
 * Provides access to single transformer configuration property depending on the
 * transformer and source and target mimetypes, falling back to defaults.
 *  
 * @author Alan Davis
 */
public class TransformerConfigProperty  extends TransformerPropertyNameExtractor
{
    private Map<String, DoubleMap<String, String, String>> values;

    public TransformerConfigProperty(ChildApplicationContextFactory subsystem,
            MimetypeService mimetypeService, String propertySuffix, String defaultValue)
    {
        setValues(subsystem, mimetypeService, propertySuffix, defaultValue);
    }

    /**
     * Sets the transformer values created from system properties.  
     */
    private void setValues(ChildApplicationContextFactory subsystem, MimetypeService mimetypeService,
            String suffix, String defaultValue)
    {
        values = new HashMap<String, DoubleMap<String, String, String>>();

        // Gets all the transformer, source and target combinations in properties that define
        // this value.
        Collection<TransformerSourceTargetValue> transformerNamesAndMimetypes =
                getTransformerSourceTargetValues(Collections.singletonList(suffix), true, subsystem, mimetypeService);

        // Add the system wide default if it does not exist, as we always need this one
        TransformerSourceTargetValue transformerSourceTargetValue = 
                new TransformerSourceTargetValue(DEFAULT_TRANSFORMER, ANY, ANY, defaultValue, suffix, mimetypeService);
        if (transformerNamesAndMimetypes.contains(transformerSourceTargetValue.key()))
        {
            transformerNamesAndMimetypes.add(transformerSourceTargetValue);
        }
        
        // Populate the transformer values
        for (TransformerSourceTargetValue property: transformerNamesAndMimetypes)
        {
            DoubleMap<String, String, String> mimetypeLimits = this.values.get(property.transformerName);
            if (mimetypeLimits == null)
            {
                mimetypeLimits = new DoubleMap<String, String, String>(ANY, ANY);
                this.values.put(property.transformerName, mimetypeLimits);
            }
            mimetypeLimits.put(property.sourceMimetype, property.targetMimetype, property.value);
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
    {
        return Long.parseLong(getString(transformer, sourceMimetype, targetMimetype));
    }
    
    public int getInt(ContentTransformer transformer, String sourceMimetype, String targetMimetype)
    {
        return Integer.parseInt(getString(transformer, sourceMimetype, targetMimetype));
    }
}
