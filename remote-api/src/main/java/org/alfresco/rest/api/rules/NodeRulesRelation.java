/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
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
 *  GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.rest.api.rules;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

import org.alfresco.rest.api.Rules;
import org.alfresco.rest.api.model.rules.Rule;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.core.exceptions.RelationshipResourceNotFoundException;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.service.Experimental;
import org.alfresco.util.PropertyCheck;
import org.springframework.beans.factory.InitializingBean;

/**
 * Folder node's rules.
 *
 */
@Experimental
@RelationshipResource(name = "rules", entityResource = NodeRuleSetsRelation.class, title = "Folder node rules")
public class NodeRulesRelation implements RelationshipResourceAction.Read<Rule>,
                                          RelationshipResourceAction.ReadById<Rule>,
                                          RelationshipResourceAction.Create<Rule>,
                                          RelationshipResourceAction.Update<Rule>,
                                          RelationshipResourceAction.Delete,
                                          InitializingBean
{

    private Rules rules;

    @Override
    public void afterPropertiesSet() throws Exception
    {
        PropertyCheck.mandatory(this, "rules", this.rules);
    }

    /**
     * List folder rules for given folder node's and rule set's IDs as a page.
     * <p>
     * - GET /nodes/{folderNodeId}/rule-sets/{ruleSetId}/rules
     *
     * @param folderNodeId - entity resource context for this relationship
     * @param parameters - will never be null. Contains i.a. paging information and ruleSetId (relationshipId)
     * @return {@link CollectionWithPagingInfo} containing a page of folder rules
     */
    @WebApiDescription(
        title = "Get folder node rules",
        description = "Returns a paged list of folder rules for given node's and rule set's IDs",
        successStatus = HttpServletResponse.SC_OK
    )
    @Override
    public CollectionWithPagingInfo<Rule> readAll(String folderNodeId, Parameters parameters)
    {
        final String ruleSetId = parameters.getRelationshipId();

        return rules.getRules(folderNodeId, ruleSetId, parameters.getInclude(), parameters.getPaging());
    }

    /**
     * Get single folder rule for given node's, rule set's and rule's IDs.
     * <p>
     * - GET /nodes/{folderNodeId}/rule-sets/{ruleSetId}/rules/{ruleId}
     *
     * @param folderNodeId - entity resource context for this relationship
     * @param ruleSetId - rule set node ID (associated with folder node)
     * @param parameters - will never be null. Contains i.a. ruleId (relationship2Id)
     * @return {@link Rule} definition
     * @throws RelationshipResourceNotFoundException in case resource was not found
     */
    @WebApiDescription(
        title="Get folder node rule",
        description = "Returns a folder single rule definition for given node's, rule set's and rule's IDs",
        successStatus = HttpServletResponse.SC_OK
    )
    @Override
    public Rule readById(String folderNodeId, String ruleSetId, Parameters parameters) throws RelationshipResourceNotFoundException
    {
        final String ruleId = parameters.getRelationship2Id();

        return rules.getRuleById(folderNodeId, ruleSetId, ruleId, parameters.getInclude());
    }

    /**
     * Create one or more rules inside a given folder and rule set.
     * <p>
     * POST /nodes/{folderNodeId}/rule-sets/{ruleSetId}/rules
     *
     * @param folderNodeId The folder in which to create the rule.
     * @param ruleList The list of rules to create.
     * @param parameters List of parameters including the rule set id as the relationship.
     * @return The newly created rules.
     */
    @WebApiDescription(
            title = "Create folder rule",
            description = "Creates one or more folder rules for the given folder and rule set",
            successStatus = HttpServletResponse.SC_CREATED
    )
    @Override
    public List<Rule> create(String folderNodeId, List<Rule> ruleList, Parameters parameters)
    {
        final String ruleSetId = parameters.getRelationshipId();

        return rules.createRules(folderNodeId, ruleSetId, ruleList, parameters.getInclude());
    }

    /**
     * Update the specified folder rule.
     * <p>
     * PUT /nodes/{folderNodeId}/rule-sets/{ruleSetId}/rules/{ruleId}
     *
     * @param folderNodeId The id of the folder containing the rule.
     * @param rule The updated rule.
     * @param parameters List of parameters including the rule set id and rule id.
     * @return The updated rule.
     * @throws RelationshipResourceNotFoundException in case resource was not found
     */
    @WebApiDescription (
            title = "Update folder node rule",
            description = "Update a single rule definition for given node's, rule set's and rule's IDs",
            successStatus = HttpServletResponse.SC_OK
    )
    @Override
    public Rule update(String folderNodeId, Rule rule, Parameters parameters)
    {
        String ruleSetId = parameters.getRelationshipId();
        String ruleId = parameters.getRelationship2Id();
        return rules.updateRuleById(folderNodeId, ruleSetId, ruleId, rule, parameters.getInclude());
    }

    /**
     * Delete single folder rule for given node's, rule set's and rule's IDs.
     * <p>
     * - DELETE /nodes/{folderNodeId}/rule-sets/{ruleSetId}/rules/{ruleId}
     *
     * @param folderNodeId - entity resource context for this relationship
     * @param ruleSetId - rule set node ID (associated with folder node)
     * @param parameters - Should not be null. Should contain at least ruleId (relationship2Id)
     * @throws RelationshipResourceNotFoundException in case resource was not found
     */
    @WebApiDescription(
            title="Delete folder node rule",
            description = "Deletes a single rule definition for given node's, rule set's and rule's IDs",
            successStatus = HttpServletResponse.SC_NO_CONTENT
    )
    @Override
    public void delete(String folderNodeId, String ruleSetId, Parameters parameters)
    {
        final String ruleId = parameters.getRelationship2Id();
        rules.deleteRuleById(folderNodeId, ruleSetId, ruleId);
    }

    public void setRules(Rules rules)
    {
        this.rules = rules;
    }
}
