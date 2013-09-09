/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
import java.util.Map;
import java.util.Set;

import org.activiti.engine.history.HistoricProcessInstance;

public class ProcessInfo
{

    String id;
    String processDefinitionId;
    String processDefinitionKey;
    Date startedAt;
    Date endedAt;
    Long durationInMs;
    String deleteReason;
    String startUserId;
    String startActivityId;
    String endActivityId;
    String businessKey;
    String superProcessInstanceId;
    boolean completed;
    
    Map<String, Object> variables;
    List<Variable> processVariables;
    Set<String> items;

    public ProcessInfo()
    {
    }

    public ProcessInfo(HistoricProcessInstance processInstance)
    {
        this.id = processInstance.getId();
        this.processDefinitionId = processInstance.getProcessDefinitionId();
        this.startedAt = processInstance.getStartTime();
        this.endedAt = processInstance.getEndTime();
        this.durationInMs = processInstance.getDurationInMillis();
        this.deleteReason = processInstance.getDeleteReason();
        this.startUserId = processInstance.getStartUserId();
        this.startActivityId = processInstance.getStartActivityId();
        this.endActivityId = processInstance.getEndActivityId();
        this.businessKey = processInstance.getBusinessKey();
        this.superProcessInstanceId = processInstance.getSuperProcessInstanceId();
        this.completed = (processInstance.getEndTime() != null);
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getProcessDefinitionId()
    {
        return processDefinitionId;
    }

    public void setProcessDefinitionId(String processDefinitionId)
    {
        this.processDefinitionId = processDefinitionId;
    }

    public Long getDurationInMs()
    {
        return durationInMs;
    }

    public void setDurationInMs(Long durationInMs)
    {
        this.durationInMs = durationInMs;
    }

    public String getDeleteReason()
    {
        return deleteReason;
    }

    public void setDeleteReason(String deleteReason)
    {
        this.deleteReason = deleteReason;
    }

    public String getBusinessKey()
    {
        return businessKey;
    }

    public void setBusinessKey(String businessKey)
    {
        this.businessKey = businessKey;
    }

    public String getSuperProcessInstanceId()
    {
        return superProcessInstanceId;
    }

    public void setSuperProcessInstanceId(String superProcessInstanceId)
    {
        this.superProcessInstanceId = superProcessInstanceId;
    }

    public String getProcessDefinitionKey()
    {
        return processDefinitionKey;
    }

    public void setProcessDefinitionKey(String processDefinitionKey)
    {
        this.processDefinitionKey = processDefinitionKey;
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

    public String getStartUserId()
    {
        return startUserId;
    }

    public void setStartUserId(String startUserId)
    {
        this.startUserId = startUserId;
    }

    public String getStartActivityId()
    {
        return startActivityId;
    }

    public void setStartActivityId(String startActivityId)
    {
        this.startActivityId = startActivityId;
    }

    public String getEndActivityId()
    {
        return endActivityId;
    }

    public void setEndActivityId(String endActivityId)
    {
        this.endActivityId = endActivityId;
    }

    public boolean isCompleted()
    {
        return completed;
    }

    public void setCompleted(boolean completed)
    {
        this.completed = completed;
    }

    public Map<String, Object> getVariables()
    {
        return variables;
    }

    public void setVariables(Map<String, Object> variables)
    {
        this.variables = variables;
    }

    public List<Variable> getProcessVariables()
    {
        return processVariables;
    }

    public void setProcessVariables(List<Variable> processVariables)
    {
        this.processVariables = processVariables;
    }

    public Set<String> getItems()
    {
        return items;
    }

    public void setItems(Set<String> items)
    {
        this.items = items;
    }
}