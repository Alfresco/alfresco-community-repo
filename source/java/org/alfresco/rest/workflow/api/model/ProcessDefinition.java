package org.alfresco.rest.workflow.api.model;

public class ProcessDefinition
{

    String id;
    String key;
    String name;
    String category;
    int version;
    String deploymentId;
    String title;
    String description;
    String startFormResourceKey;
    Boolean isGraphicNotationDefined;
    
    public ProcessDefinition()
    {
    }

    public ProcessDefinition(org.activiti.engine.repository.ProcessDefinition processDefinition)
    {
        this.id = processDefinition.getId();
        this.name = processDefinition.getName();
        this.category = processDefinition.getCategory();
        this.version = processDefinition.getVersion();
        this.deploymentId = processDefinition.getDeploymentId();
    }

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

    public String getKey()
    {
        return key;
    }

    public void setKey(String key)
    {
        this.key = key;
    }

    public String getCategory()
    {
        return category;
    }

    public void setCategory(String category)
    {
        this.category = category;
    }

    public int getVersion()
    {
        return version;
    }

    public void setVersion(int version)
    {
        this.version = version;
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

    public Boolean isGraphicNotationDefined()
    {
        return isGraphicNotationDefined;
    }

    public void setGraphicNotationDefined(Boolean isGraphicNotationDefined)
    {
        this.isGraphicNotationDefined = isGraphicNotationDefined;
    }
}
