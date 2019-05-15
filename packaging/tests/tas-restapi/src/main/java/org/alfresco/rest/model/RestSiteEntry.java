package org.alfresco.rest.model;

import org.alfresco.rest.core.IRestModel;
import org.alfresco.rest.core.assertion.ModelAssertion;
import org.alfresco.utility.model.TestModel;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 
 * Handles Site Entry in Site Membership Information response
 * 
 *      "entry": /{
          "site": {
            "id": "string",
            "guid": "string",
            "title": "string",
            "description": "string",
            "visibility": "PRIVATE",
            "role": "SiteConsumer"
          },
          "id": "string",
          "guid": "string",
          "role": "SiteConsumer"
        }
 *
 */
public class RestSiteEntry extends TestModel implements IRestModel<RestSiteEntry>
{      
    private String role;
    private String guid;
    private String id;
    
    @JsonProperty(value = "site", required = true)
    RestSiteModel site;
    
    @JsonProperty(value= "entry")
    RestSiteEntry model;

    public RestSiteModel onSite()
    {
        return site;
    }
    
    public String getRole()
    {
        return role;
    }

    public void setRole(String role)
    {
        this.role = role;
    }

    public String getGuid() {
      return guid;
    }

    public void setGuid(String guid) {
      this.guid = guid;
    }

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    /**
     * DSL for assertion on this rest model
     * @return
     */
    @Override
    public ModelAssertion<RestSiteEntry> assertThat() 
    {
      return new ModelAssertion<RestSiteEntry>(this);
    }
    
    @Override
    public ModelAssertion<RestSiteEntry> and() 
    {
      return assertThat();
    }

    @Override
    public RestSiteEntry onModel() 
    {
      return model;
    }
    
}    