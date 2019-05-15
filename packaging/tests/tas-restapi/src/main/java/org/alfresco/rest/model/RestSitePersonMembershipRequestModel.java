package org.alfresco.rest.model;

import org.alfresco.rest.core.IRestModel;
import org.alfresco.rest.core.assertion.ModelAssertion;
import org.alfresco.utility.model.TestModel;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RestSitePersonMembershipRequestModel extends TestModel implements IRestModel<RestSitePersonMembershipRequestModel>
{
    private String id;
    private String createdAt;
    private String modifiedAt;
    private String message;
    private RestSiteModel site;

    @JsonProperty(value = "person")
    private RestPersonModel person;

    @JsonProperty(value = "entry")
    RestSitePersonMembershipRequestModel model;

    public RestPersonModel getPersonModel()
    {
        return person;
    }

    public void setPersonModel(RestPersonModel person)
    {
        this.person = person;
    }

    @Override
    public RestSitePersonMembershipRequestModel onModel()
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

    public String getCreatedAt()
    {
        return createdAt;
    }

    public void setCreatedAt(String createdAt)
    {
        this.createdAt = createdAt;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public RestSiteModel getSite()
    {
        return site;
    }

    public void setSite(RestSiteModel site)
    {
        this.site = site;
    }

    public String getModifiedAt()
    {
        return modifiedAt;
    }

    public void setModifiedAt(String modifiedAt)
    {
        this.modifiedAt = modifiedAt;
    }

    /**
     * DSL for assertion on this rest model
     * @return
     */
    @Override
    public ModelAssertion<RestSitePersonMembershipRequestModel> assertThat() 
    {
      return new ModelAssertion<RestSitePersonMembershipRequestModel>(this);
    }
    
    @Override
    public ModelAssertion<RestSitePersonMembershipRequestModel> and() 
    {
      return assertThat();
    }
}
