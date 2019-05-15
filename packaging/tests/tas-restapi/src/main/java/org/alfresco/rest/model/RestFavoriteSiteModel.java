package org.alfresco.rest.model;

import org.alfresco.rest.core.IRestModel;
import org.alfresco.rest.core.assertion.ModelAssertion;
import org.alfresco.utility.model.TestModel;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RestFavoriteSiteModel extends TestModel implements IRestModel<RestFavoriteSiteModel>
{
    @JsonProperty(value = "entry")
    RestFavoriteSiteModel model;

    @JsonProperty(required = true)
    String id;

    @Override
    public RestFavoriteSiteModel onModel()
    {
        return model;
    }
    public String getId()
    {
        return id;
    }
    public void setId(String id)
    {
        this.id = id;
    }
    
    @Override
    public ModelAssertion<RestFavoriteSiteModel> and() 
    {     
        return assertThat();
    }    
    
    @Override
    public ModelAssertion<RestFavoriteSiteModel> assertThat() 
    {     
      return new ModelAssertion<RestFavoriteSiteModel>(this);
    }    
}    