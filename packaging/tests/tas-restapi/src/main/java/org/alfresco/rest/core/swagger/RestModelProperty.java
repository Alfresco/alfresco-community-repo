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
package org.alfresco.rest.core.swagger;

import java.util.AbstractMap;
import java.util.Map.Entry;

import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.RefProperty;

/**
 * Model property used by freemarker template in {@link SwaggerModel}
 *
 */
public class RestModelProperty
{
    private String name;
    private String type;
    private String description;
    private Boolean isRequired = false;
    /** If the property is a ref then this is the type referenced. */
    private String simpleRef;
    /** If the property is an array then this is the type of the items in it. */
    private RestModelProperty itemsType;

    public static RestModelProperty build(Entry<String, io.swagger.models.properties.Property> property)
    {
        RestModelProperty model = new RestModelProperty();
        model.setName(property.getKey());
        model.setType(property.getValue().getType());
        model.setDescription(property.getValue().getDescription());
        model.setIsRequired(property.getValue().getRequired());
        if (property.getValue() instanceof RefProperty)
        {
            RefProperty refProperty = (RefProperty) property.getValue();
            model.setSimpleRef(refProperty.getSimpleRef());
        }
        if (property.getValue() instanceof ArrayProperty)
        {
            ArrayProperty arrayProperty = (ArrayProperty) property.getValue();
            Entry<String, io.swagger.models.properties.Property> itemsEntry = new AbstractMap.SimpleEntry<String, io.swagger.models.properties.Property>(
                        arrayProperty.getName(), arrayProperty.getItems());
            model.setItemsType(RestModelProperty.build(itemsEntry));
        }

        return model;
    }

    public String getName()
    {
        return name;
    }

    public String getNameCapitalized()
    {
        return org.apache.commons.lang3.StringUtils.capitalize(name);
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getType()
    {
        switch (this.type)
        {
            case "string":
                return "String";
            case "integer":
                return "int";
            case "object":
                return "Object";
            case "ref":
                return "Rest" + getSimpleRef() + "Model";
            case "array":
                return "List<" + getItemsType().getType() + ">";
            default:
                return type;
        }
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public Boolean isRequired()
    {
        return isRequired;
    }

    public void setIsRequired(Boolean isRequired)
    {
        this.isRequired = isRequired;
    }

    protected void setSimpleRef(String simpleRef)
    {
        this.simpleRef = simpleRef;
    }

    public String getSimpleRef()
    {
        return simpleRef;
    }

    protected void setItemsType(RestModelProperty itemsType)
    {
        this.itemsType = itemsType;
    }

    public RestModelProperty getItemsType()
    {
        return itemsType;
    }
}
