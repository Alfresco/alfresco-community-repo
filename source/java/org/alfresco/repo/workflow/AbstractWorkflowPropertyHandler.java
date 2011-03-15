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

import org.alfresco.repo.i18n.MessageService;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.repository.datatype.TypeConverter;
import org.alfresco.service.cmr.workflow.WorkflowException;
import org.alfresco.service.namespace.QName;

/**
 * @since 3.4.e
 * @author Nick Smith
 *
 */
public abstract class AbstractWorkflowPropertyHandler implements WorkflowPropertyHandler
{
    private static final String ERR_SET_TASK_PROPS_INVALID_VALUE = "activiti.engine.set.task.properties.invalid.value";
    private  static final TypeConverter typeConverter = DefaultTypeConverter.INSTANCE;
    
    protected WorkflowNodeConverter nodeConverter;
    protected MessageService messageService;
    
    /**
     * @param task 
     * @param value
     * @param assocDef
     */
    protected Object handleAssociation(Serializable value, AssociationDefinition assocDef)
    {
        if(assocDef.isProtected())
        {
            return DO_NOT_ADD;
        }
        // Convert association to ActivitiScriptNode / List
        return convertAssociationValue(assocDef, value);
    }

    /**
     * @param task 
     * @param value
     * @param propDef
     * @return
     */
    protected Object handleProperty(Serializable value, PropertyDefinition propDef)
    {
        if (propDef.isProtected())
        {
            // Protected properties are ignored
            return DO_NOT_ADD;
        }
        // The value is converted to the correct type
        return convertPropertyValue(propDef, value);
    }

    protected Object convertPropertyValue(PropertyDefinition propDef, Serializable value)
    {
        Object newValue = value;
        // Convert property value using a default type converter
        if (value instanceof Collection<?>)
        {
            // Convert a collecion of values
            newValue =typeConverter.convert(propDef.getDataType(), (Collection<?>) value);
        }
        else
        {
            // Convert a single value
            newValue = typeConverter.convert(propDef.getDataType(), value);
        }

        // Convert NodeRefs to ActivitiScriptNodes
        DataTypeDefinition dataTypeDef = propDef.getDataType();
        if (dataTypeDef.getName().equals(DataTypeDefinition.NODE_REF))
        {
            newValue = nodeConverter.convertNodes(newValue, propDef.isMultiValued());
        }
        return newValue;
    }

    protected Object convertAssociationValue(AssociationDefinition assocDef, Serializable value)
    {
        return nodeConverter.convertNodes(value, assocDef.isTargetMany());
    }
    
    protected WorkflowException getInvalidPropertyValueException(QName key, Object value)
    {
        String msg = messageService.getMessage(ERR_SET_TASK_PROPS_INVALID_VALUE, value, key);
        return new WorkflowException(msg);
    }

    /**
     * Register this WorkflowPropertyHandler with the provided registry.
     * @param registry
     */
    public void setRegistry(WorkflowPropertyHandlerRegistry registry)
    {
        QName key = getKey();
        if(key!=null)
        {
            registry.registerHandler(key, this);
        }
    }

    /**
     * @param nodeConverter the nodeConverter to set
     */
    public void setNodeConverter(WorkflowNodeConverter nodeConverter)
    {
        this.nodeConverter = nodeConverter;
    }
    
    /**
     * @param messageService the messageService to set
     */
    public void setMessageService(MessageService messageService)
    {
        this.messageService = messageService;
    }
    
    protected abstract QName getKey();

    protected void checkType(QName key, Object value, Class<?> type)
    {
        if (value != null && !(type.isAssignableFrom(value.getClass())))
        {
            throw getInvalidPropertyValueException(key, value);
        }
    }

    protected Object handleDefaultProperty(Object task, TypeDefinition type, QName key, Serializable value)
    {
        PropertyDefinition propDef = type.getProperties().get(key);
        if(propDef!=null)
        {
            return handleProperty(value, propDef);
        }
        else
        {
            AssociationDefinition assocDef = type.getAssociations().get(key);
            if(assocDef!=null)
            {
                return handleAssociation(value, assocDef);
            }
            else if (value instanceof NodeRef)
            {
                return nodeConverter.convertNode((NodeRef)value, false);
            }
        }
        return value;
    }
}
