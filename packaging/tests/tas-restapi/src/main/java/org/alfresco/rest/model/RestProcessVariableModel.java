package org.alfresco.rest.model;

import org.alfresco.rest.core.IRestModel;
import org.alfresco.rest.core.assertion.ModelAssertion;
import org.alfresco.utility.data.RandomData;
import org.alfresco.utility.model.TestModel;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RestProcessVariableModel extends TestModel implements IRestModel<RestProcessVariableModel>
{  
    private String name;
    
    private String value;
    
    private String type;
    
    @JsonProperty(value = "entry")
    RestProcessVariableModel model;

    public RestProcessVariableModel()
    {
    }
    
    public RestProcessVariableModel(String name, String value, String type)
    {
        this.name = name;
        this.value = value;
        this.type = type;
    }

    @Override
    public RestProcessVariableModel onModel()
    {
        return model;
    }
    
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
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

    public static RestProcessVariableModel getRandomProcessVariableModel(String variableType){
        return new RestProcessVariableModel(RandomData.getRandomName("name"), RandomData.getRandomName("value"), variableType);    
    }
    
    
    @Override
    public ModelAssertion<RestProcessVariableModel> assertThat() 
    {      
      return new ModelAssertion<RestProcessVariableModel>(this);
    }
    
    @Override
    public ModelAssertion<RestProcessVariableModel> and() 
    {      
      return assertThat();
    }
    
}    