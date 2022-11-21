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

import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;

import org.alfresco.rest.api.RuleSets;
import org.alfresco.rest.api.model.rules.RuleSetLink;
import org.alfresco.rest.api.nodes.NodesEntityResource;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.WebApiParam;
import org.alfresco.rest.framework.core.ResourceParameter;
import org.alfresco.rest.framework.core.exceptions.RelationshipResourceNotFoundException;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.util.PropertyCheck;
import org.springframework.beans.factory.InitializingBean;


@RelationshipResource(name = "rule-set-links", entityResource = NodesEntityResource.class, title = "Rule set links")
public class NodeRuleSetLinksRelation implements InitializingBean, RelationshipResourceAction.Create<RuleSetLink>,
                                                                   RelationshipResourceAction.Delete
{

    private final RuleSets ruleSets;

    @Override
    public void afterPropertiesSet() throws Exception
    {
        PropertyCheck.mandatory(this, "ruleSets", ruleSets);
    }

    @WebApiParam(name = "ruleSetLinkRequest", title = "Request body - rule set id",
            description = "Request body with rule set id", kind = ResourceParameter.KIND.HTTP_BODY_OBJECT)
    @WebApiDescription(title = "Link a rule set to a folder node",
            description = "Submits a request to link a rule set to folder",
            successStatus = HttpServletResponse.SC_CREATED)
    @Override
    public List<RuleSetLink> create(String nodeId, List<RuleSetLink> ruleSetLinksBody, Parameters parameters)
    {
        return ruleSetLinksBody.stream()
                .map(r -> ruleSets.linkToRuleSet(nodeId, r.getId()))
                .collect(Collectors.toList());
    }

    /**
     * Remove link between a rule set and a folder for given rule set's and folder's node IDs.
     * <p>
     * - DELETE /nodes/{folderNodeId}/rule-set-links/{ruleSetId}
     *
     * @param folderNodeId - folder node ID
     * @param ruleSetNodeId - rule set node ID (associated with folder node)
     * @throws RelationshipResourceNotFoundException in case resource was not found
     */
    @WebApiDescription(title = "Remove link between a rule set and a folder node",
            description = "Submits a request to unlink a rule set from a folder",
            successStatus = HttpServletResponse.SC_NO_CONTENT)
    @Override
    public void delete(String folderNodeId, String ruleSetNodeId, Parameters parameters)
    {
        ruleSets.unlinkRuleSet(folderNodeId, ruleSetNodeId);
    }

    public NodeRuleSetLinksRelation(RuleSets ruleSets)
    {
        this.ruleSets = ruleSets;
    }
}
