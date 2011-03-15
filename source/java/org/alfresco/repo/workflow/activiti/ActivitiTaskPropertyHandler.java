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

package org.alfresco.repo.workflow.activiti;

import java.io.Serializable;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.task.Task;
import org.alfresco.repo.workflow.AbstractWorkflowPropertyHandler;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.namespace.QName;

/**
 * @since 3.4.e
 * @author Nick Smith
 *
 */
public abstract class ActivitiTaskPropertyHandler extends AbstractWorkflowPropertyHandler
{

    /**
    * {@inheritDoc}
    */
    public Object handleProperty(QName key, Serializable value, TypeDefinition type, Object object, Class<?> objectType)
    {
        if(DelegateTask.class.equals(objectType))
        {
            return handleDelegateTaskProperty((DelegateTask)object, type, key, value);
        }
        else if(Task.class.equals(objectType))
        {
            return handleTaskProperty((Task)object, type, key, value);
        }
        return handleProcessPropert(null, type, key, value);
    }

    /**
     * @param type
     * @param key
     * @param value
     * @return
     */
    private Object handleProcessPropert(Object process, TypeDefinition type, QName key, Serializable value)
    {
        return handleDefaultProperty(process, type, key, value);
    }

    /**
     * Handles the property for a {@link Task}.
     * @param task
     * @param type
     * @param key
     * @param value
     * @return
     */
    protected abstract Object handleTaskProperty(Task task, TypeDefinition type, QName key, Serializable value);

    /**
     * Handles the property for a {@link DelegateTask}.
     * @param task
     * @param value 
     * @param key 
     * @param type 
     * @return
     */
    protected abstract Object handleDelegateTaskProperty(DelegateTask task, TypeDefinition type, QName key, Serializable value);

}
