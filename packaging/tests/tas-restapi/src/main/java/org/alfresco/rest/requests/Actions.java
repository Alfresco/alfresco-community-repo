/*-
 * #%L
 * alfresco-tas-restapi
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.rest.requests;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.rest.core.JsonBodyGenerator;
import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.model.RestActionConstraintModel;
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
    public JSONObject executeAction(String actionDefinitionId, RepoTestModel targetNode, Map<String, Serializable> params)
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

    /**
     * Get specific action constraint using GET on '/action-parameter-constraints/{actionConstraintName}'
     */
    public RestActionConstraintModel getActionConstraintByName(String actionConstraintName)
    {
        final RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "action-parameter-constraints/".concat(actionConstraintName));
        return restWrapper.processModel(RestActionConstraintModel.class, request);
    }
}
