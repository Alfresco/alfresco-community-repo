package org.alfresco.rest.requests.discoveryAPI;

import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.model.RestDiscoveryModel;
import org.alfresco.rest.requests.ModelRequest;
import org.springframework.http.HttpMethod;

import io.restassured.RestAssured;

public class RestDiscoveryAPI extends ModelRequest<RestDiscoveryAPI> {

    public RestDiscoveryAPI(RestWrapper restWrapper)
    {
        super(restWrapper);
        RestAssured.basePath = "alfresco/api/discovery";
        restWrapper.configureRequestSpec().setBasePath(RestAssured.basePath);
    }

    // Method for getting repository info. The call is executed on root api path
    public RestDiscoveryModel getRepositoryInfo()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "");
        return restWrapper.processModel(RestDiscoveryModel.class, request);
    }

}

