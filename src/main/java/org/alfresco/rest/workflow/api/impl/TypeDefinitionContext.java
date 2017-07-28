/*
 * #%L
 * Alfresco Remote API
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
package org.alfresco.rest.workflow.api.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.alfresco.repo.workflow.WorkflowQNameConverter;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.namespace.QName;

/**
 * Helper contxt class used when checking variable types based on {@link TypeDefinition}. 
 *
 * @author Frederik Heremans
 */
public class TypeDefinitionContext {
    
    private Map<String, PropertyDefinition> propertyDefinitions;
    private Map<String, AssociationDefinition> associationDefinitions;

    public TypeDefinitionContext(TypeDefinition typeDefinition, WorkflowQNameConverter qNameConverter)
    {
        propertyDefinitions = new HashMap<String, PropertyDefinition>();
        associationDefinitions = new HashMap<String, AssociationDefinition>();
        
        for (Entry<QName, PropertyDefinition> entry : typeDefinition.getProperties().entrySet())
        {
            propertyDefinitions.put(qNameConverter.mapQNameToName(entry.getKey()), entry.getValue());
        }
        
        for (Entry<QName, AssociationDefinition> entry : typeDefinition.getAssociations().entrySet())
        {
            associationDefinitions.put(qNameConverter.mapQNameToName(entry.getKey()), entry.getValue());
        }
    }
    
    public PropertyDefinition getPropertyDefinition(String rawVariableName) 
    {
        return propertyDefinitions.get(rawVariableName);
    }
    
    public AssociationDefinition getAssociationDefinition(String rawVariableName)
    {
        return associationDefinitions.get(rawVariableName);
    }
}
