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
