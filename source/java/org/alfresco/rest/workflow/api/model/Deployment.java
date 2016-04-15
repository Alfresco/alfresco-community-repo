package org.alfresco.rest.workflow.api.model;

import java.util.Date;

public class Deployment
{

    String id;
    String name;
    String category;
    Date deployedAt;
    
    public Deployment()
    {
    }

    public Deployment(org.activiti.engine.repository.Deployment deployment)
    {
        this.id = deployment.getId();
        this.name = deployment.getName();
        this.category = deployment.getCategory();
        this.deployedAt = deployment.getDeploymentTime();
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
    
    public String getCategory()
    {
        return category;
    }

    public void setCategory(String category)
    {
        this.category = category;
    }

    public Date getDeployedAt()
    {
        return deployedAt;
    }

    public void setDeployedAt(Date deployedAt)
    {
        this.deployedAt = deployedAt;
    }
}
