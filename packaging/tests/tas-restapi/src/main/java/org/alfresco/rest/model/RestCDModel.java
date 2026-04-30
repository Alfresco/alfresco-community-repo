/*-
 * #%L
 * alfresco-tas-restapi
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
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

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.alfresco.rest.core.IRestModel;
import org.alfresco.utility.model.TestModel;

/**
 * TAS model representing a cascading dictionary entry. Maps to {@code CDResponse} returned by {@code GET /cascading-dictionaries} and {@code GET /cascading-dictionaries/{aspectId}} REST API endpoints.
 */
public class RestCDModel extends TestModel implements IRestModel<RestCDModel>
{
    @JsonProperty(value = "entry")
    RestCDModel model;

    @Override
    public RestCDModel onModel()
    {
        return model;
    }

    private String name;
    private String aspect;
    private String keyProperty;
    private String versionProperty;
    private String version;
    private Date createdAt;
    private Date modifiedAt;
    private List<RestCDLevelModel> levels;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getAspect()
    {
        return aspect;
    }

    public void setAspect(String aspect)
    {
        this.aspect = aspect;
    }

    public String getKeyProperty()
    {
        return keyProperty;
    }

    public void setKeyProperty(String keyProperty)
    {
        this.keyProperty = keyProperty;
    }

    public String getVersionProperty()
    {
        return versionProperty;
    }

    public void setVersionProperty(String versionProperty)
    {
        this.versionProperty = versionProperty;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion(String version)
    {
        this.version = version;
    }

    public Date getCreatedAt()
    {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt)
    {
        this.createdAt = createdAt;
    }

    public Date getModifiedAt()
    {
        return modifiedAt;
    }

    public void setModifiedAt(Date modifiedAt)
    {
        this.modifiedAt = modifiedAt;
    }

    public List<RestCDLevelModel> getLevels()
    {
        return levels;
    }

    public void setLevels(List<RestCDLevelModel> levels)
    {
        this.levels = levels;
    }
}
