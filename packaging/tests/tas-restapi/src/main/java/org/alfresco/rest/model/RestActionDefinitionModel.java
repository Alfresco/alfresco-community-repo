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

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.alfresco.rest.core.IRestModel;
import org.alfresco.utility.model.TestModel;

public class RestActionDefinitionModel extends TestModel implements IRestModel<RestActionDefinitionModel>
{
    
    @JsonProperty(value = "entry")
    RestActionDefinitionModel actionDefinitionModel;
    private String id;
    private String name;
    private String title;
    private String description;
    private List<String> applicableTypes;
    private boolean adhocPropertiesAllowed;
    private boolean trackStatus;
    private List<RestParameterDefinitionModel> parameterDefinitions;

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
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

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public List<String> getApplicableTypes()
    {
        return applicableTypes;
    }

    public void setApplicableTypes(List<String> applicableTypes)
    {
        this.applicableTypes = applicableTypes;
    }

    public boolean isAdhocPropertiesAllowed()
    {
        return adhocPropertiesAllowed;
    }

    public void setAdhocPropertiesAllowed(boolean adhocPropertiesAllowed)
    {
        this.adhocPropertiesAllowed = adhocPropertiesAllowed;
    }

    public boolean isTrackStatus()
    {
        return trackStatus;
    }

    public void setTrackStatus(boolean trackStatus)
    {
        this.trackStatus = trackStatus;
    }
    
    public List<RestParameterDefinitionModel> getParameterDefinitions()
    {
        return parameterDefinitions;
    }

    public void setParameterDefinitions(List<RestParameterDefinitionModel> parameterDefinitions)
    {
        this.parameterDefinitions = parameterDefinitions;
    }

    @Override
    public RestActionDefinitionModel onModel()
    {
        return actionDefinitionModel;
    }

}
