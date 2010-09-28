/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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

package org.alfresco.repo.forms.processor;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.forms.Field;

/**
 * Holds a FieldProcessor implementation for the fields that can be processed by
 * the FormProcessor.
 * 
 * @since 3.4
 * @author Nick Smith
 */
public class FieldProcessorRegistry 
{
    private final Map<String, FieldProcessor> processors = new HashMap<String, FieldProcessor>();
    
    /**
     * The {@link FieldProcessor} to be used if no specific processor can be found.
     */
    private FieldProcessor defaultProcessor;
    
    /**
     * Registers a {@link FieldProcessor} with this registry using the specified key.
     * @param key
     * @param processor
     */
    public void register(String key, FieldProcessor processor)
    {
        processors.put(key, processor);
    }
    
    /**
     * Returns the {@link FieldProcessor} that was registered witht he specified key.
     * @param key
     * @return
     */
    public FieldProcessor get(String key)
    {
        return processors.get(key);
    }

    /**
     * Attempts to build a {@link Field}. The method first tries to derive a key from the fieldname, then uses this key to look up a {@link FieldProcessor}.
     * This {@link FieldProcessor} is then used to generate a {@link Field}.
     * @param fieldName the name of the field to be created.
     * @param data A data object used by the {@link FieldProcessor} to build the {@link Field}.
     * @return a {@link Field} or <code>null</code>.
     */
    public Field buildField(String fieldName, FormCreationData data)
    {
        FieldProcessor fieldProcessor = getFieldProcessor(fieldName);
        if (fieldProcessor == null)
        {
            return null;
        }
        return fieldProcessor.generateField(fieldName, data);
    }

    /**
     * Returns a FieldProcessor for the given field name.
     * 
     * @param fieldName
     * @return The FieldProcessor implementation for the field or null if there isn't one regsitered.
     */
    protected FieldProcessor getFieldProcessor(String fieldName)
    {
        FieldProcessor fieldProcessor = get(getKey(fieldName));
        if (fieldProcessor == null && useDefaultProcessor(fieldName))
        {
            fieldProcessor = defaultProcessor;
        }
        return fieldProcessor;
    }

    /**
     * Determines if the defaultProcessor should be used.
     * @param fieldName
     * @return <code>true</code> if the defaultProcessor should be used, otherwise <code>false</code>.
     */
    protected boolean useDefaultProcessor(String fieldName)
    {
        return true;
    }

    /**
     * Derives the key used to look up the {@link FieldProcessor} from the fieldName.
     * @param fieldName
     * @return the key used to look up the {@link FieldProcessor}.
     */
    protected String getKey(String fieldName)
    {
        return fieldName;
    }

    /**
     * Sets the default field processor instance.
     * @param defaultProcessor the defaultProcessor to set
     */
    public void setDefaultProcessor(FieldProcessor defaultProcessor)
    {
        this.defaultProcessor = defaultProcessor;
    }
}
