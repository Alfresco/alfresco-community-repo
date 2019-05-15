package org.alfresco.rest.model;

import org.alfresco.rest.core.IRestModel;
import org.alfresco.rest.core.assertion.ModelAssertion;
import org.alfresco.utility.model.TestModel;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Handles single Deployment Entry JSON response
 *  "entry": {
 *  "id": "string",
 *  "name": "string",
 *  "category": "string",
 *  "deployedAt": "2016-10-04T13:15:36.222Z"
 *   }
 *
 * Created by Claudia Agache on 10/4/2016.
 */
public class RestDeploymentModel extends TestModel implements IRestModel<RestDeploymentModel>
{
    @JsonProperty(value = "entry")
    RestDeploymentModel model;
    
    @Override
    public RestDeploymentModel onModel()
    {
        return model;
    }
    
    public RestDeploymentModel()
    {
    }
    
    public RestDeploymentModel(String id)
    {
        setId(id);
    }
    

    @JsonProperty(required = true)
    private String id;
    private String name;
    private String category;
    private String deployedAt;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getCategory()
    {
        return category;
    }

    public void setCategory(String category)
    {
        this.category = category;
    }

    public String getDeployedAt()
    {
        return deployedAt;
    }

    public void setDeployedAt(String deployedAt)
    {
        this.deployedAt = deployedAt;
    }

    @Override
    public ModelAssertion<RestDeploymentModel> and() 
    {      
        return assertThat();
    }  
    
    @Override
    public ModelAssertion<RestDeploymentModel> assertThat() 
    {      
      return new ModelAssertion<>(this);
    }   
}    