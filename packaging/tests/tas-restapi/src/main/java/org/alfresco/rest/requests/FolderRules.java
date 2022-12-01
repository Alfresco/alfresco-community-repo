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
/*
 * Copyright 2022 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package org.alfresco.rest.requests;

import static org.alfresco.rest.core.JsonBodyGenerator.arrayToJson;

import java.util.List;

import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.model.RestRuleModel;
import org.alfresco.rest.model.RestRuleModelsCollection;
import org.springframework.http.HttpMethod;

public class FolderRules extends ModelRequest<FolderRules>
{
    private static final String BASE_PATH = "nodes/{nodeId}/rule-sets/{ruleSetId}/rules";
    private static final String RULE_ID_PATH = "/{ruleId}";

    private String nodeId;
    private String ruleSetId;

    public FolderRules withNodeId(String nodeId)
    {
        this.nodeId = nodeId;
        return this;
    }

    public FolderRules withRuleSetId(String ruleSetId)
    {
        this.ruleSetId = ruleSetId;
        return this;
    }

    public FolderRules(RestWrapper restWrapper)
    {
        super(restWrapper);
    }

    /**
     * Gets a list of rules for the folder node using GET call on "nodes/{nodeId}/rule-sets/{ruleSetId}/rules"
     * @return
     */
    public RestRuleModelsCollection getListOfRules()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, BASE_PATH, nodeId, ruleSetId);
        return restWrapper.processModels(RestRuleModelsCollection.class, request);
    }

    /**
     * Gets a single rule definition for the folder node using GET call on "nodes/{nodeId}/rule-sets/{ruleSetId}/rules/{ruleId}"
     * @return
     */
    public RestRuleModel getSingleRule(String ruleId)
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, BASE_PATH + RULE_ID_PATH, nodeId, ruleSetId, ruleId);
        return restWrapper.processModel(RestRuleModel.class, request);
    }

    /**
     * Create several rules.
     *
     * @param ruleModels The list of rules.
     * @return The same list of rules with some data populated by the repository.
     */
    public RestRuleModelsCollection createListOfRules(List<RestRuleModel> ruleModels)
    {
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, arrayToJson(ruleModels), BASE_PATH, nodeId, ruleSetId);
        return restWrapper.processModels(RestRuleModelsCollection.class, request);
    }

    /**
     * Create a single rule.
     *
     * @param ruleModel The rule to create.
     * @return The created rule with some data populated by the repository.
     */
    public RestRuleModel createSingleRule(RestRuleModel ruleModel)
    {
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, ruleModel.toJson(), BASE_PATH, nodeId, ruleSetId);
        return restWrapper.processModel(RestRuleModel.class, request);
    }

    /**
     * Update a rule.
     *
     * @param ruleId The id of the rule to update.
     * @param ruleModel The updated rule definition.
     * @return The updated rule with some data populated by the repository.
     */
    public RestRuleModel updateRule(String ruleId, RestRuleModel ruleModel)
    {
        RestRequest request = RestRequest.requestWithBody(HttpMethod.PUT, ruleModel.toJson(), BASE_PATH + RULE_ID_PATH, nodeId, ruleSetId, ruleId);
        return restWrapper.processModel(RestRuleModel.class, request);
    }

    /**
     * Deletes a rule definition for the folder node using DELETE call on "nodes/{nodeId}/rule-sets/{ruleSetId}/rules/{ruleId}"
     * @param ruleId The id of the rule to delete.
     * @return void
     */
    public void deleteRule(String ruleId)
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.DELETE, BASE_PATH + RULE_ID_PATH, nodeId, ruleSetId, ruleId);
        restWrapper.processEmptyModel(request);
    }
}
