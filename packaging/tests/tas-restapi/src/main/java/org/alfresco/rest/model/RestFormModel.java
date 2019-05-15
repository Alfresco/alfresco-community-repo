package org.alfresco.rest.model;

import org.alfresco.rest.core.IRestModel;
import org.alfresco.rest.core.assertion.ModelAssertion;
import org.alfresco.utility.model.TestModel;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Handles single representation of a Start Form Model
 * * "entry": {
 * "allowedValues": [
 * "1",
 * "2",
 * "3"
 * ],
 * "qualifiedName": "{http://www.alfresco.org/model/bpm/1.0}workflowPriority",
 * "defaultValue": "2",
 * "dataType": "d:int",
 * "name": "bpm_workflowPriority",
 * "title": "Workflow Priority",
 * "required": false
 * }
 * Created by Claudia Agache on 10/18/2016.
 */
public class RestFormModel extends TestModel implements IRestModel<RestFormModel>
{
    @JsonProperty(value = "entry") RestFormModel model;
    private String qualifiedName;
    private String defaultValue;
    private String dataType;
    private String name;
    private String title;
    private String required;
    private String[] allowedValues;

    public RestFormModel onModel()
    {
        return model;
    }

    public String[] getAllowedValues()
    {
        return allowedValues;
    }

    public void setAllowedValues(String[] allowedValues)
    {
        this.allowedValues = allowedValues;
    }

    public String getQualifiedName()
    {
        return qualifiedName;
    }

    public void setQualifiedName(String qualifiedName)
    {
        this.qualifiedName = qualifiedName;
    }

    public String getDefaultValue()
    {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue)
    {
        this.defaultValue = defaultValue;
    }

    public String getDataType()
    {
        return dataType;
    }

    public void setDataType(String dataType)
    {
        this.dataType = dataType;
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

    public String getRequired()
    {
        return required;
    }

    public void setRequired(String required)
    {
        this.required = required;
    }

    /**
     * DSL for assertion on this rest model
     * @return
     */
    @Override
    public ModelAssertion<RestFormModel> and()
    {
        return assertThat();
    }
    
    @Override
    public ModelAssertion<RestFormModel> assertThat()
    {
      return new ModelAssertion<RestFormModel>(this);
    }
}    