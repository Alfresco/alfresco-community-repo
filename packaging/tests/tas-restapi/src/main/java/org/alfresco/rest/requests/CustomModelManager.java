package org.alfresco.rest.requests;

import org.alfresco.rest.core.JsonBodyGenerator;
import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.model.RestCustomAspectModel;
import org.alfresco.rest.model.RestCustomModel;
import org.alfresco.rest.model.RestCustomTypeModel;
import org.alfresco.rest.model.RestGroupsModelsCollection;
import org.alfresco.utility.model.CustomAspectModel;
import org.alfresco.utility.model.CustomAspectPropertiesModel;
import org.alfresco.utility.model.CustomContentModel;
import org.springframework.http.HttpMethod;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;

/**
 * @author Bogdan Bocancea
 */
public class CustomModelManager extends ModelRequest<CustomModelManager>
{
    private CustomContentModel customContentModel;

    public CustomModelManager(RestWrapper restWrapper)
    {
        super(restWrapper);
    }

    public CustomModelManager(CustomContentModel customContentModel, RestWrapper restWrapper)
    {
        super(restWrapper);
        this.customContentModel = customContentModel;
    }

    /**
     * Create a new custom model
     *
     * @param customContentModel
     * @return {@link RestCustomModel}
     */
    public RestCustomModel createCustomModel(CustomContentModel customContentModel)
    {
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, customContentModel.toJson(),
            "cmm?{parameters}", restWrapper.getParameters());
        return restWrapper.processModel(RestCustomModel.class, request);
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

    public RestCustomAspectModel createAspect(CustomAspectModel aspectModel)
    {
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, aspectModel.toJson(),
            "cmm/{modelName}/aspects?{parameters}", this.customContentModel.getName(), restWrapper.getParameters());
        return restWrapper.processModel(RestCustomAspectModel.class, request);
    }

    public RestCustomTypeModel createCustomType(RestCustomTypeModel customType)
    {
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, customType.toJson(),
                "cmm/{modelName}/types?{parameters}", this.customContentModel.getName(), restWrapper.getParameters());
        return restWrapper.processModel(RestCustomTypeModel.class, request);
    }
}
