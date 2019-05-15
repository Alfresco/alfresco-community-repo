package org.alfresco.rest.model;

import java.util.List;
import org.alfresco.rest.core.IRestModel;
import org.alfresco.rest.core.assertion.ModelAssertion;
import org.alfresco.utility.model.TestModel;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RestActionDefinitionModel extends TestModel implements IRestModel<RestActionDefinitionModel>
{
    
    @JsonProperty(value = "entry")
    RestActionDefinitionModel actionDefinitionModel;
    private String id;
    private String name;
    private String title;
    private String description;
    private List<String> applicableTypes;
    private boolean adhocPropertiesAllowed;
    private boolean trackStatus;
    private List<RestParameterDefinitionModel> parameterDefinitions;

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public List<String> getApplicableTypes()
    {
        return applicableTypes;
    }

    public void setApplicableTypes(List<String> applicableTypes)
    {
        this.applicableTypes = applicableTypes;
    }

    public boolean isAdhocPropertiesAllowed()
    {
        return adhocPropertiesAllowed;
    }

    public void setAdhocPropertiesAllowed(boolean adhocPropertiesAllowed)
    {
        this.adhocPropertiesAllowed = adhocPropertiesAllowed;
    }

    public boolean isTrackStatus()
    {
        return trackStatus;
    }

    public void setTrackStatus(boolean trackStatus)
    {
        this.trackStatus = trackStatus;
    }
    
    public List<RestParameterDefinitionModel> getParameterDefinitions()
    {
        return parameterDefinitions;
    }

    public void setParameterDefinitions(List<RestParameterDefinitionModel> parameterDefinitions)
    {
        this.parameterDefinitions = parameterDefinitions;
    }

    @Override
    public ModelAssertion<RestActionDefinitionModel> and()
    {
        return assertThat();
    }

    @Override
    public ModelAssertion<RestActionDefinitionModel> assertThat()
    {
        return new ModelAssertion<RestActionDefinitionModel>(this);
    }

    @Override
    public RestActionDefinitionModel onModel()
    {
        return actionDefinitionModel;
    }

}
