/*
 * Copyright 2022 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package org.alfresco.rest.requests;

import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.model.RestRuleModel;
import org.alfresco.rest.model.RestRuleModelsCollection;
import org.springframework.http.HttpMethod;

public class FolderRules extends ModelRequest<FolderRules>
{
    private static final String BASE_PATH = "nodes/{nodeId}/rule-sets/{ruleSetId}/rules";

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
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, BASE_PATH + "/{ruleId}", nodeId, ruleSetId, ruleId);
        return restWrapper.processModel(RestRuleModel.class, request);
    }
}
