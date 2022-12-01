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

import static org.alfresco.utility.report.log.Step.STEP;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.alfresco.rest.core.IRestModel;
import org.alfresco.utility.model.UserModel;
import org.testng.Assert;

/**
 * Created by Cristina Axinte on 9/26/2016.
 * 
 * {
  "entry": {
    "id": "string",
    "homeNetwork": true,
    "isEnabled": true,
    "createdAt": "2016-09-26T11:33:36.343Z",
    "paidNetwork": true,
    "subscriptionLevel": "Free",
    "quotas": [
      {
        "id": "string",
        "limit": 0,
        "usage": 0
      }
    ]
  }
}
 * 
 */
public class RestNetworkModel extends RestPersonNetworkModel implements IRestModel<RestNetworkModel>
{
    @JsonProperty(value = "entry")
    RestNetworkModel model;
    
    @Override
    public RestNetworkModel onModel()
    {
       return model;
    }
    
    public RestNetworkModel assertNetworkHasName(UserModel user)
    {
        STEP(String.format("REST API: Assert that network has name '%s'", user.getDomain()));
        Assert.assertTrue(getId().equalsIgnoreCase(user.getDomain()), "Network doesn't have the expected name.");

        return this;
    }
    
    public RestNetworkModel assertNetworkIsEnabled()
    {
        STEP(String.format("REST API: Assert network is enabled"));
        Assert.assertEquals(isEnabled(), true, "Network should be enabled.");

        return this;
    }
    public RestNetworkModel assertNetworkIsNotEnabled()
    {
        STEP(String.format("REST API: Assert that network is disable"));
        Assert.assertEquals(isEnabled(), false, "Network should be disabled.");

        return this;
    }
}
