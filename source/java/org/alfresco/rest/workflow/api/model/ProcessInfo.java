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
    Long durationInMillis;
    String deleteReason;
    String businessKey;
    String superProcessInstanceId;
    
    Map<String, Object> variables;
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
        this.durationInMillis = processInstance.getDurationInMillis();
        this.deleteReason = processInstance.getDeleteReason();
        this.businessKey = processInstance.getBusinessKey();
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

    public Long getDurationInMillis()
    {
        return durationInMillis;
    }

    public void setDurationInMillis(Long durationInMillis)
    {
        this.durationInMillis = durationInMillis;
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

    public Map<String, Object> getVariables()
    {
        return variables;
    }

    public void setVariables(Map<String, Object> variables)
    {
        this.variables = variables;
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