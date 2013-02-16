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
import static org.alfresco.repo.content.transform.TransformerConfig.MIMETYPES_SEPARATOR;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.management.subsystems.ChildApplicationContextFactory;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.util.Triple;

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
            MimetypeService mimetypeService, String propertySuffix)
    {
        setValues(subsystem, mimetypeService, propertySuffix);
    }

    /**
     * Sets the transformer values created from system properties.  
     */
    private void setValues(ChildApplicationContextFactory subsystem, MimetypeService mimetypeService, String propertySuffix)
    {
        values = new HashMap<String, DoubleMap<String, String, String>>();

        // Gets all the transformer, source and target combinations in properties that define
        // this value.
        Set<Triple<String, String, String>> transformerNamesAndMimetypes =
                getTransformerNamesAndExt(MIMETYPES_SEPARATOR, Collections.singletonList(propertySuffix), true, subsystem, mimetypeService);

        // Add the system wide default just in case it is not included, as we always need this one
        transformerNamesAndMimetypes.add(new Triple<String,String,String>(DEFAULT_TRANSFORMER, ANY, ANY));
        
        // Populate the transformer values
        for (Triple<String, String, String> triple: transformerNamesAndMimetypes)
        {
            String transformerName = triple.getFirst();
            String sourceExt = triple.getSecond();
            String targetExt = triple.getThird();
            String sourceMimetype = ANY.equals(sourceExt) ? ANY : mimetypeService.getMimetype(sourceExt);
            String targetMimetype = ANY.equals(targetExt) ? ANY : mimetypeService.getMimetype(targetExt);

            String value = newTransformerValue(transformerName, sourceExt, targetExt, subsystem, propertySuffix);
            
            DoubleMap<String, String, String> mimetypeLimits = this.values.get(transformerName);
            if (mimetypeLimits == null)
            {
                mimetypeLimits = new DoubleMap<String, String, String>(ANY, ANY);
                this.values.put(transformerName, mimetypeLimits);
            }
            mimetypeLimits.put(sourceMimetype, targetMimetype, value);
        }
    }
    
    /**
     * Returns a String object using property values.
     * @param transformerName
     * @param sourceExt is null for overall transformer options rather than for a specific mimetype pair
     * @param targetExt is null for overall transformer options rather than for a specific mimetype pair
     * @return a String object or null if not created
     */
    private String newTransformerValue(String transformerName, String sourceExt, String targetExt,
            ChildApplicationContextFactory subsystem, String propertySuffix)
    {
        String value = getValueFromProperties(transformerName, sourceExt, targetExt, subsystem, propertySuffix);
        
        // The overall values can be defined in another way 
        if (value == null && ANY.equals(sourceExt) && ANY.equals(targetExt))
        {
            value = getValueFromProperties(transformerName, null, null, subsystem, propertySuffix);
        }

        return value;
    }

    private String getValueFromProperties(String transformerName, String sourceExt, String targetExt,
            ChildApplicationContextFactory subsystem, String propertySuffix)
    {
        String propertyName = CONTENT+transformerName+
                (sourceExt == null ? "" : MIMETYPES_SEPARATOR+sourceExt+'.'+targetExt)+
                propertySuffix;
         String value = subsystem.getProperty(propertyName);
         return value;
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
