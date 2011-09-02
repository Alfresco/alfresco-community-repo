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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.QName;

/**
 * @author Nick Smith
 * @since 3.4.e
 */
public class WorkflowPropertyConverter
{
    public static final Object SKIP = new Object();
    
    private final WorkflowObjectFactory factory;
    private final NodeConverter converter;
    
    
    public WorkflowPropertyConverter(WorkflowObjectFactory factory, NodeConverter converter)
    {
        this.factory = factory;
        this.converter = converter;
    }

    /**
     * Sets Properties of Task
     * 
     * @param instance
     *            task instance
     * @param properties
     *            properties to set
     */
    public Map<String, Object> getStringProperties(TypeDefinition taskDef, Map<QName, Serializable> properties)
    {
        if (properties == null)
        {
            return null;
        }

        Map<String, Object> results = new HashMap<String, Object>(properties.size());

        Map<QName, PropertyDefinition> taskProperties = taskDef.getProperties();
        Map<QName, AssociationDefinition> taskAssocs = taskDef.getAssociations();

        // map each parameter to task
        for (Entry<QName, Serializable> entry : properties.entrySet())
        {
            QName key = entry.getKey();
            Serializable value = entry.getValue();
            Object actualValue = getValue(key, value, taskProperties, taskAssocs);

            String name = factory.mapQNameToName(key);
            results.put(name, actualValue);
        }
        return results;
    }

    private Object getValue(QName key, Serializable value,
                Map<QName, PropertyDefinition> taskProperties,
                Map<QName, AssociationDefinition> taskAssocs)
    {
        
        PropertyDefinition propDef = taskProperties.get(key);
        if (propDef != null)
        {
            return getPropertyValue(key, value, propDef);
        }
        AssociationDefinition assocDef = taskAssocs.get(key);
        if (assocDef != null)
        {
            return getAssociation(key, value, assocDef);
        }
        // untyped value, perform minimal conversion
        if (value instanceof NodeRef)
        {
            return convertNodeRefs(false, value);
        }
        return value;
    }

    private Object getAssociation(QName key, Serializable value, AssociationDefinition assocDef)
    {
        return convertNodeRefs(assocDef.isTargetMany(), value);
    }

    private Object getPropertyValue(QName key, Serializable value, PropertyDefinition propDef)
    {
        if (propDef.isProtected())
        {
            // NOTE: only write non-protected properties
            return SKIP;
        }

        Object result = null;
        DataTypeDefinition dataTypeDef = propDef.getDataType();
        if (value instanceof Collection<?>)
        {
            result = DefaultTypeConverter.INSTANCE.convert(dataTypeDef, (Collection<?>) value);
        }
        else
        {
            result = DefaultTypeConverter.INSTANCE.convert(dataTypeDef, value);
        }
        // convert NodeRefs to JBPMNodes
        if (dataTypeDef.getName().equals(DataTypeDefinition.NODE_REF))
        {
            result = convertNodeRefs(propDef.isMultiValued(), result);
        }
        return result;
    }
    
    /**
     * Convert a Repository association to JBPMNodeList or JBPMNode
     * 
     * @param isMany
     *            true => force conversion to list
     * @param value
     *            value to convert
     * @return JBPMNodeList or JBPMNode
     */
    @SuppressWarnings("unchecked")
    private Object convertNodeRefs(boolean isMany, Object value)
    {
        if (value instanceof NodeRef)
        {
            NodeRef node = (NodeRef) value;
            if (isMany)
            {
                return converter.convertNodes(Collections.singletonList(node));
            }
            else
            {
                return converter.convertNode(node);
            }
        }
        if (value instanceof Collection<?>)
        {
            Collection<NodeRef> nodes = (Collection<NodeRef>) value;
            if (isMany)
            {
                converter.convertNodes(nodes);
            }
            else
            {
                NodeRef node = nodes.size()==0?null:nodes.iterator().next();
                converter.convertNode(node);
            }
        }
        return value;
    }
}
