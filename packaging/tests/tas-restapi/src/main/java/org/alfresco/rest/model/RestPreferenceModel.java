package org.alfresco.rest.model;

import org.alfresco.rest.core.IRestModel;
import org.alfresco.rest.core.assertion.ModelAssertion;
import org.alfresco.utility.model.TestModel;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Handles Pagination JSON
 *
 * Example:
 *    {
        "entry": {
          "id": "org.alfresco.share.sites.favourites.site-lwdxYDQFIi",
          "value": true
      }
 *
 * @author Cristina Axinte
 */
public class RestPreferenceModel extends TestModel implements IRestModel<RestPreferenceModel>
{
    @JsonProperty(value = "entry")
    RestPreferenceModel model;

    @Override
    public RestPreferenceModel onModel()
    {
        return model;
    }
    
    @JsonProperty(required = true)
    String id;
    String value;

    public String getValue()
    {
        return value;
    }
    public void setValue(String value)
    {
        this.value = value;
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
    public ModelAssertion<RestPreferenceModel> assertThat() 
    {      
      return new ModelAssertion<RestPreferenceModel>(this);
    }
    
    @Override
    public ModelAssertion<RestPreferenceModel> and() 
    {      
      return assertThat();
    }
}    