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
import org.alfresco.utility.data.RandomData;
import org.alfresco.utility.model.TestModel;

/**
 * Handles a single Variable JSON response
 * Example:
 * {
 *      "scope": "string",
 *      "name": "string",
 *      "value": 0,
 *      "type": "string"
 * }
 * 
 * @author Cristina Axinte
 */
public class RestVariableModel extends TestModel implements IRestModel<RestVariableModel>
{
    private String scope;
    private String name;
    private Object value;
    private String type;

    @JsonProperty(value = "entry")
    RestVariableModel model;

    public RestVariableModel()
    {
    }

    public RestVariableModel(String scope, String name, String type, Object value)
    {
        this.scope = scope;
        this.name = name;
        this.type = type;
        this.value = value;
    }

    public String getScope()
    {
        return scope;
    }

    public void setScope(String scope)
    {
        this.scope = scope;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Object getValue()
    {
        return value;
    }

    public void setValue(Object value)
    {
        this.value = value;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public static RestVariableModel getRandomTaskVariableModel(String scope, String type)
    {
        return new RestVariableModel(scope, RandomData.getRandomName("name"), type, RandomData.getRandomName("value"));
    }

    @Override
    public RestVariableModel onModel()
    {
        return model;
    }

}
