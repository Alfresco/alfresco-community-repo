package org.alfresco.rest.requests.privateAPI;

import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.model.RestCustomTypeModel;
import org.alfresco.rest.model.RestSubscriberModel;
import org.alfresco.rest.model.RestSyncNodeSubscriptionModel;
import org.alfresco.rest.requests.CustomAspectModelManager;
import org.alfresco.rest.requests.CustomModelManager;
import org.alfresco.rest.requests.CustomTypeManager;
import org.alfresco.rest.requests.ModelRequest;
import org.alfresco.rest.requests.syncServiceAPI.Healthcheck;
import org.alfresco.rest.requests.syncServiceAPI.Subscribers;
import org.alfresco.rest.requests.syncServiceAPI.Subscriptions;
import org.alfresco.rest.requests.syncServiceAPI.Sync;
import org.alfresco.utility.model.CustomAspectModel;
import org.alfresco.utility.model.CustomContentModel;

import io.restassured.RestAssured;

/**
 * @author Bogdan Bocancea
 */
public class RestPrivateAPI extends ModelRequest<RestPrivateAPI>
{
    public RestPrivateAPI(RestWrapper restWrapper)
    {
        super(restWrapper);
        RestAssured.basePath = "alfresco/api/-default-/private/alfresco/versions/1";
        restWrapper.configureRequestSpec().setBasePath(RestAssured.basePath);
    }

    /**
     * Provides DSL on all REST calls under <code>cmm/{modelName}/...</code> API path
     * 
     * @param customContentModel {@link CustomContentModel}
     * @return {@link CustomModelManager}
     */
    public CustomModelManager usingCustomModel(CustomContentModel customContentModel)
    {
        return new CustomModelManager(customContentModel, restWrapper);
    }

    public CustomModelManager usingCustomModel()
    {
        return new CustomModelManager(restWrapper);
    }

    /**
     * Provides DSL on all REST calls under <code>cmm/{modelName}/aspects/{aspectName}...</code> API path
     * 
     * @param customContentModel {@link CustomContentModel}
     * @param aspectModel {@link CustomAspectModel}
     * @return {@link CustomAspectModelManager}
     */
    public CustomAspectModelManager usingAspect(CustomContentModel customContentModel, CustomAspectModel aspectModel)
    {
        return new CustomAspectModelManager(customContentModel, aspectModel, restWrapper);
    }

    /**
     * Provides DSL on all REST calls under <code>cmm/{modelName}/types/{typeName}...</code> API path
     *
     * @param customContentModel {@link CustomContentModel}
     * @param customType {@link RestCustomTypeModel}
     * @return {@link CustomTypeManager}
     */
    public CustomTypeManager usingCustomType(CustomContentModel customContentModel, RestCustomTypeModel customType)
    {
        return new CustomTypeManager(customContentModel, customType, restWrapper);
    }

    /**
     * Provides DSL on all REST calls under <code>subscribers/</code> API path
     * 
     * @return {@link Subscribers}
     */
    public Subscribers withSubscribers()
    {
        return new Subscribers(restWrapper);
    }

    /**
     * Provides DSL on all REST calls under <code>subscribers/{subscriberId}/</code> API path
     * 
     * @return {@link Subscribers}
     */
    public Subscriptions withSubscriber(RestSubscriberModel subscriber) throws Exception
    {
        return new Subscriptions(subscriber, restWrapper);
    }

    /**
     * Provides DSL on all REST calls under <code>subscribers/{subscriberId}/</code> API path
     * 
     * @return {@link Subscribers}
     */
    public Subscriptions withSubscriber(String subscriberID) throws Exception
    {
        RestSubscriberModel s = new RestSubscriberModel();
        s.setId(subscriberID);
        return new Subscriptions(s, restWrapper);
    }

    /**
     * Provides DSL on all REST calls under <code>subscribers/{subscriberId}/subscriptions/</code> API path
     * 
     * @return {@link Subscribers}
     */
    public Sync withSubscription(RestSyncNodeSubscriptionModel nodeSubscription) throws Exception
    {
        RestSubscriberModel s = new RestSubscriberModel();
        s.setId(nodeSubscription.getDeviceSubscriptionId());
        return new Sync(nodeSubscription, restWrapper);
    }
    /**
     * Provides DSL on all REST calls under <code>subscribers/</code> API path
     * 
     * @return {@link Subscribers}
     */
    public Healthcheck doHealthCheck()
    {
        return new Healthcheck(restWrapper);
    }

}
