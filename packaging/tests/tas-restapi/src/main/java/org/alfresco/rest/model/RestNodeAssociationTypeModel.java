package org.alfresco.rest.model;

import org.alfresco.rest.core.IRestModel;
import org.alfresco.rest.core.assertion.ModelAssertion;
import org.alfresco.utility.model.TestModel;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents an association between two nodes
 */
public class RestNodeAssociationTypeModel extends TestModel implements IRestModel<RestNodeAssociationTypeModel>
{
    @Override
    public ModelAssertion<RestNodeAssociationTypeModel> assertThat()
    {
        return new ModelAssertion<RestNodeAssociationTypeModel>(this);
    }

    @Override
    public ModelAssertion<RestNodeAssociationTypeModel> and()
    {
        return assertThat();
    }

    @JsonProperty(value = "entry")
    RestNodeAssociationTypeModel model;

    @Override
    public RestNodeAssociationTypeModel onModel()
    {
        return model;
    }

    @JsonProperty
    private String assocType;

    @JsonProperty
    private boolean isPrimary;

    public String getAssocType()
    {
        return assocType;
    }

    public void setAssocType(String assocType)
    {
        this.assocType = assocType;
    }

    public boolean isPrimary()
    {
        return isPrimary;
    }

    public void setPrimary(boolean isPrimary)
    {
        this.isPrimary = isPrimary;
    }

}
