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
package org.alfresco.rest.api.impl.rules;

import java.util.List;

import org.alfresco.rest.api.model.mapper.RestModelMapper;
import org.alfresco.rest.api.model.rules.CompositeCondition;
import org.alfresco.rest.api.model.rules.Rule;
import org.alfresco.rest.api.model.rules.SimpleCondition;
import org.alfresco.service.Experimental;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.RuleService;

/** Responsible for creating {@link Rule} objects. */
@Experimental
public class RuleLoader
{
    public static final String IS_SHARED = "isShared";
    private RuleService ruleService;
    private NodeValidator nodeValidator;
    private RestModelMapper<Rule, org.alfresco.service.cmr.rule.Rule> ruleMapper;

    public Rule loadRule(org.alfresco.service.cmr.rule.Rule ruleModel, List<String> includes)
    {
        final Rule rule = ruleMapper.toRestModel(ruleModel);
        if (includes != null && includes.contains(IS_SHARED))
        {
            NodeRef ruleSet = ruleService.getRuleSetNode(ruleModel.getNodeRef());
            boolean isShared = nodeValidator.isRuleSetNotNullAndShared(ruleSet);
            rule.setIsShared(isShared);
        }
        return rule;
    }

    public void setRuleService(RuleService ruleService)
    {
        this.ruleService = ruleService;
    }

    public void setNodeValidator(NodeValidator nodeValidator)
    {
        this.nodeValidator = nodeValidator;
    }

    public void setRuleMapper(
            RestModelMapper<Rule, org.alfresco.service.cmr.rule.Rule> ruleMapper)
    {
        this.ruleMapper = ruleMapper;
    }
}
