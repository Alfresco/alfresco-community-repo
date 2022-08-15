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
import org.alfresco.utility.model.SiteModel;

/**
 * Handles single Site JSON responses
 * Example:
 * "visibility": "PUBLIC",
 * "guid": "79e140e1-5039-4efa-acaf-c22b5ba7c947",
 * "description": "Description1470255221170",
 * "id": "0-C2291-1470255221170",
 * "title": "0-C2291-1470255221170"
 */
public class RestSiteModel extends SiteModel implements IRestModel<RestSiteModel>
{
    private String role;

    public String getPreset()
    {
        return preset;
    }

    public void setPreset(String preset)
    {
        this.preset = preset;
    }

    private String preset;

    @JsonProperty(value = "entry")
    RestSiteModel model;

    @Override
    public RestSiteModel onModel()
    {
        return model;
    }

    public String getRole()
    {
        return role;
    }

    public void setRole(String role)
    {
        this.role = role;        
    }
}
