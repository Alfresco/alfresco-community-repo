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

public class RestParameterDefinitionModel extends TestModel implements IRestModel<RestParameterDefinitionModel>
{
    private String name;
    private String type;
    private boolean multiValued;
    private boolean mandatory;
    private String displayLabel;
    private String parameterConstraintName;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public boolean isMultiValued()
    {
        return multiValued;
    }

    public void setMultiValued(boolean multiValued)
    {
        this.multiValued = multiValued;
    }

    public boolean isMandatory()
    {
        return mandatory;
    }

    public void setMandatory(boolean mandatory)
    {
        this.mandatory = mandatory;
    }

    public String getDisplayLabel()
    {
        return displayLabel;
    }

    public void setDisplayLabel(String displayLabel)
    {
        this.displayLabel = displayLabel;
    }

    public String getParameterConstraintName()
    {
        return parameterConstraintName;
    }

    public void setParameterConstraintName(String parameterConstraintName)
    {
        this.parameterConstraintName = parameterConstraintName;
    }

    public RestParameterDefinitionModel getParameterDefinitionModel()
    {
        return parameterDefinitionModel;
    }

    public void setParameterDefinitionModel(RestParameterDefinitionModel parameterDefinitionModel)
    {
        this.parameterDefinitionModel = parameterDefinitionModel;
    }

    @JsonProperty(value = "entry")
    RestParameterDefinitionModel parameterDefinitionModel;

    @Override
    public RestParameterDefinitionModel onModel()
    {
        return parameterDefinitionModel;
    }

}
