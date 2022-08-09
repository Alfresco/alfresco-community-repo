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

import org.alfresco.rest.api.model.rules.RuleSet;
import org.alfresco.service.Experimental;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;

/** Responsible for converting a NodeRef into a {@link RuleSet} object. */
@Experimental
public class RuleSetLoader
{
    private NodeService nodeService;

    /**
     * Load a rule set for the given node ref.
     *
     * @param ruleSetNodeRef The rule set node.
     * @param includes A list of fields to include.
     * @return The rule set object.
     */
    protected RuleSet loadRuleSet(NodeRef ruleSetNodeRef, List<String> includes)
    {
        String ruleSetId = ruleSetNodeRef.getId();
        RuleSet ruleSet = RuleSet.of(ruleSetId);

        if (includes != null && includes.contains("owningFolder"))
        {
            NodeRef parentRef = nodeService.getPrimaryParent(ruleSetNodeRef).getParentRef();
            ruleSet.setOwningFolder(parentRef);
        }
        return ruleSet;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
}
