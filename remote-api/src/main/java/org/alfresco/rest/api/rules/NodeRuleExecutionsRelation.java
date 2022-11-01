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

import org.alfresco.rest.api.Rules;
import org.alfresco.rest.api.model.rules.RuleExecution;
import org.alfresco.rest.api.nodes.NodesEntityResource;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.service.Experimental;
import org.alfresco.util.PropertyCheck;
import org.springframework.beans.factory.InitializingBean;

@Experimental
@RelationshipResource(name = "rule-executions", entityResource = NodesEntityResource.class, title = "Executing rules")
public class NodeRuleExecutionsRelation implements RelationshipResourceAction.Create<RuleExecution>, InitializingBean
{
    private final Rules rules;

    public NodeRuleExecutionsRelation(Rules rules)
    {
        this.rules = rules;
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        PropertyCheck.mandatory(this, "rules", this.rules);
    }

    /**
     * Execute rules for given folder node.
     *
     * @param folderNodeId - the ID of a folder
     * @param ruleExecutionParameters - rule execution parameters
     * @param parameters - additional request parameters
     * @return execution details
     */
    @Override
    public List<RuleExecution> create(String folderNodeId, List<RuleExecution> ruleExecutionParameters, Parameters parameters)
    {
        final RuleExecution ruleExecution = ruleExecutionParameters.stream().findFirst().orElse(new RuleExecution());
        return List.of(rules.executeRules(folderNodeId, ruleExecution.getIsEachSubFolderIncluded()));
    }
}
