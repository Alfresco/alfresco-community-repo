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
package org.alfresco.repo.node.integrity;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.crypto.SealedObject;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.Constraint;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.ConstraintException;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Event raised to check nodes
 * 
 * @author Derek Hulley
 */
public class PropertiesIntegrityEvent extends AbstractIntegrityEvent
{
    private static Log logger = LogFactory.getLog(PropertiesIntegrityEvent.class);
    
    protected PropertiesIntegrityEvent(
            NodeService nodeService,
            DictionaryService dictionaryService,
            NodeRef nodeRef)
    {
        super(nodeService, dictionaryService, nodeRef, null, null);
    }
    
    public void checkIntegrity(List<IntegrityRecord> eventResults)
    {
        NodeRef nodeRef = getNodeRef();
        if (!nodeService.exists(nodeRef))
        {
            // node has gone
            if (logger.isDebugEnabled())
            {
                logger.debug("Event ignored - node gone: " + this);
            }
            eventResults.clear();
            return;
        }
        else
        {
            checkAllProperties(getNodeRef(), eventResults);
        }
    }

    /**
     * Checks the properties for the type and aspects of the given node.
     */
    private void checkAllProperties(NodeRef nodeRef, List<IntegrityRecord> eventResults)
    {
        // get all properties for the node
        Map<QName, Serializable> nodeProperties = nodeService.getProperties(nodeRef);
        
        // get the node type
        QName nodeTypeQName = nodeService.getType(nodeRef);
        // get property definitions for the node type
        TypeDefinition typeDef = dictionaryService.getType(nodeTypeQName);
        if (typeDef == null)
        {
            // Type not found, so ignore properties
            return;
        }
        Collection<PropertyDefinition> propertyDefs = typeDef.getProperties().values();
        // check them
        checkAllProperties(nodeRef, nodeTypeQName, propertyDefs, nodeProperties, eventResults);
        
        // get the node aspects
        Set<QName> aspectTypeQNames = nodeService.getAspects(nodeRef);
        for (QName aspectTypeQName : aspectTypeQNames)
        {
            // Shortcut sys:referencable
            if (aspectTypeQName.equals(ContentModel.ASPECT_REFERENCEABLE))
            {
                continue;
            }
            // Shortcut cm:auditable
            if (aspectTypeQName.equals(ContentModel.ASPECT_AUDITABLE))
            {
                continue;
            }
            
            // get property definitions for the aspect
            AspectDefinition aspectDef = dictionaryService.getAspect(aspectTypeQName);
            if (aspectDef == null)
            {
                // Aspect not found, so can't check properties
                continue;
            }
            propertyDefs = aspectDef.getProperties().values();
            // check them
            checkAllProperties(nodeRef, aspectTypeQName, propertyDefs, nodeProperties, eventResults);
        }
        // done
    }

    /**
     * Checks the specific map of properties against the required property definitions
     * 
     * @param nodeRef the node to which this applies
     * @param typeQName the qualified name of the aspect or type to which the properties belong
     * @param propertyDefs the definitions to check against - may be null or empty
     * @param nodeProperties the properties to check
     */
    private void checkAllProperties(
            NodeRef nodeRef,
            QName typeQName,
            Collection<PropertyDefinition> propertyDefs,
            Map<QName, Serializable> nodeProperties,
            Collection<IntegrityRecord> eventResults)
    {
        // check for null or empty definitions
        if (propertyDefs == null || propertyDefs.isEmpty())
        {
            return;
        }
        for (PropertyDefinition propertyDef : propertyDefs)
        {
            QName propertyQName = propertyDef.getName();
            // check that enforced, mandatoryproperties are set
            if (propertyDef.isMandatory() && propertyDef.isMandatoryEnforced() && !nodeProperties.containsKey(propertyQName))
            {
                IntegrityRecord result = new IntegrityRecord(
                        "Mandatory property not set: \n" +
                        "   Node: " + nodeRef + "\n" +
                        "   Type: " + typeQName + "\n" +
                        "   Property: " + propertyQName);
                eventResults.add(result);
                // next one
                continue;
            }
            Serializable propertyValue = nodeProperties.get(propertyQName);
            // Check for encryption first
            if (propertyDef.getDataType().getName().equals(DataTypeDefinition.ENCRYPTED))
            {
                if (propertyValue != null && !(propertyValue instanceof SealedObject))
                {
                    IntegrityRecord result = new IntegrityRecord(
                            "Property must be encrypted: \n" +
                            "   Node: " + nodeRef + "\n" +
                            "   Type: " + typeQName + "\n" +
                            "   Property: " + propertyQName);
                    eventResults.add(result);
                }
            }
            // check constraints
            List<ConstraintDefinition> constraintDefs = propertyDef.getConstraints();
            for (ConstraintDefinition constraintDef : constraintDefs)
            {
                // get the constraint implementation
                Constraint constraint = constraintDef.getConstraint();
                try
                {
                    constraint.evaluate(propertyValue);
                }
                catch (ConstraintException e)
                {
                    IntegrityRecord result = new IntegrityRecord(
                            "Invalid property value: \n" +
                            "   Node: " + nodeRef + "\n" +
                            "   Type: " + typeQName + "\n" +
                            "   Property: " + propertyQName + "\n" +
                            "   Constraint: " + e.getMessage());
                    eventResults.add(result);
                    // next one
                    continue;
                }
            }
        }
    }
}
