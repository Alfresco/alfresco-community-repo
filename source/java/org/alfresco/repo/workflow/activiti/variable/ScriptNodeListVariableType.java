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

package org.alfresco.repo.workflow.activiti.variable;

import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.variable.SerializableType;
import org.activiti.engine.impl.variable.ValueFields;
import org.activiti.engine.impl.variable.VariableType;
import org.alfresco.repo.workflow.activiti.ActivitiScriptNode;
import org.alfresco.repo.workflow.activiti.ActivitiScriptNodeList;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Custom {@link VariableType} that allows storing a list of {@link ActivitiScriptNode}s as
 * a process variable in activiti.
 *
 * @author Frederik Heremans
 * @since 3.4.e
 */
public class ScriptNodeListVariableType extends SerializableType
{
    public static final String TYPE = "alfrescoScriptNodeList";
    
    private ServiceRegistry serviceRegistry;

    @Override
    public String getTypeName()
    {
        return TYPE;
    }

    @Override
    public boolean isCachable()
    {
        // The ActivitiScriptNodeList can be cached since it uses the serviceRegistry internally
        // for resolving actual values.
        return true;
    }

    @Override
    public boolean isAbleToStore(Object value)
    {
        if(value == null) 
        {
            return true;
        }
        return ActivitiScriptNodeList.class.isAssignableFrom(value.getClass());
    }
 
    @Override
    public void setValue(Object value, ValueFields valueFields)
    {
        if(value != null) 
        {
            if(!(value instanceof ActivitiScriptNodeList)) 
            {
                throw new ActivitiException("Passed value is not an instance of ActivitiScriptNodeList, cannot set variable value.");
            }
           
            // Extract all node references
            List<NodeRef> nodeRefs = ((ActivitiScriptNodeList) value).getNodeReferences();
            // Save the list as a serializable
            super.setValue(nodeRefs, valueFields);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object getValue(ValueFields valueFields)
    {
        Object serializable = super.getValue(valueFields);
        if(serializable == null)
        {
            return null;
        }
        
        if(!(serializable instanceof List<?>))
        {
            throw new ActivitiException("Serializable stored in variable is not instance of List<NodeRef>, cannot get value.");
        }
        
        ActivitiScriptNodeList scriptNodes = new ActivitiScriptNodeList();
        // Wrap all node references in an ActivitiScriptNode
        List<NodeRef> nodeRefs =(List<NodeRef>) serializable;
        for(NodeRef ref : nodeRefs) 
        {
            scriptNodes.add(new ActivitiScriptNode(ref, serviceRegistry));
        }
        return scriptNodes;
    }
    
    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
    }
}
