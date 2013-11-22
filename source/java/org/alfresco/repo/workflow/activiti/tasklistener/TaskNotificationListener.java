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

package org.alfresco.repo.workflow.activiti.tasklistener;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.form.FormData;
import org.activiti.engine.impl.form.TaskFormHandler;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.IdentityLinkEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.repository.ProcessDefinition;
import org.alfresco.repo.workflow.WorkflowNotificationUtils;
import org.alfresco.repo.workflow.activiti.ActivitiConstants;
import org.alfresco.repo.workflow.activiti.ActivitiScriptNode;
import org.alfresco.repo.workflow.activiti.properties.ActivitiPropertyConverter;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Tasklistener that is notified when a task is created, will send email-notification
 * if this is required for this workflow.
 * 
 * @author Frederik Heremans
 * @since 4.2
 */
public class TaskNotificationListener implements TaskListener
{
    private static final long serialVersionUID = 1L;
    
    private WorkflowNotificationUtils workflowNotificationUtils;
    private ActivitiPropertyConverter propertyConverter;
    
    /**
     * @param services  the service registry
     */
    public void setWorkflowNotification(WorkflowNotificationUtils service)
    {
        this.workflowNotificationUtils = service;
    }
    
    /**
     * @param propertyConverter the property converter
     */
    public void setPropertyConverter(ActivitiPropertyConverter propertyConverter)
    {
        this.propertyConverter = propertyConverter;
    }
    
    @Override
    public void notify(DelegateTask task)
    {
        // Determine whether we need to send the workflow notification or not
        ExecutionEntity executionEntity = ((ExecutionEntity)task.getExecution()).getProcessInstance();
        Boolean value = (Boolean)executionEntity.getVariable(WorkflowNotificationUtils.PROP_SEND_EMAIL_NOTIFICATIONS);
        if (Boolean.TRUE.equals(value) == true)
        {    
            NodeRef workflowPackage = null;
            ActivitiScriptNode scriptNode = (ActivitiScriptNode)executionEntity.getVariable(WorkflowNotificationUtils.PROP_PACKAGE);
            if (scriptNode != null)
            {
                workflowPackage = scriptNode.getNodeRef();
            }
            
            // Determine whether the task is pooled or not
            String[] authorities = null;
            boolean isPooled = false;
            if (task.getAssignee() == null)
            {
                // Task is pooled
                isPooled = true;
                
                // Get the pool of user/groups for this task
                List<IdentityLinkEntity> identities = ((TaskEntity)task).getIdentityLinks();
                List<String> temp = new ArrayList<String>(identities.size());
                for (IdentityLinkEntity item : identities)
                {
                    String group = item.getGroupId();
                    if (group != null)
                    {
                        temp.add(group);
                    }
                    String user = item.getUserId();
                    if (user != null)
                    {
                        temp.add(user);
                    }
                }
                authorities = temp.toArray(new String[temp.size()]);
            }
            else
            {
                // Get the assigned user or group
                authorities = new String[]{task.getAssignee()};
            }
            
            String title = null;
            String taskFormKey = getFormKey(task);
            
            // Fetch definition and extract name again. Possible that the default is used if the provided is missing
            TypeDefinition typeDefinition = propertyConverter.getWorkflowObjectFactory().getTaskTypeDefinition(taskFormKey, false);
            taskFormKey = typeDefinition.getName().toPrefixString();
            
            if (taskFormKey != null) 
            {
                String processDefinitionKey = ((ProcessDefinition) ((TaskEntity)task).getExecution().getProcessDefinition()).getKey();
                String defName = propertyConverter.getWorkflowObjectFactory().buildGlobalId(processDefinitionKey);
                title = propertyConverter.getWorkflowObjectFactory().getTaskTitle(typeDefinition, defName, task.getName(), taskFormKey.replace(":", "_"));
            }
            
            if (title == null)
            {
                if (task.getName() != null)
                {
                    title = task.getName();
                }
                else
                {
                    title = taskFormKey.replace(":", "_");
                }
            }
            
            // Make sure a description is present
            String description = task.getDescription();
            if (description == null || description.length() == 0)
            {
            	// use the task title as the description
            	description = title;
            }

            // Send email notification
            workflowNotificationUtils.sendWorkflowAssignedNotificationEMail(
                    ActivitiConstants.ENGINE_ID + "$" + task.getId(),
                    title,
                    description,
                    task.getDueDate(),
                    Integer.valueOf(task.getPriority()),
                    workflowPackage,
                    authorities,
                    isPooled);
        }
    }

    private String getFormKey(DelegateTask task)
    {
        FormData formData = null;
        TaskEntity taskEntity = (TaskEntity) task;
        TaskFormHandler taskFormHandler = taskEntity.getTaskDefinition().getTaskFormHandler();
        if (taskFormHandler != null)
        {
            formData = taskFormHandler.createTaskForm(taskEntity);
            if (formData != null) { return formData.getFormKey(); }
        }
        return null;
    }
}