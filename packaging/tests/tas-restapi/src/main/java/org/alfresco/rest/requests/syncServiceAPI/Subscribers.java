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
package org.alfresco.rest.requests.syncServiceAPI;

import java.util.HashMap;

import org.alfresco.rest.core.JsonBodyGenerator;
import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.model.RestSubscriberModel;
import org.alfresco.rest.model.RestSubscriberModelCollection;
import org.alfresco.rest.requests.ModelRequest;
import org.alfresco.rest.requests.privateAPI.RestPrivateAPI;
import org.springframework.http.HttpMethod;

/**
 * Declares all Rest API under the /subscribers path
 * 
 * @author Meenal Bhave
 */
public class Subscribers extends ModelRequest<RestPrivateAPI>
{   
    public Subscribers(RestWrapper restWrapper)
    {
        super(restWrapper);
    }

    /**
     * Get Subscription(s) using GET call on /subscribers    
     * @return {@link RestSubscriberModelCollection}
     */
    public RestSubscriberModelCollection getSubscribers()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "subscribers?{parameters}", restWrapper.getParameters());
        return restWrapper.processModels(RestSubscriberModelCollection.class, request);
    }

    /**
     * POST Create Device Subscription using POST call on /subscribers
     *     
     * @param deviceOS
     * @param clientVersion
     * @return {@link RestSubscriberModel}
     */
    public RestSubscriberModel registerDevice(String deviceOS, String clientVersion)
    {
        HashMap<String, String> body = new HashMap<String, String>();
        body.put("deviceOS", deviceOS);
        body.put("clientVersion", clientVersion);
        String postBody = JsonBodyGenerator.keyValueJson(body);

        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, postBody, "subscribers?{parameters}", restWrapper.getParameters());
        return restWrapper.processModel(RestSubscriberModel.class, request);
    }

}
