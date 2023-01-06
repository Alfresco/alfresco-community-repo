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

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.alfresco.rest.core.IRestModel;
import org.alfresco.utility.model.TestModel;

public class RestAuditEntryModel extends TestModel implements IRestModel<RestAuditEntryModel>
{
    @JsonProperty(value = "entry")
    RestAuditEntryModel model;

    @Override
    public RestAuditEntryModel onModel()
    {
        return model;
    }

    @JsonProperty(required = true)
    private String id;
    private String auditApplicationId ;
    private String createdAt;
    @JsonProperty("createdByUser")
    private RestByUserModel createdByUser;

    @JsonProperty("values")
    Map<String, Object>  values;

public Map<String, Object> getValues()
    {
        return values;
    }

    public void setValues(Map<String, Object> values)
    {
        this.values = values;
    }

    public String getId()
    {
        return this.id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getAuditApplicationId()
    {
        return this.auditApplicationId;
    }

    public void setAuditApplicationId(String auditApplicationId)
    {
        this.auditApplicationId = auditApplicationId;
    }

    public RestByUserModel getCreatedByUser()
    {
        return this.createdByUser;
    }

    public void setCreatedByUser(RestByUserModel createdByUser)
    {
        this.createdByUser = createdByUser;
    }

    public String getCreatedAt()
    {
        return this.createdAt;
    }

    public void setCreatedAt(String createdAt)
    {
        this.createdAt = createdAt;
    }

}
