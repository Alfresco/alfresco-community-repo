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

import com.fasterxml.jackson.annotation.JsonProperty;

import org.alfresco.rest.core.IRestModel;
import org.alfresco.rest.core.assertion.IModelAssertion;
import org.alfresco.utility.model.ProcessModel;

/**
 * Handles single Process Entry JSON response
 * "entry": {
 * "processDefinitionId": "activitiAdhoc:1:4",
 * "startUserId": "admin",
 * "startActivityId": "start",
 * "startedAt": "2016-05-24T09:43:17.000+0000",
 * "id": "55069",
 * "completed": false,
 * "processDefinitionKey": "activitiAdhoc"
 * }
 * Created by Claudia Agache on 10/11/2016.
 */
public class RestProcessModel extends ProcessModel implements IRestModel<RestProcessModel>,IModelAssertion<RestProcessModel>
{
    @JsonProperty(value = "entry")
    RestProcessModel model;
    
    @Override
    public RestProcessModel onModel()
    {
        return model;
    }
    
    private String processDefinitionId;
    private String startedAt;
    private String startUserId;
    private String startActivityId;
    private String completed;
    private String processDefinitionKey;
    private String durationInMs;
    private String endedAt;
    private String deleteReason;

    public String getProcessDefinitionId()
    {
        return processDefinitionId;
    }

    public void setProcessDefinitionId(String processDefinitionId)
    {
        this.processDefinitionId = processDefinitionId;
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

    public String getCompleted()
    {
        return completed;
    }

    public void setCompleted(String completed)
    {
        this.completed = completed;
    }

    public String getProcessDefinitionKey()
    {
        return processDefinitionKey;
    }

    public void setProcessDefinitionKey(String processDefinitionKey)
    {
        this.processDefinitionKey = processDefinitionKey;
    }
    
    public String getStartedAt()
    {
        return startedAt;
    }

    public void setStartedAt(String startedAt)
    {
        this.startedAt = startedAt;
    }

    public String getDurationInMs()
    {
        return durationInMs;
    }

    public void setDurationInMs(String durationInMs)
    {
        this.durationInMs = durationInMs;
    }

    public String getEndedAt()
    {
        return endedAt;
    }

    public void setEndedAt(String endedAt)
    {
        this.endedAt = endedAt;
    }

    public String getDeleteReason()
    {
        return deleteReason;
    }

    public void setDeleteReason(String deleteReason)
    {
        this.deleteReason = deleteReason;
    }
}    
