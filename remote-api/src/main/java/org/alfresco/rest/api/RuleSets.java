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
package org.alfresco.rest.api;

import java.util.List;

import org.alfresco.rest.api.model.rules.RuleSet;
import org.alfresco.rest.api.model.rules.RuleSetLink;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.service.Experimental;

/**
 * Rule sets API.
 */
@Experimental
public interface RuleSets
{
    /**
     * Get rule sets for a folder.
     *
     * @param folderNodeId Folder node ID
     * @param paging {@link Paging} information
     * @param includes List of fields to include in the rule set
     * @return {@link CollectionWithPagingInfo} containing a list page of rule sets
     */
    CollectionWithPagingInfo<RuleSet> getRuleSets(String folderNodeId, List<String> includes, Paging paging);

    /**
     * Get the rule set with the given ID and check associations with the folder node.
     *
     * @param folderNodeId Folder node ID
     * @param ruleSetId Rule set ID
     * @param includes List of fields to include in the rule set
     * @return {@link RuleSet} definition
     */
    RuleSet getRuleSetById(String folderNodeId, String ruleSetId, List<String> includes);

    /**
     * Update a rule set - for example to reorder the rules within it.
     *
     * @param folderNodeId Folder node ID
     * @param ruleSet The updated rule set.
     * @param includes List of fields to include in the response.
     * @return The updated rule set from the server.
     */
    RuleSet updateRuleSet(String folderNodeId, RuleSet ruleSet, List<String> includes);

    /**
     * Link a rule set to a folder
     */
    RuleSetLink linkToRuleSet(String folderNodeId, String linkToNodeId);

    /**
     * Removes the link between a rule set and a folder
     */
    void unlinkRuleSet(String folderNodeId, String ruleSetId);
}
