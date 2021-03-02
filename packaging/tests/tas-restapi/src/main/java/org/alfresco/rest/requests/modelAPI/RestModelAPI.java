package org.alfresco.rest.requests.modelAPI;

import io.restassured.RestAssured;
import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.exception.JsonToModelConversionException;
import org.alfresco.rest.model.*;
import org.alfresco.rest.requests.ModelRequest;
import org.springframework.http.HttpMethod;

/**
 * Defines the entire Rest Model API
 * {@link https://api-explorer.alfresco.com/api-explorer/} select "Model API"
 */
public class RestModelAPI extends ModelRequest<RestModelAPI>
{
    public RestModelAPI(RestWrapper restWrapper)
    {
        super(restWrapper);
        RestAssured.basePath = "alfresco/api/-default-/public/alfresco/versions/1";
        restWrapper.configureRequestSpec().setBasePath(RestAssured.basePath);
    }

    /**
     * Retrieve all aspects using GET call on "aspects"
     *
     * @return RestAspectsCollection
     * @throws JsonToModelConversionException
     */
    public RestAspectsCollection getAspects()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "aspects?{parameters}", restWrapper.getParameters());
        return restWrapper.processModels(RestAspectsCollection.class, request);
    }

    /**
     * Retrieve aspect by id using GET call on "aspects/{aspectId}"
     *
     * @return RestAspectModel
     * @throws JsonToModelConversionException
     */
    public RestAspectModel getAspect(String aspectId)
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "aspects/{aspectId}?{parameters}", aspectId, restWrapper.getParameters());
        return restWrapper.processModel(RestAspectModel.class, request);
    }

    /**
     * Retrieve all types using GET call on "types"
     *
     * @return RestTypesCollection
     * @throws JsonToModelConversionException
     */
    public RestTypesCollection getTypes()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "types?{parameters}", restWrapper.getParameters());
        return restWrapper.processModels(RestTypesCollection.class, request);
    }

    /**
     * Retrieve Type by id using GET call on "types/{typeId}"
     *
     * @return RestTypeModel
     * @throws JsonToModelConversionException
     */
    public RestTypeModel getType(String typeId)
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "types/{typeId}?{parameters}", typeId, restWrapper.getParameters());
        return restWrapper.processModel(RestTypeModel.class, request);
    }
}
