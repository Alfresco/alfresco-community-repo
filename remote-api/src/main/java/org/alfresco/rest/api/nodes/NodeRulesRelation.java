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
import org.alfresco.rest.api.model.Rule;
import org.alfresco.rest.framework.WebApiDescription;
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
public class NodeRulesRelation implements RelationshipResourceAction.Read<Rule>, InitializingBean
{

    private Rules rules;

    @Override
    public void afterPropertiesSet() throws Exception
    {
        PropertyCheck.mandatory(this, "rules", this.rules);
    }

    /**
     * List folder node rules for given node's and rule set's IDs as a page.
     *
     * - GET /nodes/{folderNodeId}/rulesets/{ruleSetId}/rules
     *
     * @param folderNodeId - entity resource context for this relationship
     * @param parameters - will never be null. Contains i.a. paging information and ruleSetId (relationshipId)
     * @return a paged list of folder rules
     */
    @WebApiDescription(
        title = "Get folder node rules",
        description = "Returns a paged list of folder rules for given node's and rule set's ID",
        successStatus = HttpServletResponse.SC_OK
    )
    @Override
    public CollectionWithPagingInfo<Rule> readAll(String folderNodeId, Parameters parameters)
    {
        final String ruleSetId = parameters.getRelationshipId();

        return rules.getRules(folderNodeId, ruleSetId, parameters.getPaging());
    }

    public void setRules(Rules rules)
    {
        this.rules = rules;
    }
}
