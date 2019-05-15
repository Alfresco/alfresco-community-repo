package org.alfresco.rest.requests;

import org.alfresco.rest.core.JsonBodyGenerator;
import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.model.RestCustomModel;
import org.alfresco.utility.model.CustomContentModel;
import org.springframework.http.HttpMethod;

/**
 * @author Bogdan Bocancea
 */
public class CustomModelManager extends ModelRequest<CustomModelManager>
{
    private CustomContentModel customContentModel;

    public CustomModelManager(CustomContentModel customContentModel, RestWrapper restWrapper)
    {
        super(restWrapper);
        this.customContentModel = customContentModel;
    }

    /**
     * Retrieve one model using GET call on "cmm/{modelName}"
     * 
     * @return {@link RestCustomModel}
     */
    public RestCustomModel getModel()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "cmm/{modelName}?{parameters}", this.customContentModel.getName(),
                restWrapper.getParameters());
        return restWrapper.processModel(RestCustomModel.class, request);
    }
    
    public void activateModel()
    {
        String json = JsonBodyGenerator.keyValueJson("status", "ACTIVE");
        RestRequest request = RestRequest.requestWithBody(HttpMethod.PUT, json, "cmm/{modelName}?select=status", this.customContentModel.getName());
        restWrapper.processEmptyModel(request);
    }

    public void deactivateModel()
    {
        String json = JsonBodyGenerator.keyValueJson("status", "DRAFT");
        RestRequest request = RestRequest.requestWithBody(HttpMethod.PUT, json, "cmm/{modelName}?select=status", this.customContentModel.getName());
        restWrapper.processEmptyModel(request);
    }

    public void deleteModel()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.DELETE, "cmm/{modelName}", this.customContentModel.getName());
        restWrapper.processEmptyModel(request);
    }
}
