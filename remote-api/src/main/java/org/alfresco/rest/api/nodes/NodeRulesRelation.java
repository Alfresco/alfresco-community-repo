/*
 * #%L
 * Alfresco Remote API
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
 *  GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.rest.api.nodes;

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

import javax.servlet.http.HttpServletResponse;

/**
 * Folder node's rules.
 *
 */
@Experimental
@RelationshipResource(name = "rules", entityResource = NodeRuleSetsRelation.class, title = "Folder node rules")
public class NodeRulesRelation implements RelationshipResourceAction.Read<Rule>, RelationshipResourceAction.ReadById<Rule>, InitializingBean
{

    private Rules rules;

    @Override
    public void afterPropertiesSet() throws Exception
    {
        PropertyCheck.mandatory(this, "rules", this.rules);
    }

    /**
     * List folder rules for given folder node's and rule set's IDs as a page.
     *
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

        return rules.getRules(folderNodeId, ruleSetId, parameters.getPaging());
    }

    /**
     * Get single folder rule for given node's, rule set's and rule's IDs.
     *
     * - GET /nodes/{folderNodeId}/rule-sets/{ruleSetId}/rules/{ruleId}
     *
     * @param folderNodeId - entity resource context for this relationship
     * @param ruleSetId - rule set node ID (associated with folder node)
     * @param parameters - will never be null. Contains i.a. ruleId (relationship2Id)
     * @return {@link Rule} information
     * @throws RelationshipResourceNotFoundException in case resource was not found
     */
    @WebApiDescription(
        title="Get folder node rule",
        description = "Returns a folder single rule information for given node's, rule set's and rule's IDs",
        successStatus = HttpServletResponse.SC_OK
    )
    @Override
    public Rule readById(String folderNodeId, String ruleSetId, Parameters parameters) throws RelationshipResourceNotFoundException
    {
        final String ruleId = parameters.getRelationship2Id();

        return rules.getRuleById(folderNodeId, ruleSetId, ruleId);
    }

    public void setRules(Rules rules)
    {
        this.rules = rules;
    }
}
