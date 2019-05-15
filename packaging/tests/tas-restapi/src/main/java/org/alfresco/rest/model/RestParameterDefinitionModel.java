package org.alfresco.rest.model;

import org.alfresco.rest.core.IRestModel;
import org.alfresco.rest.core.assertion.ModelAssertion;
import org.alfresco.utility.model.TestModel;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RestParameterDefinitionModel extends TestModel implements IRestModel<RestParameterDefinitionModel>
{
    private String name;
    private String type;
    private boolean multiValued;
    private boolean mandatory;
    private String displayLabel;
    private String parameterConstraintName;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public boolean isMultiValued()
    {
        return multiValued;
    }

    public void setMultiValued(boolean multiValued)
    {
        this.multiValued = multiValued;
    }

    public boolean isMandatory()
    {
        return mandatory;
    }

    public void setMandatory(boolean mandatory)
    {
        this.mandatory = mandatory;
    }

    public String getDisplayLabel()
    {
        return displayLabel;
    }

    public void setDisplayLabel(String displayLabel)
    {
        this.displayLabel = displayLabel;
    }

    public String getParameterConstraintName()
    {
        return parameterConstraintName;
    }

    public void setParameterConstraintName(String parameterConstraintName)
    {
        this.parameterConstraintName = parameterConstraintName;
    }

    public RestParameterDefinitionModel getParameterDefinitionModel()
    {
        return parameterDefinitionModel;
    }

    public void setParameterDefinitionModel(RestParameterDefinitionModel parameterDefinitionModel)
    {
        this.parameterDefinitionModel = parameterDefinitionModel;
    }

    @JsonProperty(value = "entry")
    RestParameterDefinitionModel parameterDefinitionModel;

    @Override
    public ModelAssertion<RestParameterDefinitionModel> and()
    {
        return assertThat();
    }

    @Override
    public ModelAssertion<RestParameterDefinitionModel> assertThat()
    {
        return new ModelAssertion<RestParameterDefinitionModel>(this);
    }

    @Override
    public RestParameterDefinitionModel onModel()
    {
        return parameterDefinitionModel;
    }

}
