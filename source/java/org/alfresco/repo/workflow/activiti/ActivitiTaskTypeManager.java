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

import org.activiti.engine.FormService;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.form.FormData;
import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.impl.form.TaskFormHandler;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.task.Task;
import org.alfresco.repo.workflow.WorkflowObjectFactory;
import org.alfresco.service.cmr.dictionary.TypeDefinition;

/**
 * @author Nick Smith
 * @since 3.4.e
 */
public class ActivitiTaskTypeManager
{
    private final WorkflowObjectFactory factory;
    private final FormService formService;
    
    public ActivitiTaskTypeManager(WorkflowObjectFactory factory, FormService formService)
    {
        this.factory = factory;
        this.formService = formService;
    }

    public TypeDefinition getStartTaskDefinition(String taskTypeName) 
    {
        return factory.getTaskFullTypeDefinition(taskTypeName, true);
    }
    
    public TypeDefinition getFullTaskDefinition(Task task)
    {
        TaskFormData taskFormData = formService.getTaskFormData(task.getId());
        return getFullTaskDefinition(task.getId(), taskFormData);
    }
    
    public TypeDefinition getFullTaskDefinition(DelegateTask delegateTask)
    {
        FormData formData = null;
        TaskEntity taskEntity = (TaskEntity) delegateTask;
        TaskFormHandler taskFormHandler = taskEntity.getTaskDefinition().getTaskFormHandler();
        if (taskFormHandler != null)
        {
            formData = taskFormHandler.createTaskForm(taskEntity);
        }
        return getFullTaskDefinition(delegateTask.getId(), formData);
    }
    
    public TypeDefinition getFullTaskDefinition(String typeName)
    {
        return getFullTaskDefinition(typeName, null);
    }
    
    private TypeDefinition getFullTaskDefinition(String taskDefinitionKey, FormData taskFormData)
    {
        String formKey = null;
        if (taskFormData != null)
        {
            formKey = taskFormData.getFormKey();
        }
        else
        {
            // Revert to task definition key
            formKey = taskDefinitionKey;
        }
        // Since Task instances are never the start-task, it's safe to always be false
        return factory.getTaskFullTypeDefinition(formKey, false);
    }
}
