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

import java.util.List;

import jakarta.json.JsonArrayBuilder;

import org.alfresco.rest.core.JsonBodyGenerator;
import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.model.RestSyncNodeSubscriptionModel;
import org.alfresco.rest.model.RestSyncSetChangesModel;
import org.alfresco.rest.model.RestSyncSetGetModel;
import org.alfresco.rest.model.RestSyncSetRequestModel;
import org.alfresco.rest.requests.ModelRequest;
import org.alfresco.rest.requests.privateAPI.RestPrivateAPI;
import org.alfresco.utility.Utility;
import org.springframework.http.HttpMethod;

import io.restassured.http.ContentType;

/**
 * Declares all Rest API under the /subscribers/<subscriberId>/subscriptions/<subscriptionId>/sync path
 * 
 * @author Meenal Bhave
 */
public class Sync extends ModelRequest<RestPrivateAPI>
{
    String subscriber;
    RestSyncNodeSubscriptionModel subscription;
    String requestSyncURL = "subscribers/{deviceSubscriptionId}/subscriptions/{nodeSubscriptionId}/sync";
    String syncSetURL = requestSyncURL + "/{syncId}";

    public Sync(RestSyncNodeSubscriptionModel subscription, RestWrapper restWrapper)
    {
        super(restWrapper);
        restWrapper.configureSyncServiceEndPoint();
        this.subscription = subscription;
        this.subscriber = subscription.getDeviceSubscriptionId();
        Utility.checkObjectIsInitialized(this.subscription, "Node Subscription");
    }

    /**
     * Request sync for the specified subscriber/subscription using POST call on /subscribers/{deviceSubscriptionId}/subscriptions/{nodeSubscriptionId}/sync
     * 
     * @param nodeSubscriptionId
     * @param clientChanges
     * @return
     */
    public RestSyncSetRequestModel startSync(RestSyncNodeSubscriptionModel nodeSubscriptionModel, List<RestSyncSetChangesModel> clientChanges, String clientVersion)
    {
        JsonArrayBuilder array = JsonBodyGenerator.defineJSONArray();
        for (RestSyncSetChangesModel change : clientChanges)
        {
            array.add(JsonBodyGenerator.defineJSON().add("changes", change.toJson()));
        }

        String postBody = JsonBodyGenerator.defineJSON().add("changes", array.build()).add("clientVersion", clientVersion).build().toString();

        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, postBody, requestSyncURL, this.subscriber, nodeSubscriptionModel.getId(),
                restWrapper.getParameters());

        // This step is necessary for this request. Without it, empty json response is returned
        restWrapper.configureRequestSpec().setContentType(ContentType.JSON.withCharset(request.getContentType()));
        RestSyncSetRequestModel model = restWrapper.processModelWithoutEntryObject(RestSyncSetRequestModel.class, request);
        restWrapper.initializeRequestSpecBuilder();
        return model;
    }

    public RestWrapper endSync(RestSyncNodeSubscriptionModel nodeSubscriptionModel, RestSyncSetRequestModel sync)
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.DELETE, syncSetURL, this.subscriber, nodeSubscriptionModel.getId(),
                sync.getSyncId(), restWrapper.getParameters());

        restWrapper.processEmptyModel(request);
        restWrapper.initializeRequestSpecBuilder();
        return restWrapper;
    }

    /**
     * Gets the Change Set for the specified subscriber/subscription and SyncId using GET call on
     * /subscribers/{deviceSubscriptionId}/subscriptions/{nodeSubscriptionId}/sync
     * 
     * @param syncRequest
     * @return
     */
    public RestSyncSetGetModel getSync(RestSyncNodeSubscriptionModel nodeSubscriptionModel, RestSyncSetRequestModel sync)
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, syncSetURL, this.subscriber, nodeSubscriptionModel.getId(), sync.getSyncId(),
                restWrapper.getParameters());

        RestSyncSetGetModel model = restWrapper.processModelWithoutEntryObject(RestSyncSetGetModel.class, request);
        restWrapper.initializeRequestSpecBuilder();
        return model;
    }

}
