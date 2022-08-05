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
import java.util.stream.Collectors;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.rule.RuleModel;
import org.alfresco.repo.rule.RuntimeRuleService;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.Rules;
import org.alfresco.rest.api.model.rules.Rule;
import org.alfresco.rest.api.model.rules.RuleSet;
import org.alfresco.rest.api.model.rules.RuleSetLink;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.ListPage;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.service.Experimental;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.rule.RuleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Experimental
public class RulesImpl implements Rules
{
    private static final Logger LOGGER = LoggerFactory.getLogger(RulesImpl.class);

    private Nodes nodes;
    private NodeService nodeService;
    private RuleService ruleService;
    private NodeValidator validator;
    private RuntimeRuleService runtimeRuleService;

    @Override
    public CollectionWithPagingInfo<Rule> getRules(final String folderNodeId, final String ruleSetId, final Paging paging)
    {
        final NodeRef folderNodeRef = validator.validateFolderNode(folderNodeId, false);
        final NodeRef ruleSetNodeRef = validator.validateRuleSetNode(ruleSetId, folderNodeRef);

        final boolean isShared = validator.isRuleSetNotNullAndShared(ruleSetNodeRef);
        final List<Rule> rules = ruleService.getRules(folderNodeRef).stream()
            .map(ruleModel -> Rule.from(ruleModel, isShared))
            .collect(Collectors.toList());

        return ListPage.of(rules, paging);
    }

    @Override
    public Rule getRuleById(final String folderNodeId, final String ruleSetId, final String ruleId)
    {
        final NodeRef folderNodeRef = validator.validateFolderNode(folderNodeId, false);
        final NodeRef ruleSetNodeRef = validator.validateRuleSetNode(ruleSetId, folderNodeRef);
        final NodeRef ruleNodeRef = validator.validateRuleNode(ruleId, ruleSetNodeRef);

        return Rule.from(ruleService.getRule(ruleNodeRef), validator.isRuleSetNotNullAndShared(ruleSetNodeRef));
    }

    @Override
    public List<Rule> createRules(final String folderNodeId, final String ruleSetId, final List<Rule> rules)
    {
        final NodeRef folderNodeRef = validator.validateFolderNode(folderNodeId, true);
        // Don't validate the ruleset node if -default- is passed since we may need to create it.
        final NodeRef ruleSetNodeRef = (RuleSet.isNotDefaultId(ruleSetId)) ? validator.validateRuleSetNode(ruleSetId, folderNodeRef) : null;

        return rules.stream()
                    .map(rule -> rule.toServiceModel(nodes))
                    .map(rule -> ruleService.saveRule(folderNodeRef, rule))
                    .map(rule -> Rule.from(rule, validator.isRuleSetNotNullAndShared(ruleSetNodeRef, folderNodeRef)))
                    .collect(Collectors.toList());
    }

    @Override
    public Rule updateRuleById(String folderNodeId, String ruleSetId, String ruleId, Rule rule)
    {
        LOGGER.debug("Updating rule in folder {}, rule set {}, rule {} to {}", folderNodeId, ruleSetId, ruleId, rule);

        NodeRef folderNodeRef = validator.validateFolderNode(folderNodeId, true);
        NodeRef ruleSetNodeRef = validator.validateRuleSetNode(ruleSetId, folderNodeRef);
        validator.validateRuleNode(ruleId, ruleSetNodeRef);

        boolean shared = validator.isRuleSetNotNullAndShared(ruleSetNodeRef, folderNodeRef);
        return Rule.from(ruleService.saveRule(folderNodeRef, rule.toServiceModel(nodes)), shared);
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

    @Override
    public RuleSetLink linkToRuleSet(String folderNodeId, String linkToNodeId) {

        RuleSetLink ruleSetLink = new RuleSetLink();
        final NodeRef folderNodeRef = validator.validateFolderNode(folderNodeId,false);
        final NodeRef linkToNodeRef = validator.validateFolderNode(linkToNodeId, false);

        if(nodeService.exists(folderNodeRef))
        {
            //The target node should have pre-existing rules to link to
            if(!ruleService.hasRules(linkToNodeRef))
            {
                throw new AlfrescoRuntimeException("The target node has no rules to link.");
            }

            //The folder shouldn't have any pre-existing rules
            if((!ruleService.hasRules(folderNodeRef)))
            {
                // Create the destination folder as a secondary child of the first
                NodeRef ruleSetNodeRef = runtimeRuleService.getSavedRuleFolderAssoc(linkToNodeRef).getChildRef();
                // The required aspect will automatically be added to the node
                nodeService.addChild(folderNodeRef, ruleSetNodeRef, RuleModel.ASSOC_RULE_FOLDER, RuleModel.ASSOC_RULE_FOLDER);
                ruleSetLink.setId(ruleSetNodeRef.getId());
            }
            else
            {
                throw new AlfrescoRuntimeException("Unable to link to a ruleset because the folder has pre-existing rules.");
            }

        }
        return ruleSetLink;
    }

    public void setNodes(Nodes nodes)
    {
        this.nodes = nodes;
    }

    public void setRuleService(RuleService ruleService)
    {
        this.ruleService = ruleService;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setRuntimeRuleService(RuntimeRuleService runtimeRuleService)
    {
        this.runtimeRuleService = runtimeRuleService;
    }

    public void setValidator(NodeValidator validator)
    {
        this.validator = validator;
    }
}
