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
package org.alfresco.rest.requests.privateAPI;

import io.restassured.RestAssured;
import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.model.RestCustomTypeModel;
import org.alfresco.rest.model.RestSubscriberModel;
import org.alfresco.rest.model.RestSyncNodeSubscriptionModel;
import org.alfresco.rest.requests.CustomAspectModelManager;
import org.alfresco.rest.requests.CustomModelManager;
import org.alfresco.rest.requests.CustomTypeManager;
import org.alfresco.rest.requests.ModelRequest;
import org.alfresco.rest.requests.Node;
import org.alfresco.rest.requests.syncServiceAPI.Healthcheck;
import org.alfresco.rest.requests.syncServiceAPI.Subscribers;
import org.alfresco.rest.requests.syncServiceAPI.Subscriptions;
import org.alfresco.rest.requests.syncServiceAPI.Sync;
import org.alfresco.utility.model.CustomAspectModel;
import org.alfresco.utility.model.CustomContentModel;
import org.alfresco.utility.model.RepoTestModel;

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
    public Subscriptions withSubscriber(RestSubscriberModel subscriber)
    {
        return new Subscriptions(subscriber, restWrapper);
    }

    /**
     * Provides DSL on all REST calls under <code>subscribers/{subscriberId}/</code> API path
     * 
     * @return {@link Subscribers}
     */
    public Subscriptions withSubscriber(String subscriberID)
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
    public Sync withSubscription(RestSyncNodeSubscriptionModel nodeSubscription)
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

    /**
     * Provides DSL on all REST calls under <code>nodes/</code> API path
     *
     * @param node
     * @return
     */
    public Node usingNode(RepoTestModel node)
    {
        return new Node(node, restWrapper);
    }

}
