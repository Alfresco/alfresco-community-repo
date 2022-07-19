package org.alfresco.rest.requests;

import java.util.Map;

import org.alfresco.rest.core.JsonBodyGenerator;
import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.model.RestActionDefinitionModel;
import org.alfresco.rest.model.RestActionDefinitionModelsCollection;
import org.alfresco.utility.model.RepoTestModel;
import org.json.JSONObject;
import org.springframework.http.HttpMethod;

public class Actions extends ModelRequest<Actions>
{
    public Actions(RestWrapper restWrapper)
    {
        super(restWrapper);
    }

    /**
     * List available actions using GET on '/action-definitions'
     */
    public RestActionDefinitionModelsCollection listActionDefinitions()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "action-definitions?{parameters}", restWrapper.getParameters());
        return restWrapper.processModels(RestActionDefinitionModelsCollection.class, request);
    }

    /**
     * Execute action with parameters using POST on '/action-executions'
     */
    public JSONObject executeAction(String actionDefinitionId, RepoTestModel targetNode, Map<String, String> params)
    {
        String postBody = JsonBodyGenerator.executeActionPostBody(actionDefinitionId, targetNode, params);
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, postBody, "action-executions");
        return restWrapper.processJson(request);
    }

    /**
     * Execute action without parameters using POST on '/action-executions'
     */
    public JSONObject executeAction(String actionDefinitionId, RepoTestModel targetNode)
    {
        String postBody = JsonBodyGenerator.executeActionPostBody(actionDefinitionId, targetNode);
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, postBody, "action-executions");
        return restWrapper.processJson(request);
    }

    /**
     * Get specific action definition using GET on '/action-definitions/{actionDefinitionId}'
     */
    public RestActionDefinitionModel getActionDefinitionById(String actionDefinitionId)
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "action-definitions/".concat(actionDefinitionId));
        return restWrapper.processModel(RestActionDefinitionModel.class, request);
    }
}
