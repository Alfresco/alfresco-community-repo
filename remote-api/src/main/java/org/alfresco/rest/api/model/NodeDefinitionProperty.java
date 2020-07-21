/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
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
package org.alfresco.rest.api.model;

import java.util.List;
/**
 * Represents a property of the node definition.
 *
 * @author gfertuso
 */
public class NodeDefinitionProperty 
{
    private String id;
    private String title;
    private String description;
    private String defaultValue;
    private String dataType;
    private Boolean isMultiValued;
    private Boolean isMandatory;
    private Boolean isMandatoryEnforced;
    private Boolean isProtected;
    private List<NodeDefinitionConstraint> constraints;

    public String getId() 
    {
        return id;
    }

    public void setId(String id) 
    {
        this.id = id;
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

    public boolean getIsProtected()
    {
        return isProtected;
    }

    public void setIsProtected(boolean isProtected)
    {
        this.isProtected = isProtected;
    }

    public List<NodeDefinitionConstraint> getConstraints() 
    {
        return constraints;
    }

    public void setConstraints(List<NodeDefinitionConstraint> constraints) 
    {
        this.constraints = constraints;
    }

    public boolean getIsMultiValued() 
    {
        return isMultiValued;
    }

    public void setIsMultiValued(boolean isMultiValued) 
    {
        this.isMultiValued = isMultiValued;
    }

    public boolean getIsMandatory() 
    {
        return isMandatory;
    }

    public void setIsMandatory(boolean isMandatory) 
    {
        this.isMandatory = isMandatory;
    }

    public boolean getIsMandatoryEnforced() 
    {
        return isMandatoryEnforced;
    }

    public void setIsMandatoryEnforced(boolean isMandatoryEnforced) 
    {
        this.isMandatoryEnforced = isMandatoryEnforced;
    }
    
}