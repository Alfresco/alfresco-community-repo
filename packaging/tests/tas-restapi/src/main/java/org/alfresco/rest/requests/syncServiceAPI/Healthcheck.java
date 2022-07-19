package org.alfresco.rest.requests.syncServiceAPI;

import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.model.RestSyncServiceHealthCheckModel;
import org.alfresco.rest.requests.ModelRequest;
import org.alfresco.rest.requests.privateAPI.RestPrivateAPI;
import org.springframework.http.HttpMethod;

/**
 * Declares all Rest API under the /healthcheck path
 * 
 * @author Meenal Bhave
 */
public class Healthcheck extends ModelRequest<RestPrivateAPI>
{   
    public Healthcheck(RestWrapper restWrapper)
    {
        super(restWrapper);
        restWrapper.configureSyncServiceEndPoint();
        restWrapper.configureRequestSpec().setBasePath("alfresco/");
    }

    /**
     * Get Healthcheck using GET call on alfresco/healthcheck    
     * @return {@link RestSyncServiceHealthCheckModel}
     */
    public RestSyncServiceHealthCheckModel getHealthcheck()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "healthcheck?{parameters}", restWrapper.getParameters());
        return restWrapper.processModelWithoutEntryObject(RestSyncServiceHealthCheckModel.class, request);
    }

}
