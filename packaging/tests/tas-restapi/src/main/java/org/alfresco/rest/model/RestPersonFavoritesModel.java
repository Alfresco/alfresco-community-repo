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

public class RestPersonFavoritesModel extends TestModel implements IRestModel<RestPersonFavoritesModel>
{
    @JsonProperty(value = "entry")
    RestPersonFavoritesModel model;

    @Override
    public RestPersonFavoritesModel onModel()
    {
        return model;
    }

    private String targetGuid;
    private String createdAt;
    private List<String> aspectNames;
    private List<String> allowableOperations;

    private RestTargetModel target;

    public RestPersonFavoritesModel()
    {}

    public RestPersonFavoritesModel(String targetGuid, String createdAt)
    {
        super();
        this.targetGuid = targetGuid;
        this.createdAt = createdAt;
    }

    public String getTargetGuid()
    {
        return targetGuid;
    }

    public void setTargetGuid(String targetGuid)
    {
        this.targetGuid = targetGuid;
    }

    public RestTargetModel getTarget()
    {
        return target;
    }

    public void setTarget(RestTargetModel target)
    {
        this.target = target;
    }

    public String getCreatedAt()
    {
        return createdAt;
    }

    public void setCreatedAt(String createdAt)
    {
        this.createdAt = createdAt;
    }

    public List<String> getAspectNames()
    {
        return aspectNames;
    }

    public void setAspectNames(List<String> aspectNames)
    {
        this.aspectNames = aspectNames;
    }

    public List<String> getAllowableOperations()
    {
        return allowableOperations;
    }

    public void setAllowableOperations(List<String> allowableOperations)
    {
        this.allowableOperations = allowableOperations;
    }
}
