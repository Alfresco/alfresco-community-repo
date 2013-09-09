/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.rest.workflow.api.model;

import java.util.Date;
import java.util.List;

import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.task.DelegationState;

public class Task
{
    String id;
    String processId;
    String processDefinitionId;
    String activityDefinitionId;
    String name;
    String description;
    Date dueAt;
    Date startedAt;
    Date endedAt;
    Long durationInMs;
    int priority;
    String owner;
    String assignee;
    String formResourceKey;
    String state;
    List<TaskVariable> variables;
    
    public Task()
    {
    }

    public Task(HistoricTaskInstance taskInstance)
    {
        this.id = taskInstance.getId();
        this.processId = taskInstance.getProcessInstanceId();
        this.processDefinitionId = taskInstance.getProcessDefinitionId();
        this.activityDefinitionId = taskInstance.getTaskDefinitionKey();
        this.name = taskInstance.getName();
        this.description = taskInstance.getDescription();
        this.dueAt = taskInstance.getDueDate();
        this.startedAt = taskInstance.getStartTime();
        this.endedAt = taskInstance.getEndTime();
        this.durationInMs = taskInstance.getDurationInMillis();
        this.priority = taskInstance.getPriority();
        this.owner = taskInstance.getOwner();
        this.assignee = taskInstance.getAssignee();
        this.formResourceKey = taskInstance.getFormKey();
        if (taskInstance.getEndTime() != null)
        {
        	this.state = TaskStateTransition.COMPLETED.name().toLowerCase();
        }
        else if (taskInstance.getAssignee() != null)
        {
        	this.state = TaskStateTransition.CLAIMED.name().toLowerCase();
        }
        else
        {
        	this.state = TaskStateTransition.UNCLAIMED.name().toLowerCase();
        }
    }
    
    public Task(org.activiti.engine.task.Task taskInstance)
    {
        this.id = taskInstance.getId();
        this.processId = taskInstance.getProcessInstanceId();
        this.processDefinitionId = taskInstance.getProcessDefinitionId();
        this.activityDefinitionId = taskInstance.getTaskDefinitionKey();
        this.name = taskInstance.getName();
        this.description = taskInstance.getDescription();
        this.dueAt = taskInstance.getDueDate();
        this.startedAt = taskInstance.getCreateTime();
        this.priority = taskInstance.getPriority();
        this.owner = taskInstance.getOwner();
        this.assignee = taskInstance.getAssignee();
        if (taskInstance.getDelegationState() == DelegationState.PENDING)
        {
            this.state = TaskStateTransition.DELEGATED.name().toLowerCase();
        }
        else if (taskInstance.getDelegationState() == DelegationState.RESOLVED)
        {
            this.state = TaskStateTransition.RESOLVED.name().toLowerCase();
        }
        else if (taskInstance.getAssignee() != null)
        {
        	this.state = TaskStateTransition.CLAIMED.name().toLowerCase();
        }
        else
        {
        	this.state = TaskStateTransition.UNCLAIMED.name().toLowerCase();
        }
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getProcessId()
    {
        return processId;
    }

    public void setProcessId(String processId)
    {
        this.processId = processId;
    }

    public String getProcessDefinitionId()
    {
        return processDefinitionId;
    }

    public void setProcessDefinitionId(String processDefinitionId)
    {
        this.processDefinitionId = processDefinitionId;
    }

    public String getActivityDefinitionId()
    {
        return activityDefinitionId;
    }

    public void setActivityDefinitionId(String activityDefinitionId)
    {
        this.activityDefinitionId = activityDefinitionId;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public Date getDueAt()
    {
        return dueAt;
    }

    public void setDueAt(Date dueAt)
    {
        this.dueAt = dueAt;
    }

    public Date getStartedAt()
    {
        return startedAt;
    }

    public void setStartedAt(Date startedAt)
    {
        this.startedAt = startedAt;
    }

    public Date getEndedAt()
    {
        return endedAt;
    }

    public void setEndedAt(Date endedAt)
    {
        this.endedAt = endedAt;
    }

    public Long getDurationInMs()
    {
        return durationInMs;
    }

    public void setDurationInMs(Long durationInMs)
    {
        this.durationInMs = durationInMs;
    }

    public int getPriority()
    {
        return priority;
    }

    public void setPriority(int priority)
    {
        this.priority = priority;
    }

    public String getOwner()
    {
        return owner;
    }

    public void setOwner(String owner)
    {
        this.owner = owner;
    }

    public String getAssignee()
    {
        return assignee;
    }

    public void setAssignee(String assignee)
    {
        this.assignee = assignee;
    }

    public String getFormResourceKey()
    {
        return formResourceKey;
    }

    public void setFormResourceKey(String formResourceKey)
    {
        this.formResourceKey = formResourceKey;
    }
    
    public String getState()
    {
        return this.state;
    }
    
    public void setState(String state)
    {
        this.state = state;
    }

    public List<TaskVariable> getVariables()
    {
        return variables;
    }

    public void setVariables(List<TaskVariable> variables)
    {
        this.variables = variables;
    }
}