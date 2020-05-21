package org.alfresco.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.alfresco.rest.core.IRestModel;
import org.alfresco.rest.core.assertion.ModelAssertion;
import org.alfresco.utility.model.CustomAspectPropertiesModel;
import org.alfresco.utility.model.TestModel;

import java.util.List;

/**
 * @author Bogdan Bocancea
 */
public class RestCustomTypeModel extends TestModel implements IRestModel<RestCustomTypeModel>
{
    @JsonProperty(value = "entry")
    RestCustomTypeModel model;

    @JsonProperty(required = true)
    private String name;

    @JsonProperty(required = true)
    private String parentName;

    @JsonProperty
    private String title;

    @JsonProperty
    private String description;

    @JsonProperty
    private List<CustomAspectPropertiesModel> properties;

    public RestCustomTypeModel()
    {

    }

    public RestCustomTypeModel(String name, String parentName)
    {
        this.name = name;
        this.parentName = parentName;
    }

    public RestCustomTypeModel(String name, String parentName, String title)
    {
        this.name = name;
        this.parentName = parentName;
        this.title = title;
    }

    @Override
    public RestCustomTypeModel onModel()
    {
        return model;
    }

    @Override
    public ModelAssertion<RestCustomTypeModel> and()
    {
        return assertThat();
    }

    @Override
    public ModelAssertion<RestCustomTypeModel> assertThat()
    {
        return new ModelAssertion<RestCustomTypeModel>(this);
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getParentName()
    {
        return parentName;
    }

    public void setParentName(String parentName)
    {
        this.parentName = parentName;
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

    public List<CustomAspectPropertiesModel> getProperties()
    {
        return properties;
    }

    public void setProperties(List<CustomAspectPropertiesModel> properties)
    {
        this.properties = properties;
    }
}
