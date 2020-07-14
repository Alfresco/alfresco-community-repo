package org.alfresco.rest.model;

import static org.alfresco.utility.report.log.Step.STEP;

import org.alfresco.rest.core.IRestModel;
import org.alfresco.rest.core.assertion.ModelAssertion;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.model.TestModel;
import org.testng.Assert;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RestSiteMemberModel extends TestModel implements IRestModel<RestSiteMemberModel>
{
    @JsonProperty(value = "entry")
    RestSiteMemberModel model;

    @Override
    public RestSiteMemberModel onModel()
    {
        return model;
    }

    private UserRole role;
    private String id = "no-id";
    private boolean isMemberOfGroup;

    private RestPersonModel person;

    public UserRole getRole()
    {
        return role;
    }

    public void setRole(UserRole role)
    {
        this.role = role;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public RestPersonModel getPerson()
    {
        return person;
    }

    public boolean getIsMemberOfGroup()
    {
        return isMemberOfGroup;
    }

    public void setIsMemberOfGroup(boolean memberOfGroup)
    {
        isMemberOfGroup = memberOfGroup;
    }

    public void setPerson(RestPersonModel person)
    {
        this.person = person;
    }

    public RestSiteMemberModel assertSiteMemberHasRole(UserRole role) {
        STEP(String.format("REST API: Assert that site member role is '%s'", role));
        Assert.assertEquals(getRole(), role, "Site member role is not as expected.");
        
        return this;
    }

    /**
     * DSL for assertion on this rest model
     * @return
     */
    @Override
    public ModelAssertion<RestSiteMemberModel> assertThat() 
    {
      return new ModelAssertion<RestSiteMemberModel>(this);
    }
    
    @Override
    public ModelAssertion<RestSiteMemberModel> and() 
    {
      return assertThat();
    }
}