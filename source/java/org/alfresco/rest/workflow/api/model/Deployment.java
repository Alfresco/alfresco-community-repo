/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
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
