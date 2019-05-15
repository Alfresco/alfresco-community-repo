package org.alfresco.rest.requests;

import javax.json.JsonArrayBuilder;

import org.alfresco.rest.core.JsonBodyGenerator;
import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.model.RestCustomAspectModel;
import org.alfresco.utility.model.CustomAspectModel;
import org.alfresco.utility.model.CustomAspectPropertiesModel;
import org.alfresco.utility.model.CustomContentModel;
import org.springframework.http.HttpMethod;

/**
 * @author Bogdan Bocancea
 */
public class CustomAspectModelManager extends ModelRequest<CustomAspectModelManager>
{
    private CustomContentModel customContentModel;
    private CustomAspectModel customAspectModel;
    
    public CustomAspectModelManager(CustomContentModel customContentModel, CustomAspectModel aspectModel, RestWrapper restWrapper)
    {
        super(restWrapper);
        this.customContentModel = customContentModel;
        this.customAspectModel = aspectModel;
    }

    public RestCustomAspectModel getAspect()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "cmm/{modelName}/aspects/{aspectName}?{parameters}",
                this.customContentModel.getName(), this.customAspectModel.getName(), restWrapper.getParameters());
        return restWrapper.processModel(RestCustomAspectModel.class, request);
    }
    
    public void addProperty(CustomAspectPropertiesModel propertyModel)
    {
        JsonArrayBuilder array = JsonBodyGenerator.defineJSONArray();
        array.add(JsonBodyGenerator.defineJSON()
                .add("name", propertyModel.getName())
                .add("title", propertyModel.getTitle())
                .add("description", propertyModel.getDescription())
                .add("dataType", propertyModel.getDataType())
                .add("multiValued", propertyModel.isMultiValued())
                .add("mandatory", propertyModel.isMandatory())
                .add("mandatoryEnforced", propertyModel.isMandatoryEnforced()));
        
        String body = JsonBodyGenerator.defineJSON().add("name", this.customAspectModel.getName()).add("properties", array).build().toString();
        RestRequest request = RestRequest.requestWithBody(HttpMethod.PUT, body, "cmm/{modelName}/aspects/{aspectName}?select=props", 
                this.customContentModel.getName(), this.customAspectModel.getName());
        restWrapper.processEmptyModel(request);
    }
    
    public void deleteAspectProperty(CustomAspectPropertiesModel propertyModel)
    {
        String body = JsonBodyGenerator.defineJSON()
                        .add("name", this.customAspectModel.getName()).build().toString();
        RestRequest request = RestRequest.requestWithBody(HttpMethod.PUT, body, "cmm/{modelName}/aspects/{aspectName}?select=props&delete={propertyName}", 
                this.customContentModel.getName(), this.customAspectModel.getName(), propertyModel.getName());
        restWrapper.processEmptyModel(request);
    }
    
    public void deleteAspect()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.DELETE, "cmm/{modelName}/aspects/{aspectName}",
                this.customContentModel.getName(), this.customAspectModel.getName());
        restWrapper.processEmptyModel(request);
    }
}
