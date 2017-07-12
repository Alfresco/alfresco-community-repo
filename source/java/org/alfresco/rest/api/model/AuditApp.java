/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * A representation of a Audit App
 *
 * @author janv
 * 
 */
public class AuditApp
{
	private String id;
    private String name;
    private Boolean isEnabled;

    public AuditApp()
    {
    }

    public AuditApp(String id, String name, boolean isEnabled)
    {
    	this.id = id;
        this.name = name;
        this.isEnabled = isEnabled;
    }

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

    @JsonProperty(value="isEnabled")
    public Boolean getIsEnabled()
    {
        return isEnabled;
    }

    @JsonProperty(value="isEnabled")
    public void setIsEnabled(Boolean isEnabled)
    {
        this.isEnabled = isEnabled;
    }

	@Override
	public String toString()
    {
		return "AuditApp [id=" + id + ", name= " + name + ", isEnabled=" + isEnabled + "]";
	}
}
