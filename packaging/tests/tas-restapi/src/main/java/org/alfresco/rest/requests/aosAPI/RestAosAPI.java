package org.alfresco.rest.requests.aosAPI;

import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.requests.ModelRequest;
import org.alfresco.rest.requests.cmisAPI.RestCmisAPI;

import io.restassured.RestAssured;

/**
 * @author Catalin Gornea
 * 
 * Perform AOS rest API calls "alfresco/aos"
 * 
 * Take a look at {@link org.alfresco.rest.aos.AosApiTest}
 * @since 5.2.0-8
 */

public class RestAosAPI extends ModelRequest<RestCmisAPI>
{

    public RestAosAPI(RestWrapper restWrapper)
    {
        super(restWrapper);
        RestAssured.basePath = "alfresco/aos";
        restWrapper.configureRequestSpec().setBasePath(RestAssured.basePath);
    }
}
