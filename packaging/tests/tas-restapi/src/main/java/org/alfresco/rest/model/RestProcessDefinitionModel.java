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
}

