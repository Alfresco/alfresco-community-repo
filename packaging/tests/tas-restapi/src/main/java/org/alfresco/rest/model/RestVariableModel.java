package org.alfresco.rest.model;

import org.alfresco.rest.core.IRestModel;
import org.alfresco.rest.core.assertion.ModelAssertion;
import org.alfresco.utility.data.RandomData;
import org.alfresco.utility.model.TestModel;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Handles a single Variable JSON response
 * Example:
 * {
 *      "scope": "string",
 *      "name": "string",
 *      "value": 0,
 *      "type": "string"
 * }
 * 
 * @author Cristina Axinte
 */
public class RestVariableModel extends TestModel implements IRestModel<RestVariableModel>
{
    private String scope;
    private String name;
    private Object value;
    private String type;

    @JsonProperty(value = "entry")
    RestVariableModel model;

    public RestVariableModel()
    {
    }

    public RestVariableModel(String scope, String name, String type, Object value)
    {
        this.scope = scope;
        this.name = name;
        this.type = type;
        this.value = value;
    }

    public String getScope()
    {
        return scope;
    }

    public void setScope(String scope)
    {
        this.scope = scope;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Object getValue()
    {
        return value;
    }

    public void setValue(Object value)
    {
        this.value = value;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public static RestVariableModel getRandomTaskVariableModel(String scope, String type)
    {
        return new RestVariableModel(scope, RandomData.getRandomName("name"), type, RandomData.getRandomName("value"));
    }

    /**
     * DSL for assertion on this rest model
     * 
     * @return
     */
    @Override
    public ModelAssertion<RestVariableModel> assertThat()
    {
        return new ModelAssertion<RestVariableModel>(this);
    }

    @Override
    public ModelAssertion<RestVariableModel> and()
    {
        return assertThat();
    }

    @Override
    public RestVariableModel onModel()
    {
        return model;
    }

}