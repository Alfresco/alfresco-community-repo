package org.alfresco.rest.model;

import org.alfresco.rest.core.IRestModel;
import org.alfresco.rest.core.assertion.ModelAssertion;
import org.alfresco.utility.model.TestModel;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RestNodeAssocTargetModel extends TestModel implements IRestModel<RestNodeAssocTargetModel>
{
    @JsonProperty(value = "aggregate")
    RestNodeAssocTargetModel model;

    @JsonProperty(required = true)
    private String targetId;
    @JsonProperty(required = true)
    private String assocType;

    public RestNodeAssocTargetModel()
    {

    }

    public RestNodeAssocTargetModel(String targetId, String assocType)
    {
        this.targetId = targetId;
        this.assocType = assocType;
    }

    @Override
    public RestNodeAssocTargetModel onModel()
    {
        return model;
    }

    public String getTargetId()
    {
        return targetId;
    }

    public void setTargetId(String targetId)
    {
        this.targetId = targetId;
    }

    public String getAssocType()
    {
        return assocType;
    }

    public void setAssocType(String assocType)
    {
        this.assocType = assocType;
    }

    @Override
    public ModelAssertion<RestNodeAssocTargetModel> and()
    {
        return assertThat();
    }

    @Override
    public ModelAssertion<RestNodeAssocTargetModel> assertThat()
    {
        return new ModelAssertion<RestNodeAssocTargetModel>(this);
    }

}
