package org.alfresco.rest.model;

import org.alfresco.rest.core.IRestModel;
import org.alfresco.rest.core.assertion.ModelAssertion;
import org.alfresco.utility.model.TestModel;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Handles single Process Definition Entry JSON response
 * "entry": {
 * "deploymentId": "1",
 * "name": "Adhoc Activiti Process",
 * "description": "Assign a new task to yourself or a colleague",
 * "id": "activitiAdhoc:1:4",
 * "startFormResourceKey": "wf:submitAdhocTask",
 * "category": "http://alfresco.org",
 * "title": "New Task",
 * "version": 1,
 * "graphicNotationDefined": true,
 * "key": "activitiAdhoc"
 * }
 * Created by Claudia Agache on 10/13/2016.
 */
public class RestProcessDefinitionModel extends TestModel implements IRestModel<RestProcessDefinitionModel>
{
    @JsonProperty(value = "entry")
    RestProcessDefinitionModel model;

    @JsonProperty(required = true)
    private String id;
    private String deploymentId;
    private String name;
    private String description;
    private String startFormResourceKey;
    private String category;
    private String title;
    private int version;
    private boolean graphicNotationDefined;
    private String key;

    @Override
    public RestProcessDefinitionModel onModel()
    {
        return model;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getKey()
    {
        return key;
    }

    public void setKey(String key)
    {
        this.key = key;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getCategory()
    {
        return category;
    }

    public void setCategory(String category)
    {
        this.category = category;
    }

    public String getDeploymentId()
    {
        return deploymentId;
    }

    public void setDeploymentId(String deploymentId)
    {
        this.deploymentId = deploymentId;
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

    public String getStartFormResourceKey()
    {
        return startFormResourceKey;
    }

    public void setStartFormResourceKey(String startFormResourceKey)
    {
        this.startFormResourceKey = startFormResourceKey;
    }

    public boolean getGraphicNotationDefined()
    {
        return graphicNotationDefined;
    }

    public void setGraphicNotationDefined(boolean graphicNotationDefined)
    {
        this.graphicNotationDefined = graphicNotationDefined;
    }

    public int getVersion()
    {
        return version;
    }

    public void setVersion(int version)
    {
        this.version = version;
    }
 
    
    @Override
    public ModelAssertion<RestProcessDefinitionModel> and() 
    {      
      return assertThat();
    }

    @Override
    public ModelAssertion<RestProcessDefinitionModel> assertThat() 
    {      
      return new ModelAssertion<RestProcessDefinitionModel>(this);
    }
}

