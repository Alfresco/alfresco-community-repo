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

import static org.alfresco.rest.api.model.rules.InclusionType.INHERITED;
import static org.alfresco.rest.api.model.rules.InclusionType.LINKED;
import static org.alfresco.rest.api.model.rules.InclusionType.OWNED;

import java.util.List;
import java.util.stream.Collectors;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.rest.api.model.rules.RuleSet;
import org.alfresco.service.Experimental;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.rule.RuleService;

/** Responsible for converting a NodeRef into a {@link RuleSet} object. */
@Experimental
public class RuleSetLoader
{
    protected static final String OWNING_FOLDER = "owningFolder";
    protected static final String INCLUSION_TYPE = "inclusionType";
    protected static final String INHERITED_BY = "inheritedBy";
    protected static final String LINKED_TO_BY = "linkedToBy";
    protected static final String IS_INHERITED = "isInherited";
    private static final int MAX_INHERITED_BY_SIZE = 100;
    private NodeService nodeService;
    private RuleService ruleService;

    /**
     * Load a rule set for the given node ref.
     *
     * @param ruleSetNodeRef The rule set node.
     * @param includes A list of fields to include.
     * @return The rule set object.
     */
    public RuleSet loadRuleSet(NodeRef ruleSetNodeRef, NodeRef folderNodeRef, List<String> includes)
    {
        String ruleSetId = ruleSetNodeRef.getId();
        RuleSet ruleSet = RuleSet.of(ruleSetId);

        if (includes != null)
        {
            NodeRef parentRef = nodeService.getPrimaryParent(ruleSetNodeRef).getParentRef();
            if (includes.contains(OWNING_FOLDER))
            {
                ruleSet.setOwningFolder(parentRef);
            }
            if (includes.contains(INCLUSION_TYPE))
            {
                // In the case that a rule set applies to the given folder for multiple reasons then priority is given to owned, then linked, then inherited.
                if (parentRef.equals(folderNodeRef))
                {
                    ruleSet.setInclusionType(OWNED);
                }
                else
                {
                    boolean linked = nodeService.getParentAssocs(ruleSetNodeRef)
                                           .stream().map(ChildAssociationRef::getParentRef)
                                           .anyMatch(folderNodeRef::equals);
                    ruleSet.setInclusionType(linked ? LINKED : INHERITED);
                }
            }
            if (includes.contains(INHERITED_BY))
            {
                ruleSet.setInheritedBy(loadInheritedBy(ruleSetNodeRef));
            }
            if (includes.contains(LINKED_TO_BY))
            {
                List<NodeRef> linkedToBy = nodeService.getParentAssocs(ruleSetNodeRef)
                                                      .stream()
                                                      .map(ChildAssociationRef::getParentRef)
                                                      .filter(folder -> !folder.equals(parentRef))
                                                      .collect(Collectors.toList());
                ruleSet.setLinkedToBy(linkedToBy);
            }
            if (includes.contains(IS_INHERITED))
            {
                ruleSet.setIsInherited(loadIsInherited(ruleSetNodeRef));
            }
        }
        return ruleSet;
    }

    private List<NodeRef> loadInheritedBy(NodeRef ruleSetNodeRef)
    {
        return ruleService.getFoldersInheritingRuleSet(ruleSetNodeRef, MAX_INHERITED_BY_SIZE);
    }

    private boolean loadIsInherited(NodeRef ruleSetNodeRef)
    {
        return AuthenticationUtil.runAsSystem(() -> !ruleService.getFoldersInheritingRuleSet(ruleSetNodeRef, 1).isEmpty());
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setRuleService(RuleService ruleService)
    {
        this.ruleService = ruleService;
    }
}
