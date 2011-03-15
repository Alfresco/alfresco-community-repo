/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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

package org.alfresco.repo.workflow;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.namespace.QName;

/**
 * @since 3.4.e
 * @author Nick Smith
 *
 */
public class WorkflowPropertyHandlerRegistry
{
    private final Map<QName, WorkflowPropertyHandler> handlers = new HashMap<QName, WorkflowPropertyHandler>();

    private final WorkflowPropertyHandler defaultHandler;
    private final WorkflowQNameConverter qNameConverter;
    
    /**
     * @param defaultHandler
     * @param qNameConverter
     */
    public WorkflowPropertyHandlerRegistry(WorkflowPropertyHandler defaultHandler, WorkflowQNameConverter qNameConverter)
    {
        this.defaultHandler = defaultHandler;
        this.qNameConverter = qNameConverter;
    }

    public void registerHandler(QName key, WorkflowPropertyHandler handler)
    {
        handlers.put(key, handler);
    }
    
    public void clear()
    {
        handlers.clear();
    }
    
    public Map<String, Object> handleVariablesToSet(Map<QName, Serializable> properties, 
                TypeDefinition type,
                Object object, Class<?> objectType)
    {
        Map<String, Object> variablesToSet = new HashMap<String, Object>();
        for (Entry<QName, Serializable> entry : properties.entrySet())
        {
            QName key = entry.getKey();
            Serializable value = entry.getValue();
            WorkflowPropertyHandler handler = handlers.get(key);
            if(handler == null)
            {
                handler = defaultHandler;
            }
            Object result = handler.handleProperty(key, value, type, object, objectType);
            if(WorkflowPropertyHandler.DO_NOT_ADD.equals(result)==false) 
            {
                String keyStr = qNameConverter.mapQNameToName(key);
                variablesToSet.put(keyStr, result);
            }
        }
        return variablesToSet;
    }
    
}
