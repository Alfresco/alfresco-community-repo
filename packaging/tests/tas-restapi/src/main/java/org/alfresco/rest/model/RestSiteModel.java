package org.alfresco.rest.model;

import org.alfresco.rest.core.IRestModel;
import org.alfresco.rest.core.assertion.ModelAssertion;
import org.alfresco.utility.model.SiteModel;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Handles single Site JSON responses
 * Example:
 * "visibility": "PUBLIC",
 * "guid": "79e140e1-5039-4efa-acaf-c22b5ba7c947",
 * "description": "Description1470255221170",
 * "id": "0-C2291-1470255221170",
 * "title": "0-C2291-1470255221170"
 */
public class RestSiteModel extends SiteModel implements IRestModel<RestSiteModel>
{
    private String role;

    public String getPreset()
    {
        return preset;
    }

    public void setPreset(String preset)
    {
        this.preset = preset;
    }

    private String preset;

    @JsonProperty(value = "entry")
    RestSiteModel model;

    @Override
    public RestSiteModel onModel()
    {
        return model;
    }

    public String getRole()
    {
        return role;
    }

    public void setRole(String role)
    {
        this.role = role;        
    }
    
    /**
     * DSL for assertion on this rest model
     * @return
     */
    @Override
    public ModelAssertion<RestSiteModel> assertThat() 
    {
      return new ModelAssertion<RestSiteModel>(this);
    }
    
    @Override
    public ModelAssertion<RestSiteModel> and() 
    {
      return assertThat();
    }
}