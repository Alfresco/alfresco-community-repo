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
/*
 * Copyright 2017 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */

package org.alfresco.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.alfresco.rest.core.IRestModel;
import org.alfresco.utility.model.TestModel;

/**
 * Base Path {@linkplain /alfresco/api/-default-/private/alfresco/versions/1}
 * 
 * @author Meenal Bhave { "entry": { "deviceSubscriptionId": "012d6bf6-8b11-4dc3-bd45-6e4f77b48f67", "createdAt": "2017-08-11T15:54:41.444+0000", "targetPath": "/Company Home/Data Dictionary", "state": "VALID", "id": "5d120857-e155-44bc-9d1f-97ead5631090", "targetNodeId": "20ad767c-4d86-4d9f-91a6-2e82fffa4e87" } }
 */
public class RestSyncNodeSubscriptionModel extends TestModel implements IRestModel<RestSyncNodeSubscriptionModel>
{

    public RestSyncNodeSubscriptionModel()
    {}

    public RestSyncNodeSubscriptionModel(String targetNodeId, String subscriptionType)
    {
        super();
        this.createdAt = targetNodeId;
        this.targetNodeId = subscriptionType;
    }

    @JsonProperty(value = "entry")
    RestSyncNodeSubscriptionModel model;

    @Override
    public RestSyncNodeSubscriptionModel onModel()
    {
        return model;
    }

    @JsonProperty(required = true)
    private String deviceSubscriptionId;

    @JsonProperty(required = true)
    private String createdAt;

    @JsonProperty(required = true)
    private String targetPath;

    @JsonProperty(required = true)
    private String state;

    @JsonProperty(required = true)
    private String id;

    @JsonProperty(required = true)
    private String targetNodeId;

    public RestSyncNodeSubscriptionModel getModel()
    {
        return model;
    }

    public String getDeviceSubscriptionId()
    {
        return deviceSubscriptionId;
    }

    public String getCreatedAt()
    {
        return createdAt;
    }

    public String getTargetPath()
    {
        return targetPath;
    }

    public String getState()
    {
        return state;
    }

    public String getId()
    {
        return id;
    }

    public String getTargetNodeId()
    {
        return targetNodeId;
    }

}
