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
 * Handles single representation of a Start Form Model
 * * "entry": {
 * "allowedValues": [
 * "1",
 * "2",
 * "3"
 * ],
 * "qualifiedName": "{http://www.alfresco.org/model/bpm/1.0}workflowPriority",
 * "defaultValue": "2",
 * "dataType": "d:int",
 * "name": "bpm_workflowPriority",
 * "title": "Workflow Priority",
 * "required": false
 * }
 * Created by Claudia Agache on 10/18/2016.
 */
public class RestFormModel extends TestModel implements IRestModel<RestFormModel>
{
    @JsonProperty(value = "entry") RestFormModel model;
    private String qualifiedName;
    private String defaultValue;
    private String dataType;
    private String name;
    private String title;
    private String required;
    private String[] allowedValues;

    public RestFormModel onModel()
    {
        return model;
    }

    public String[] getAllowedValues()
    {
        return allowedValues;
    }

    public void setAllowedValues(String[] allowedValues)
    {
        this.allowedValues = allowedValues;
    }

    public String getQualifiedName()
    {
        return qualifiedName;
    }

    public void setQualifiedName(String qualifiedName)
    {
        this.qualifiedName = qualifiedName;
    }

    public String getDefaultValue()
    {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue)
    {
        this.defaultValue = defaultValue;
    }

    public String getDataType()
    {
        return dataType;
    }

    public void setDataType(String dataType)
    {
        this.dataType = dataType;
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

    public String getRequired()
    {
        return required;
    }

    public void setRequired(String required)
    {
        this.required = required;
    }
}
