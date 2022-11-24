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

import static org.alfresco.service.cmr.repository.StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;
import static org.alfresco.util.collections.CollectionUtils.isEmpty;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.IntStream;

import org.alfresco.repo.rule.RuleModel;
import org.alfresco.repo.rule.RuntimeRuleService;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.rest.api.RuleSets;
import org.alfresco.rest.api.model.rules.RuleSet;
import org.alfresco.rest.api.model.rules.RuleSetLink;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
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
public class RuleSetsImpl implements RuleSets
{
    private static final Logger LOGGER = LoggerFactory.getLogger(RuleSetsImpl.class);

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
                                            .map(supplyingNode -> loadRuleSet(supplyingNode, folderNode, includes))
                                            .filter(Objects::nonNull)
                                            .distinct()
                                            .collect(toList());

        return ListPage.of(ruleSets, paging);
    }

    /**
     * Load the specified rule set if the user has permission.
     *
     * @param supplyingNode The folder supplying a rule set.
     * @param folderNode The folder being supplied with rule sets.
     * @param includes The list of optional fields to include for each rule set in the response.
     * @return The rule set from the DB or null if the folder has no rule set, or the current user does not have permission to view it.
     */
    private RuleSet loadRuleSet(NodeRef supplyingNode, NodeRef folderNode, List<String> includes)
    {
        NodeRef ruleSetNode = ruleService.getRuleSetNode(supplyingNode);
        // Check if the folder has no rule sets.
        if (ruleSetNode == null)
        {
            return null;
        }

        try
        {
            return ruleSetLoader.loadRuleSet(ruleSetNode, folderNode, includes);
        }
        catch (AccessDeniedException e)
        {
            LOGGER.debug("User does not have permission to view rule set with id {}.", ruleSetNode, e);
            return null;
        }
    }

    @Override
    public RuleSet getRuleSetById(String folderNodeId, String ruleSetId, List<String> includes)
    {
        NodeRef folderNode = validator.validateFolderNode(folderNodeId, false);
        NodeRef ruleSetNode = validator.validateRuleSetNode(ruleSetId, folderNode);

        return ruleSetLoader.loadRuleSet(ruleSetNode, folderNode, includes);
    }

    @Override
    public RuleSet updateRuleSet(String folderNodeId, RuleSet ruleSet, List<String> includes)
    {
        // Editing the order of the rules doesn't require permission to edit the rule set itself.
        NodeRef folderNode = validator.validateFolderNode(folderNodeId, false);
        NodeRef ruleSetNode = validator.validateRuleSetNode(ruleSet.getId(), folderNode);

        RuleSet returnedRuleSet = ruleSetLoader.loadRuleSet(ruleSetNode, folderNode, includes);

        // Currently the only field that can be updated is ruleIds to reorder the rules.
        List<String> suppliedRuleIds = ruleSet.getRuleIds();
        if (!isEmpty(suppliedRuleIds))
        {
            // Check there are no duplicate rule ids in the request.
            Set<String> suppliedRuleIdSet = new HashSet<>(suppliedRuleIds);

            // Check that the set of rule ids hasn't changed.
            Set<String> existingRuleIds = new HashSet<>(ruleSetLoader.loadRuleIds(folderNode));
            if (suppliedRuleIdSet.size() != suppliedRuleIds.size() || !suppliedRuleIdSet.equals(existingRuleIds))
            {
                throw new InvalidArgumentException("Unexpected set of rule ids - received " + suppliedRuleIds + " but expected " + existingRuleIds);
            }

            IntStream.range(0, suppliedRuleIds.size()).forEach(index ->
            {
                NodeRef ruleNode = new NodeRef(STORE_REF_WORKSPACE_SPACESSTORE, suppliedRuleIds.get(index));
                ruleService.setRulePosition(folderNode, ruleNode, index);
            });
            if (includes.contains(RuleSetLoader.RULE_IDS))
            {
                returnedRuleSet.setRuleIds(suppliedRuleIds);
            }
        }

        return returnedRuleSet;
    }

    @Override
    public RuleSetLink linkToRuleSet(String folderNodeId, String linkToNodeId)
    {

        final NodeRef folderNodeRef = validator.validateFolderNode(folderNodeId,true);
        final boolean isRuleSetNode = validator.isRuleSetNode(linkToNodeId);
        final NodeRef linkToNodeRef = isRuleSetNode
                ? validator.validateRuleSetNode(linkToNodeId, false)
                : validator.validateFolderNode(linkToNodeId, false);

        //The target node should have pre-existing rules to link to
        if (!ruleService.hasRules(linkToNodeRef)) {
            throw new InvalidArgumentException("The target node has no rules to link.");
        }

        //The folder shouldn't have any pre-existing rules
        if (ruleService.hasNonInheritedRules(folderNodeRef)) {
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
        final NodeRef ruleSetNodeRef = validator.validateRuleSetNode(ruleSetId, folderNodeRef);

        //The folder should be linked to a rule set
        if (!ruleService.isLinkedToRuleNode(folderNodeRef))
        {
            throw new InvalidArgumentException("The folder is not linked to a rule set.");
        }

        //The following line also handles the deletion of the parent-child association that gets created during linking
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
