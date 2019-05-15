package org.alfresco.rest.requests;

import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.core.RestResponse;
import org.alfresco.rest.core.RestWrapper;
import org.springframework.http.HttpMethod;

import io.restassured.RestAssured;

public class AdminConsole extends ModelRequest<AdminConsole> {

    public AdminConsole(RestWrapper restWrapper)
    {
        super(restWrapper);
        RestAssured.basePath = "alfresco/service/api/server";
        restWrapper.configureRequestSpec().setBasePath(RestAssured.basePath);
    }

    // Method for getting repository info from Admin Console
    public RestResponse getAdminConsoleRepoInfo()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "");
        return restWrapper.process(request);
    }
}
