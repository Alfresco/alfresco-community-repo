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
import java.util.Map;
import java.util.stream.Collectors;

import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.Rules;
import org.alfresco.rest.api.model.mapper.RestModelMapper;
import org.alfresco.rest.api.model.rules.CompositeCondition;
import org.alfresco.rest.api.model.rules.Rule;
import org.alfresco.rest.api.model.rules.RuleSet;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.api.model.rules.SimpleCondition;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.ListPage;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.service.Experimental;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.CompositeAction;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.util.collections.CollectionUtils;
import org.alfresco.service.namespace.NamespaceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Experimental
public class RulesImpl implements Rules
{
    private static final Logger LOGGER = LoggerFactory.getLogger(RulesImpl.class);
    private static final String MUST_HAVE_AT_LEAST_ONE_ACTION = "A rule must have at least one action";

    private RuleService ruleService;
    private NodeValidator validator;
    private RuleLoader ruleLoader;
    private ActionPermissionValidator actionPermissionValidator;
    private RestModelMapper<Rule, org.alfresco.service.cmr.rule.Rule> ruleMapper;

    @Override
    public CollectionWithPagingInfo<Rule> getRules(final String folderNodeId,
                                                   final String ruleSetId,
                                                   final List<String> includes,
                                                   final Paging paging)
    {
        final NodeRef folderNodeRef = validator.validateFolderNode(folderNodeId, false);
        NodeRef ruleSetNode = validator.validateRuleSetNode(ruleSetId, folderNodeRef);
        NodeRef owningFolder = ruleService.getOwningNodeRef(ruleSetNode);

        final List<Rule> rules = ruleService.getRules(owningFolder, false).stream()
                .map(ruleModel -> ruleLoader.loadRule(ruleModel, includes))
                .collect(Collectors.toList());

        return ListPage.of(rules, paging);
    }

    @Override
    public Rule getRuleById(final String folderNodeId, final String ruleSetId, final String ruleId, final List<String> includes)
    {
        final NodeRef folderNodeRef = validator.validateFolderNode(folderNodeId, false);
        final NodeRef ruleSetNodeRef = validator.validateRuleSetNode(ruleSetId, folderNodeRef);
        final NodeRef ruleNodeRef = validator.validateRuleNode(ruleId, ruleSetNodeRef);

        return ruleLoader.loadRule(ruleService.getRule(ruleNodeRef), includes);
    }

    @Override
    public List<Rule> createRules(final String folderNodeId, final String ruleSetId, final List<Rule> rules, final List<String> includes)
    {
        final NodeRef folderNodeRef = validator.validateFolderNode(folderNodeId, true);
        // Don't validate the ruleset node if -default- is passed since we may need to create it.
        if (RuleSet.isNotDefaultId(ruleSetId))
        {
            validator.validateRuleSetNode(ruleSetId, folderNodeRef);
        }

        return rules.stream()
                .map(this::mapToServiceModelAndValidateActions)
                .map(rule -> ruleService.saveRule(folderNodeRef, rule))
                .map(rule -> ruleLoader.loadRule(rule, includes))
                .collect(Collectors.toList());
    }

    @Override
    public Rule updateRuleById(String folderNodeId, String ruleSetId, String ruleId, Rule rule, List<String> includes)
    {
        LOGGER.debug("Updating rule in folder {}, rule set {}, rule {} to {}", folderNodeId, ruleSetId, ruleId, rule);

        NodeRef folderNodeRef = validator.validateFolderNode(folderNodeId, true);
        NodeRef ruleSetNodeRef = validator.validateRuleSetNode(ruleSetId, folderNodeRef);
        validator.validateRuleNode(ruleId, ruleSetNodeRef);

        return ruleLoader.loadRule(ruleService.saveRule(folderNodeRef, mapToServiceModelAndValidateActions(rule)), includes);
    }

    @Override
    public void deleteRuleById(String folderNodeId, String ruleSetId, String ruleId)
    {
        final NodeRef folderNodeRef = validator.validateFolderNode(folderNodeId, true);
        final NodeRef ruleSetNodeRef = validator.validateRuleSetNode(ruleSetId, folderNodeRef);
        final NodeRef ruleNodeRef = validator.validateRuleNode(ruleId, ruleSetNodeRef);
        final org.alfresco.service.cmr.rule.Rule rule = ruleService.getRule(ruleNodeRef);
        ruleService.removeRule(folderNodeRef, rule);
    }

    private org.alfresco.service.cmr.rule.Rule mapToServiceModelAndValidateActions(Rule rule)
    {
        if (CollectionUtils.isEmpty(rule.getActions()))
        {
            throw new InvalidArgumentException(MUST_HAVE_AT_LEAST_ONE_ACTION);
        }
        final org.alfresco.service.cmr.rule.Rule serviceModelRule = ruleMapper.toServiceModel(rule);

        return actionPermissionValidator.validateRulePermissions(serviceModelRule);
    }

    public void setRuleService(RuleService ruleService)
    {
        this.ruleService = ruleService;
    }

    public void setValidator(NodeValidator validator)
    {
        this.validator = validator;
    }

    public void setRuleLoader(RuleLoader ruleLoader)
    {
        this.ruleLoader = ruleLoader;
    }

    public void setActionPermissionValidator(ActionPermissionValidator actionPermissionValidator)
    {
        this.actionPermissionValidator = actionPermissionValidator;
    }

    public void setRuleMapper(
            RestModelMapper<Rule, org.alfresco.service.cmr.rule.Rule> ruleMapper)
    {
        this.ruleMapper = ruleMapper;
    }
}
