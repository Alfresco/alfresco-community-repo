package org.alfresco.rest.requests.cmisAPI;

import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.core.RestResponse;
import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.requests.ModelRequest;
import org.alfresco.utility.model.ContentModel;
import org.springframework.http.HttpMethod;

import io.restassured.RestAssured;

/**
 * @author Paul Brodner
 * 
 * Perform CMIS rest API calls "alfresco/api/-default-/public/cmis/versions/1.1/browser"
 * 
 * Take a look at {@link org.alfresco.rest.cmis.CmisBrowserTest}
 * @since 5.2.0-8
 */
public class RestCmisAPI extends ModelRequest<RestCmisAPI> {

    public RestCmisAPI(RestWrapper restWrapper)
    {
        super(restWrapper);
        RestAssured.basePath = "alfresco/api/-default-/public/cmis/versions/1.1/browser";
        restWrapper.configureRequestSpec().setBasePath(RestAssured.basePath);
    }

    /**
     * @param objectCmisLocation
     * @return object
     */
    public RestResponse getRootObjectByLocation(ContentModel contentModel)
    {
        return getRootObjectByLocation(contentModel.getName());
    }

    /**
     * @param objectCmisLocation
     * @return object
     */
    public RestResponse getRootObjectByLocation(String objectName)
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "root/Shared/{objectName}?{parameters}", objectName, restWrapper.getParameters());
        return restWrapper.process(request);
    }

    /**
     * @param objectId
     * @return object
     */
    public RestResponse getRootObjectByID(ContentModel contentModel)
    {
        return getRootObjectByID(contentModel.getNodeRef());
    }

    /**
     * @param objectId
     * @return object
     */
    public RestResponse getRootObjectByID(String objectID)
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "root?objectid={objectId}?{parameters}", objectID, restWrapper.getParameters());
        return restWrapper.process(request);
    }

}
