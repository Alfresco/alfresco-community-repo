/*-
 * #%L
 * alfresco-tas-restapi
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
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
package org.alfresco.rest.search;

import org.alfresco.rest.core.IRestModel;
import org.alfresco.utility.model.TestModel;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RestRequestLimitsModel extends TestModel implements IRestModel<RestRequestLimitsModel>
{
    @JsonProperty(value = "entry")
    RestRequestLimitsModel model;

    @Override
    public RestRequestLimitsModel onModel()
    {
        return model;
    }
    
    public RestRequestLimitsModel(Integer permissionEvaluationTime, Integer permissionEvaluationCount,
            Integer trackTotalHitsLimit)
    {
        super();
        this.permissionEvaluationTime = permissionEvaluationTime;
        this.permissionEvaluationCount = permissionEvaluationCount;
        this.trackTotalHitsLimit = trackTotalHitsLimit;
    }

    private Integer permissionEvaluationTime;
    private Integer permissionEvaluationCount;
    private Integer trackTotalHitsLimit;

    public Integer getPermissionEvaluationTime()
    {
        return permissionEvaluationTime;
    }
    public void setPermissionEvaluationTime(Integer permissionEvaluationTime)
    {
        this.permissionEvaluationTime = permissionEvaluationTime;
    }
    public Integer getPermissionEvaluationCount()
    {
        return permissionEvaluationCount;
    }
    public void setPermissionEvaluationCount(Integer permissionEvaluationCount)
    {
        this.permissionEvaluationCount = permissionEvaluationCount;
    }
    public Integer getTrackTotalHitsLimit()
    {
        return trackTotalHitsLimit;
    }
    public void setTrackTotalHitsLimit(Integer trackTotalHitsLimit)
    {
        this.trackTotalHitsLimit = trackTotalHitsLimit;
    }
}
 
