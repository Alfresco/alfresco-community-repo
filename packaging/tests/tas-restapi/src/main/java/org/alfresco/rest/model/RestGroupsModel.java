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

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.alfresco.rest.core.IRestModel;
import org.alfresco.utility.model.TestModel;

public class RestGroupsModel extends TestModel implements IRestModel<RestGroupsModel>
{
    @JsonProperty(required = true)
    private String id;
    @JsonProperty(required = true)
    private String displayName;
    @JsonProperty()
    private String description;
    @JsonProperty(required = true)
    private Boolean isRoot;
    @JsonProperty(required = true)
    private Boolean hasSubgroups;

    @JsonProperty("parentIds")
    private ArrayList<String> parentIds;
    @JsonProperty("zones")
    private ArrayList<String> zones;

    @JsonProperty(value = "entry")
    RestGroupsModel model;

    @Override
    public RestGroupsModel onModel()
    {
        return model;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public void setDisplayName(String displayName)
    {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getHasSubgroups() {
        return hasSubgroups;
    }

    public void setHasSubgroups(Boolean hasSubgroups) {
        this.hasSubgroups = hasSubgroups;
    }

    public Boolean getIsRoot()
    {
        return isRoot;
    }

    public void setIsRoot(Boolean isRoot)
    {
        this.isRoot = isRoot;
    }

    public ArrayList<String> getParentIds()
    {
        return parentIds;
    }

    public void setParentIds(ArrayList<String> parentIds)
    {
        this.parentIds = parentIds;
    }

    public ArrayList<String> getZones()
    {
        return zones;
    }

    public void setZones(ArrayList<String> zones)
    {
        this.zones = zones;
    }
}
