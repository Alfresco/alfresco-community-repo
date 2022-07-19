package org.alfresco.rest.requests;

import org.alfresco.rest.core.JsonBodyGenerator;
import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.exception.JsonToModelConversionException;
import org.alfresco.rest.model.RestTagModel;
import org.alfresco.rest.model.RestTagModelsCollection;
import org.springframework.http.HttpMethod;

public class Tags extends ModelRequest<Tags>
{
    RestTagModel tag;

    public Tags(RestTagModel tag, RestWrapper restWrapper)
    {
        super(restWrapper);
        this.tag = tag;
    }

    /**
     * Retrieves 100 tags (this is the default size when maxItems is not specified) from Alfresco using GET call on "/tags"
     * 
     * @return
     * @throws JsonToModelConversionException
     */
    public RestTagModelsCollection getTags()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "tags?{parameters}", restWrapper.getParameters());
        return restWrapper.processModels(RestTagModelsCollection.class, request);
    }

    /**
     * Retrieves a tag with ID using GET call on using GET call on "/tags/{tagId}"
     * 
     * @param tag
     * @return
     */
    public RestTagModel getTag()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "tags/{tagId}?{parameters}", tag.getId(), restWrapper.getParameters());
        return restWrapper.processModel(RestTagModel.class, request);
    }

    /**
     * Update a tag using PUT call on tags/{tagId}
     *
     * @param tag
     * @param newTag
     * @return
     * @throws JsonToModelConversionException
     */
    public RestTagModel update(String newTag)
    {
        String postBody = JsonBodyGenerator.keyValueJson("tag", newTag);
        RestRequest request = RestRequest.requestWithBody(HttpMethod.PUT, postBody, "tags/{tagId}", tag.getId());

        return restWrapper.processModel(RestTagModel.class, request);
    }
}