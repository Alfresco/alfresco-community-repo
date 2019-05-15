package org.alfresco.rest.model;

import org.alfresco.rest.core.IRestModel;
import org.alfresco.rest.core.assertion.ModelAssertion;
import org.alfresco.utility.model.TestModel;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RestNodeChildAssociationModel extends TestModel implements IRestModel<RestNodeChildAssociationModel>
{
    @Override
    public ModelAssertion<RestNodeChildAssociationModel> assertThat()
    {
        return new ModelAssertion<RestNodeChildAssociationModel>(this);
    }

    @Override
    public ModelAssertion<RestNodeChildAssociationModel> and()
    {
        return assertThat();
    }

    @Override
    public RestNodeChildAssociationModel onModel()
    {
        return model;
    }

    @JsonProperty(value = "entry")
    RestNodeChildAssociationModel model;

    @JsonProperty(required = true)
    private String childId;

    @JsonProperty(required = true)
    private String assocType;

    public RestNodeChildAssociationModel(String childId, String assocType)
    {
        this.childId = childId;
        this.assocType = assocType;
    }

    public RestNodeChildAssociationModel()
    {

    }

    public String getChildId()
    {
        return childId;
    }

    public void setChildId(String childId)
    {
        this.childId = childId;
    }

    public String getAssocType()
    {
        return assocType;
    }

    public void setAssocType(String assocType)
    {
        this.assocType = assocType;
    }

}
