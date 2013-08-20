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

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

/**
 * Representation of a variable in the Activiti engine.
 * 
 * @author Frederik Heremans
 */
public class Variable
{
    protected String name;
    protected String type;
    protected Object value;
    
    public String getName()
    {
        return this.name;
    }
    public void setName(String name)
    {
        this.name = name;
    }
    public String getType()
    {
        return this.type;
    }
    public void setType(String type)
    {
        this.type = type;
    }
    @JsonSerialize(include=Inclusion.ALWAYS)
    public Object getValue()
    {
        return this.value;
    }
    public void setValue(Object value)
    {
        this.value = value;
    }
}
