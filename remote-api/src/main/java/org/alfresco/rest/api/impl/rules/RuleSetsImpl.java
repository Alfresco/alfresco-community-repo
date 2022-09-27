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

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Objects;

import org.alfresco.repo.rule.RuleModel;
import org.alfresco.repo.rule.RuntimeRuleService;
import org.alfresco.rest.api.RuleSets;
import org.alfresco.rest.api.model.rules.RuleSet;
import org.alfresco.rest.api.model.rules.RuleSetLink;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.ListPage;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.service.Experimental;
import org.alfresco.service.cmr.repository.AspectMissingException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.rule.RuleService;

@Experimental
public class RuleSetsImpl implements RuleSets
{
    private RuleSetLoader ruleSetLoader;
    private RuleService ruleService;
    private NodeValidator validator;
    private NodeService nodeService;
    private RuntimeRuleService runtimeRuleService;

    @Override
    public CollectionWithPagingInfo<RuleSet> getRuleSets(String folderNodeId, List<String> includes, Paging paging)
    {
        NodeRef folderNode = validator.validateFolderNode(folderNodeId, false);

        List<RuleSet> ruleSets = ruleService.getNodesSupplyingRuleSets(folderNode)
                                            .stream()
                                            .map(ruleService::getRuleSetNode)
                                            .filter(Objects::nonNull)
                                            .map(nodeRef -> ruleSetLoader.loadRuleSet(nodeRef, folderNode, includes))
                                            .distinct()
                                            .collect(toList());

        return ListPage.of(ruleSets, paging);
    }

    @Override
    public RuleSet getRuleSetById(String folderNodeId, String ruleSetId, List<String> includes)
    {
        NodeRef folderNode = validator.validateFolderNode(folderNodeId, false);
        NodeRef ruleSetNode = validator.validateRuleSetNode(ruleSetId, folderNode);

        return ruleSetLoader.loadRuleSet(ruleSetNode, folderNode, includes);
    }

    @Override
    public RuleSetLink linkToRuleSet(String folderNodeId, String linkToNodeId)
    {

        final NodeRef folderNodeRef = validator.validateFolderNode(folderNodeId,true);
        final boolean isRuleSetNode = validator.isRuleSetNode(linkToNodeId);
        final NodeRef linkToNodeRef = isRuleSetNode
                ? validator.validateRuleSetNode(linkToNodeId, true)
                : validator.validateFolderNode(linkToNodeId, true);

        //The target node should have pre-existing rules to link to
        if (!ruleService.hasRules(linkToNodeRef)) {
            throw new InvalidArgumentException("The target node has no rules to link.");
        }

        //The folder shouldn't have any pre-existing rules
        if (ruleService.hasRules(folderNodeRef)) {
            throw new InvalidArgumentException("Unable to link to a rule set because the folder has pre-existing rules or is already linked to a rule set.");
        }

        // Create the destination folder as a secondary child of the first
        NodeRef ruleSetNodeRef = runtimeRuleService.getSavedRuleFolderAssoc(linkToNodeRef).getChildRef();
        // The required aspect will automatically be added to the node
        nodeService.addChild(folderNodeRef, ruleSetNodeRef, RuleModel.ASSOC_RULE_FOLDER, RuleModel.ASSOC_RULE_FOLDER);

        RuleSetLink ruleSetLink = new RuleSetLink();
        ruleSetLink.setId(ruleSetNodeRef.getId());

        return ruleSetLink;
    }

    @Override
    public void unlinkRuleSet(String folderNodeId, String ruleSetId)
    {
        final NodeRef folderNodeRef = validator.validateFolderNode(folderNodeId,true);
        final NodeRef ruleSetNodeRef = validator.validateRuleSetNode(ruleSetId, true);

        //The folder should be linked to a rule set
        if (!ruleService.isLinkedToRuleNode(ruleSetNodeRef))
        {
            throw new InvalidArgumentException("The folder is not linked to a rule set.");
        }

        // Check that the folder node has the rules aspect applied
        if (!nodeService.hasAspect(folderNodeRef,RuleModel.ASPECT_RULES))
        {
            throw new AspectMissingException(RuleModel.ASPECT_RULES, folderNodeRef);
        }

        //The following line also handles the deletion of the child folder that gets created during linking
        nodeService.removeAspect(folderNodeRef,RuleModel.ASPECT_RULES);
    }

    public void setRuleSetLoader(RuleSetLoader ruleSetLoader)
    {
        this.ruleSetLoader = ruleSetLoader;
    }

    public void setValidator(NodeValidator validator)
    {
        this.validator = validator;
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
}
