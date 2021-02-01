package org.alfresco.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.alfresco.rest.core.IRestModel;
import org.alfresco.rest.core.assertion.ModelAssertion;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.model.TestModel;
import org.testng.Assert;

import static org.alfresco.utility.report.log.Step.STEP;

public class RestSiteGroupModel extends TestModel implements IRestModel<RestSiteGroupModel>
{
    @JsonProperty(value = "entry")
    RestSiteGroupModel model;

    @Override
    public RestSiteGroupModel onModel()
    {
        return model;
    }

    private UserRole role;
    private String id = "no-id";
    private RestGroupsModel group;

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

    public RestGroupsModel getGroup()
    {
        return group;
    }

    public void setGroup(RestGroupsModel group)
    {
        this.group = group;
    }

    public RestSiteGroupModel assertSiteGroupHasRole(UserRole role) {
        STEP(String.format("REST API: Assert that site group role is '%s'", role));
        Assert.assertEquals(getRole(), role, "Site group role is not as expected.");
        return this;
    }

    /**
     * DSL for assertion on this rest model
     * @return
     */
    @Override
    public ModelAssertion<RestSiteGroupModel> assertThat()
    {
      return new ModelAssertion<RestSiteGroupModel>(this);
    }
    
    @Override
    public ModelAssertion<RestSiteGroupModel> and()
    {
      return assertThat();
    }
}