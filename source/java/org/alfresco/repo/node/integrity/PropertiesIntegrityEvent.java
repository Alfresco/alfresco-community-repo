/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.node.integrity;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.Constraint;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.ConstraintException;
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
     * 
     * @param nodeRef
     * @param eventResults
     */
    private void checkAllProperties(NodeRef nodeRef, List<IntegrityRecord> eventResults)
    {
        // get all properties for the node
        Map<QName, Serializable> nodeProperties = nodeService.getProperties(nodeRef);
        
        // get the node type
        QName nodeTypeQName = nodeService.getType(nodeRef);
        // get property definitions for the node type
        TypeDefinition typeDef = dictionaryService.getType(nodeTypeQName);
        Collection<PropertyDefinition> propertyDefs = typeDef.getProperties().values();
        // check them
        checkAllProperties(nodeRef, nodeTypeQName, propertyDefs, nodeProperties, eventResults);
        
        // get the node aspects
        Set<QName> aspectTypeQNames = nodeService.getAspects(nodeRef);
        for (QName aspectTypeQName : aspectTypeQNames)
        {
            // get property definitions for the aspect
            AspectDefinition aspectDef = dictionaryService.getAspect(aspectTypeQName);
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
