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
