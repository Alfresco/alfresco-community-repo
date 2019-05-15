package org.alfresco.rest.model;

import org.alfresco.rest.core.IRestModel;
import org.alfresco.rest.core.assertion.ModelAssertion;
import org.alfresco.utility.model.TestModel;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RestPersonFavoritesModel extends TestModel implements IRestModel<RestPersonFavoritesModel>
{
    @JsonProperty(value = "entry")
    RestPersonFavoritesModel model;
    
    @Override
    public RestPersonFavoritesModel onModel() 
    {     
      return model;
    }

    private String targetGuid;
    private String createdAt;
    
    private RestTargetModel target;

    public RestPersonFavoritesModel()
    {
    }

    public RestPersonFavoritesModel(String targetGuid, String createdAt)
    {
        super();
        this.targetGuid = targetGuid;
        this.createdAt = createdAt;
    }

    public String getTargetGuid()
    {
        return targetGuid;
    }

    public void setTargetGuid(String targetGuid)
    {
        this.targetGuid = targetGuid;
    }
    
    public RestTargetModel getTarget()
    {
        return target;
    }

    public void setTarget(RestTargetModel target)
    {
        this.target = target;
    }

    public String getCreatedAt()
    {
        return createdAt;
    }

    public void setCreatedAt(String createdAt)
    {
        this.createdAt = createdAt;
    }

    @Override
    public ModelAssertion<RestPersonFavoritesModel> assertThat() 
    {      
      return new ModelAssertion<>(this);
    }
    
    @Override
    public ModelAssertion<RestPersonFavoritesModel> and() 
    {      
      return assertThat();
    }

}    