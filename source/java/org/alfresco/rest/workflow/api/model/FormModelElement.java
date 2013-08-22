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

import java.util.List;

public class FormModelElement
{
    String name;
    String qualifiedName;
    String title;
    String dataType;
    boolean required;
    String defaultValue;
    List<String> allowedValues;
    
    public String getName()
    {
        return name;
    }
    public void setName(String name)
    {
        this.name = name;
    }
    public String getQualifiedName()
    {
        return qualifiedName;
    }
    public void setQualifiedName(String qualifiedName)
    {
        this.qualifiedName = qualifiedName;
    }
    public String getTitle()
    {
        return title;
    }
    public void setTitle(String title)
    {
        this.title = title;
    }
    public String getDataType()
    {
        return dataType;
    }
    public void setDataType(String dataType)
    {
        this.dataType = dataType;
    }
    public boolean isRequired()
    {
        return required;
    }
    public void setRequired(boolean required)
    {
        this.required = required;
    }
    public String getDefaultValue()
    {
        return defaultValue;
    }
    public void setDefaultValue(String defaultValue)
    {
        this.defaultValue = defaultValue;
    }
	public List<String> getAllowedValues() 
	{
		return allowedValues;
	}
	public void setAllowedValues(List<String> allowedValues) 
	{
		this.allowedValues = allowedValues;
	}
}
