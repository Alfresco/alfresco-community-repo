package org.alfresco.rest.model;

import org.alfresco.rest.core.IRestModel;
import org.alfresco.rest.core.assertion.ModelAssertion;
import org.alfresco.utility.model.TestModel;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RestGroupMember extends TestModel implements IRestModel<RestGroupMember>
{
    @JsonProperty(required = true)
    private String id;
    @JsonProperty(required = true)
    private String displayName;
    @JsonProperty(required = true)
    private String memberType;

    @JsonProperty(value = "entry")
    RestGroupMember model;


    @Override
    public ModelAssertion<RestGroupMember> and()
    {
        return assertThat();
    }

    @Override
    public ModelAssertion<RestGroupMember> assertThat()
    {
        return new ModelAssertion<RestGroupMember>(this);
    }

    @Override
    public RestGroupMember onModel()
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

    public String getDisplayName()
    {
        return displayName;
    }

    public void setDisplayName(String displayName)
    {
        this.displayName = displayName;
    }

    public String getMemberType()
    {
        return memberType;
    }

    public void setMemberType(String memberType)
    {
        this.memberType = memberType;
    }
}
