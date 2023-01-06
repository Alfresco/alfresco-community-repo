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

/**
 * Generated from 'Alfresco Core REST API' swagger file
 * Base Path {@linkplain /alfresco/api/-default-/public/alfresco/versions/1}
 */
public class RestNodeBodyModel extends TestModel implements IRestModel<RestNodeBodyModel>
{
    @JsonProperty(value = "entry")
    RestNodeBodyModel model;

    @Override
    public RestNodeBodyModel onModel()
    {
        return model;
    }

    /**
     * The name must not contain spaces or the following special characters: * " < > \ / ? : and |.
     * The character . must not be used at the end of the name.
     */

    @JsonProperty(required = true)
    private String name;

    @JsonProperty(required = true)
    private String nodeType;

    private List<String> aspectNames;

    private Object properties;

    private String relativePath;

    private Object association;

    private List<String> secondaryChildren;

    private List<String> targets;

    public String getName()
    {
        return this.name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getNodeType()
    {
        return this.nodeType;
    }

    public void setNodeType(String nodeType)
    {
        this.nodeType = nodeType;
    }

     

    public Object getProperties()
    {
        return this.properties;
    }

    public void setProperties(Object properties)
    {
        this.properties = properties;
    }

    public String getRelativePath()
    {
        return this.relativePath;
    }

    public void setRelativePath(String relativePath)
    {
        this.relativePath = relativePath;
    }

    public Object getAssociation()
    {
        return this.association;
    }

    public void setAssociation(Object association)
    {
        this.association = association;
    }

    public List<String> getAspectNames()
    {
        return aspectNames;
    }

    public void setAspectNames(List<String> aspectNames)
    {
        this.aspectNames = aspectNames;
    }

    public List<String> getSecondaryChildren()
    {
        return secondaryChildren;
    }

    public void setSecondaryChildren(List<String> secondaryChildren)
    {
        this.secondaryChildren = secondaryChildren;
    }

    public List<String> getTargets()
    {
        return targets;
    }

    public void setTargets(List<String> targets)
    {
        this.targets = targets;
    }
}
