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

package org.alfresco.rest.api.impl;

import org.alfresco.model.ContentModel;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.Rules;
import org.alfresco.rest.api.model.Rule;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.resource.parameters.ListPages;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.namespace.QName;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RulesImpl implements Rules
{

    private Nodes nodes;
    private RuleService ruleService;

    @Override
    public CollectionWithPagingInfo<Rule> getRules(final String nodeId, final Paging paging)
    {
        final NodeRef nodeRef = nodes.validateNode(nodeId);

        final Set<QName> folders = new HashSet<>(List.of(ContentModel.TYPE_FOLDER));
        if (!nodes.nodeMatches(nodeRef, folders, null)) {
            throw new InvalidArgumentException("NodeId of a folder is expected");
        }

        final List<org.alfresco.service.cmr.rule.Rule> rulesModels = ruleService.getRules(nodeRef);
        final List<Rule> rules = new ArrayList<>(rulesModels.size());
        rulesModels.forEach(ruleModel -> rules.add(new Rule(ruleModel)));

        return ListPages.createPage(rules, paging);
    }

    public void setNodes(Nodes nodes)
    {
        this.nodes = nodes;
    }

    public void setRuleService(RuleService ruleService)
    {
        this.ruleService = ruleService;
    }
}
