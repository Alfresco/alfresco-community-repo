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
