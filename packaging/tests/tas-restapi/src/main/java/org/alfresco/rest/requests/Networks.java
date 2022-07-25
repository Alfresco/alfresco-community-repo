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
package org.alfresco.rest.requests;

import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.model.RestNetworkModel;
import org.alfresco.utility.Utility;
import org.alfresco.utility.model.UserModel;
import org.json.JSONObject;
import org.springframework.http.HttpMethod;

/**
 * Declares all Rest API under the /networks path
 *
 */
public class Networks extends ModelRequest<Networks>
{
    public Networks(RestWrapper restWrapper)
    {
        super(restWrapper);
    }

    /**
     * Retrieve details for the current user network using GET call on "networks/{networkId}"
     *
     * @return
     */
    public RestNetworkModel getNetwork()
    {
        return getNetwork(restWrapper.getTestUser());
    }

    /**
     * Retrieve details of a specific network using GET call on "networks/{networkId}"
     *
     * @return
     */
    public RestNetworkModel getNetwork(UserModel tenant)
    {
        Utility.checkObjectIsInitialized(tenant.getDomain(), "tenant.getDomain()");
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "networks/{networkId}", tenant.getDomain());
        return restWrapper.processModel(RestNetworkModel.class, request);
    }
    
    /**
     * Retrieve details of a specific network using GET call with parameters on "networks/{networkId}?{parameters}"
     *
     * @return JSONObject
     */
    public JSONObject getNetworkWithParams(UserModel tenant)
    {
        Utility.checkObjectIsInitialized(tenant.getDomain(), "tenant.getDomain()");
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "networks/{networkId}?{parameters}", tenant.getDomain(), restWrapper.getParameters());
        return restWrapper.processJson(request);
    }
}
