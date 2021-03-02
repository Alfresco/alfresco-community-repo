package org.alfresco.rest.model;

import java.util.Map;

public class RestConstraintDefinitionModel
{
    private String id;
    private String type;
    private String title;
    private String description;
    private Map<String, Object> parameters;

    public String getId()
    {
        return id;
    }

    public void setId(String id) 
    {
        this.id = id;
    }

    public String getType() 
    {
        return type;
    }

    public void setType(String type) 
    {
        this.type = type;
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

    public Map<String, Object> getParameters() 
    {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters)
    {
        this.parameters = parameters;
    }
}