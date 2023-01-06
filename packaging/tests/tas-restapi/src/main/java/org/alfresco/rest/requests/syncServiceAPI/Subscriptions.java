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

import javax.json.JsonArrayBuilder;

import org.alfresco.rest.core.JsonBodyGenerator;
import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.model.RestSubscriberModel;
import org.alfresco.rest.model.RestSyncNodeSubscriptionModel;
import org.alfresco.rest.model.RestSyncNodeSubscriptionModelCollection;
import org.alfresco.rest.requests.ModelRequest;
import org.alfresco.rest.requests.privateAPI.RestPrivateAPI;
import org.alfresco.utility.Utility;
import org.springframework.http.HttpMethod;

/**
 * Declares all Rest API under the /subscribers/<subscriberId>/subscriptions/ path
 * 
 * @author Meenal Bhave
 */
public class Subscriptions extends ModelRequest<RestPrivateAPI>
{
    public enum TYPE
    {
        BOTH, CONTENT, METADATA
    }

    private RestSubscriberModel subscriber;
    private String subscriptionsURL = "subscribers/{deviceSubscriptionId}/subscriptions";
    String nodeSubscriptionURL = subscriptionsURL + "/{nodeSubscriptionId}";
    String params = "?{parameters}";

    public Subscriptions(RestSubscriberModel subscriber, RestWrapper restWrapper)
    {
        super(restWrapper);
        this.subscriber = subscriber;
        Utility.checkObjectIsInitialized(this.subscriber, "Subscriber Device");
    }

    /**
     * Create Node Subscriptions for multiple targets using POST call on /subscribers/{deviceSubscriptionId}/subscriptions
     * 
     * @param targetNodeIds: one or more
     * @return RestSyncNodeSubscriptionModel
     * @throws EmptyJsonResponseException, JsonToModelConversionException
     */
    public RestSyncNodeSubscriptionModelCollection subscribeToNodes(String... targetNodeIds)
    {

        JsonArrayBuilder array = JsonBodyGenerator.defineJSONArray();
        for (String targetNodeId : targetNodeIds)
        {
            array.add(JsonBodyGenerator.defineJSON().add("targetNodeId", targetNodeId).add("subscriptionType", TYPE.BOTH.toString()));
        }

        String postBody = array.build().toString();

        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, postBody, subscriptionsURL + params, this.subscriber.getId(),
                restWrapper.getParameters());

        return restWrapper.processModels(RestSyncNodeSubscriptionModelCollection.class, request);
    }

    /**
     * Create Node Subscription for the specified target node using POST call on /subscribers/{deviceSubscriptionId}/subscriptions
     * 
     * @param targetNodeId
     * @param subscriberType
     * @return
     */
    public RestSyncNodeSubscriptionModel subscribeToNode(String targetNodeId, TYPE subscriberType)
    {
        HashMap<String, String> body = new HashMap<String, String>();
        body.put("targetNodeId", targetNodeId);
        body.put("subscriptionType", subscriberType.toString());
        String postBody = JsonBodyGenerator.keyValueJson(body);

        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, postBody, subscriptionsURL + params, this.subscriber.getId(),
                restWrapper.getParameters());
        return restWrapper.processModel(RestSyncNodeSubscriptionModel.class, request);
    }

    /**
     * Get NODE Subscription(s) using GET call on /subscribers/{deviceSubscriptionId}/subscriptions
     * 
     * @return {@link RestSyncNodeSubscriptionModelCollection}
     */
    public RestSyncNodeSubscriptionModelCollection getSubscriptions()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, subscriptionsURL + params, this.subscriber.getId(), restWrapper.getParameters());
        return restWrapper.processModels(RestSyncNodeSubscriptionModelCollection.class, request);
    }

    /**
     * Get NODE Subscription using GET call on /subscribers/{deviceSubscriptionId}/subscriptions/{nodeSubscriptionId}
     * 
     * @return RestSyncNodeSubscriptionModelCollection
     * @throws EmptyJsonResponseException, JsonToModelConversionException
     */
    public RestSyncNodeSubscriptionModel getSubscription(String nodeSubscriptionId)
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, nodeSubscriptionURL + params, this.subscriber.getId(), nodeSubscriptionId,
                restWrapper.getParameters());
        return restWrapper.processModel(RestSyncNodeSubscriptionModel.class, request);
    }

}
