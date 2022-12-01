/*-
 * #%L
 * alfresco-tas-restapi
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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
package org.alfresco.rest.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.alfresco.rest.core.IRestModel;
import org.alfresco.utility.model.TaskModel;

/**
 * Handles single Task JSON response
 * Example:
 * "entry": {
          "id": "string",
          "processId": "string",
          "processDefinitionId": "string",
          "activityDefinitionId": "string",
          "name": "string",
          "description": "string",
          "dueAt": "2016-10-11T09:53:02.549Z",
          "startedAt": "2016-10-11T09:53:02.549Z",
          "endedAt": "2016-10-11T09:53:02.549Z",
          "durationInMs": 0,
          "priority": 0,
          "owner": "string",
          "assignee": "string",
          "formResourceKey": "string",
          "state": "unclaimed",
          "variables": [
            {
              "scope": "string",
              "name": "string",
              "value": 0,
              "type": "string"
            }
          ]
        }
 * 
 * @author Cristina Axinte
 *
 */
public class RestTaskModel extends TaskModel implements IRestModel<RestTaskModel>
{
    @JsonProperty(value = "entry")
    RestTaskModel model;

    @Override
    public RestTaskModel onModel()
    {
        return model;
    }

    private String processDefinitionId;
    private String activityDefinitionId;
    private String name;
    private String startedAt;
    private String endedAt;
    private Integer durationInMs;
    private String owner;
    private String formResourceKey;
    private String state;
    private String description;
    @JsonProperty(value = "priority")
    private Integer priorityTask;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private RestVariableModelsCollection variables;

    public RestVariableModelsCollection getVariables()
    {
        return variables;
    }

    public void setVariables(RestVariableModelsCollection variables)
    {
        this.variables = variables;
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

    public String getStartedAt()
    {
        return startedAt;
    }

    public void setStartedAt(String startedAt)
    {
        this.startedAt = startedAt;
    }

    public String getEndedAt()
    {
        return endedAt;
    }

    public void setEndedAt(String endedAt)
    {
        this.endedAt = endedAt;
    }

    public Integer getDurationInMs()
    {
        return durationInMs;
    }

    public void setDurationInMs(Integer durationInMs)
    {
        this.durationInMs = durationInMs;
    }

    public String getOwner()
    {
        return owner;
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
        return state;
    }

    public void setState(String state)
    {
        this.state = state;
    }

    public Integer getPriorityTask()
    {
        return priorityTask;
    }

    public void setPriorityTask(Integer priorityTask)
    {
        this.priorityTask = priorityTask;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }
    
}    
