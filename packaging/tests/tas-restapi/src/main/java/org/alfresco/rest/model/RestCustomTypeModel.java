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
import org.alfresco.utility.model.CustomAspectPropertiesModel;
import org.alfresco.utility.model.TestModel;

/**
 * @author Bogdan Bocancea
 */
public class RestCustomTypeModel extends TestModel implements IRestModel<RestCustomTypeModel>
{
    @JsonProperty(value = "entry")
    RestCustomTypeModel model;

    @JsonProperty(required = true)
    private String name;

    @JsonProperty(required = true)
    private String parentName;

    @JsonProperty
    private String title;

    @JsonProperty
    private String description;

    @JsonProperty
    private List<CustomAspectPropertiesModel> properties;

    public RestCustomTypeModel()
    {

    }

    public RestCustomTypeModel(String name, String parentName)
    {
        this.name = name;
        this.parentName = parentName;
    }

    public RestCustomTypeModel(String name, String parentName, String title)
    {
        this.name = name;
        this.parentName = parentName;
        this.title = title;
    }

    @Override
    public RestCustomTypeModel onModel()
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

    public String getParentName()
    {
        return parentName;
    }

    public void setParentName(String parentName)
    {
        this.parentName = parentName;
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

    public List<CustomAspectPropertiesModel> getProperties()
    {
        return properties;
    }

    public void setProperties(List<CustomAspectPropertiesModel> properties)
    {
        this.properties = properties;
    }
}
