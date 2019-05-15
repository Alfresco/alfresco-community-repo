package org.alfresco.rest.model;

import org.alfresco.rest.core.IRestModel;
import org.alfresco.rest.core.assertion.ModelAssertion;
import org.alfresco.utility.model.TestModel;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RestCandidateModel extends TestModel implements IRestModel<RestCandidateModel>
{
    @JsonProperty(value = "entry")
    RestCandidateModel model;
    
    public enum CandidateType
    {
        USER, GROUP
    }
    
    private String candidateType;
    private String candidateId;

    @Override
    public RestCandidateModel onModel()
    {         
        return model;
    }
    
    public String getCandidateType()
    {
        return candidateType;
    }

    public void setCandidateType(String candidateType)
    {
        this.candidateType = candidateType;
    }
    
    public String getCandidateId()
    {
        return candidateId;
    }

    public void setCandidateId(String candidateId)
    {
        this.candidateId = candidateId;
    }

    @Override
    public ModelAssertion<RestCandidateModel> and() 
    {      
        return assertThat();
    }   
    
    @Override
    public ModelAssertion<RestCandidateModel> assertThat() 
    {      
      return new ModelAssertion<RestCandidateModel>(this);
    } 
}
