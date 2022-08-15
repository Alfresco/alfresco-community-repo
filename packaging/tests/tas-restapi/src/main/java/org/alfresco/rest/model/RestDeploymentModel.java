/*-
 * #%L
 * alfresco-tas-restapi
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.alfresco.rest.core.IRestModel;
import org.alfresco.utility.model.TestModel;

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
}
