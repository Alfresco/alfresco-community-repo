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

package org.alfresco.rest.api.rules;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.rest.api.RuleSets;
import org.alfresco.rest.api.model.rules.RuleSet;
import org.alfresco.rest.api.nodes.NodesEntityResource;
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
 * Folder node rule sets.
 */
@Experimental
@RelationshipResource(name = "rule-sets", entityResource = NodesEntityResource.class, title = "Folder node rule sets")
public class NodeRuleSetsRelation implements RelationshipResourceAction.Read<RuleSet>,
                                             RelationshipResourceAction.ReadById<RuleSet>,
                                             RelationshipResourceAction.Update<RuleSet>,
                                             InitializingBean
{
    private RuleSets ruleSets;

    @Override
    public void afterPropertiesSet() throws Exception
    {
        PropertyCheck.mandatory(this, "ruleSets", ruleSets);
    }

    /**
     * List rule sets for given folder.
     * <p>
     * - GET /nodes/{folderNodeId}/rule-sets
     *
     * @param folderNodeId The id of the folder node.
     * @param parameters Contains paging information and information about which fields to include
     * @return {@link CollectionWithPagingInfo} containing a page of rule sets
     */
    @WebApiDescription (
            title = "Get rule sets for a folder",
            description = "Returns a paged list of rule sets for given node",
            successStatus = HttpServletResponse.SC_OK
    )
    @Override
    public CollectionWithPagingInfo<RuleSet> readAll(String folderNodeId, Parameters parameters)
    {
        return ruleSets.getRuleSets(folderNodeId, parameters.getInclude(), parameters.getPaging());
    }

    /**
     * Get single folder rule for given node's, rule set's and rule's IDs.
     * <p>
     * - GET /nodes/{folderNodeId}/rule-sets/{ruleSetId}
     *
     * @param folderNodeId - entity resource context for this relationship
     * @param ruleSetId - rule set node ID (associated with folder node)
     * @param parameters Contains information about which fields to include
     * @return {@link RuleSet} definition
     * @throws RelationshipResourceNotFoundException in case resource was not found
     */
    @WebApiDescription (
            title = "Get rule set",
            description = "Returns a single rule set for the given node",
            successStatus = HttpServletResponse.SC_OK
    )
    @Override
    public RuleSet readById(String folderNodeId, String ruleSetId, Parameters parameters) throws RelationshipResourceNotFoundException
    {
        return ruleSets.getRuleSetById(folderNodeId, ruleSetId, parameters.getInclude());
    }

    public void setRuleSets(RuleSets ruleSets)
    {
        this.ruleSets = ruleSets;
    }

    /**
     * Update a rule set, in particular this is useful for reordering rules in a rule set.
     * <p>
     * - PUT /nodes/{folderNodeId}/rule-sets/{ruleSetId}
     *
     * @param folderNodeId The id for the folder.
     * @param ruleSet The updated rule set.
     * @param parameters Contains information about which fields to include in the response.
     * @return The updated rule set.
     */
    @Override
    public RuleSet update(String folderNodeId, RuleSet ruleSet, Parameters parameters)
    {
        return ruleSets.updateRuleSet(folderNodeId, ruleSet, parameters.getInclude());
    }
}
