package org.alfresco.rest.model;

import org.alfresco.rest.core.IRestModel;
import org.alfresco.rest.core.assertion.IModelAssertion;
import org.alfresco.rest.core.assertion.ModelAssertion;
import org.alfresco.utility.model.ProcessModel;

import com.fasterxml.jackson.annotation.JsonProperty;

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

    /**
     * DSL for assertion on this rest model
     * @return
     */
    @Override
    public ModelAssertion<RestProcessModel> assertThat() 
    {
      return new ModelAssertion<RestProcessModel>(this);
    }

    @Override
    public ModelAssertion<RestProcessModel> and() 
    {
      return assertThat();
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