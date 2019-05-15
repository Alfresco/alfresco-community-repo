package org.alfresco.rest.model;

import org.alfresco.rest.core.IRestModel;
import org.alfresco.rest.core.assertion.ModelAssertion;
import org.alfresco.utility.model.TestModel;
import org.testng.Assert;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RestSiteMembershipRequestModel extends TestModel implements IRestModel<RestSiteMembershipRequestModel>
{
    @JsonProperty(value = "entry")
    RestSiteMembershipRequestModel model;

    @Override
    public RestSiteMembershipRequestModel onModel()
    {
        return model;
    }

    private String id;
    private String createdAt;
    private String modifiedAt;
    private String message;
    private RestSiteModel site;

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

    public RestSiteMembershipRequestModel assertMembershipRequestMessageIs(String message)
    {
        Assert.assertEquals(getMessage(), message, "Site membership request message is not correct");
        return this;
    }

    /**
     * DSL for assertion on this rest model
     * @return
     */
    @Override
    public ModelAssertion<RestSiteMembershipRequestModel> assertThat() 
    {
      return new ModelAssertion<RestSiteMembershipRequestModel>(this);
    }
    
    @Override
    public ModelAssertion<RestSiteMembershipRequestModel> and() 
    {
      return assertThat();
    }
    
}    