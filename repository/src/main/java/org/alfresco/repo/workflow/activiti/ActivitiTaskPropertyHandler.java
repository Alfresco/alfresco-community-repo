/*
 * #%L
 * Alfresco Repository
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

package org.alfresco.repo.workflow.activiti;

import java.io.Serializable;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.task.Task;

import org.alfresco.repo.workflow.AbstractWorkflowPropertyHandler;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.namespace.QName;

/**
 * @author Nick Smith
 * @since 3.4.e
 */
public abstract class ActivitiTaskPropertyHandler extends AbstractWorkflowPropertyHandler
{
    /**
     * {@inheritDoc}
     */
    public Object handleProperty(QName key, Serializable value, TypeDefinition type, Object object, Class<?> objectType)
    {
        if (DelegateTask.class.equals(objectType))
        {
            return handleDelegateTaskProperty((DelegateTask) object, type, key, value);
        }
        else if (Task.class.equals(objectType))
        {
            return handleTaskProperty((Task) object, type, key, value);
        }
        return handleProcessPropert(null, type, key, value);
    }

    /**
     * @param process
     *            Object
     * @param type
     *            TypeDefinition
     * @param key
     *            QName
     * @param value
     *            Serializable
     * @return Object
     */
    private Object handleProcessPropert(Object process, TypeDefinition type, QName key, Serializable value)
    {
        return handleDefaultProperty(process, type, key, value);
    }

    /**
     * Handles the property for a {@link Task}.
     * 
     * @param task
     *            Task
     * @param type
     *            TypeDefinition
     * @param key
     *            QName
     * @param value
     *            Serializable
     * @return Object
     */
    protected abstract Object handleTaskProperty(Task task, TypeDefinition type, QName key, Serializable value);

    /**
     * Handles the property for a {@link DelegateTask}.
     * 
     * @param task
     *            DelegateTask
     * @param value
     *            TypeDefinition
     * @param key
     *            QName
     * @param type
     *            Serializable
     * @return Object
     */
    protected abstract Object handleDelegateTaskProperty(DelegateTask task, TypeDefinition type, QName key, Serializable value);
}
