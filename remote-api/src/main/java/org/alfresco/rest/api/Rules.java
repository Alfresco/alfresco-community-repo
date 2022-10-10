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

import org.alfresco.rest.api.model.rules.Rule;
import org.alfresco.rest.api.model.rules.RuleExecution;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.service.Experimental;
import org.alfresco.service.cmr.rule.RuleServiceException;

/**
 * Folder node rules API.
 *
 */
@Experimental
public interface Rules
{
    /**
     * Get rules for node's and rule set's IDs
     *
     * @param folderNodeId - folder node ID
     * @param ruleSetId - rule set ID
     * @param includes - The list of optional fields to include in the response.
     * @param paging - {@link Paging} information
     * @return {@link CollectionWithPagingInfo} containing a list page of folder rules
     */
    CollectionWithPagingInfo<Rule> getRules(String folderNodeId, String ruleSetId, List<String> includes, Paging paging);

    /**
     * Get rule for rule's ID and check associations with folder node and rule set node
     *
     * @param folderNodeId - folder node ID
     * @param ruleSetId - rule set ID
     * @param ruleId - rule ID
     * @param includes - The list of optional fields to include in the response.
     * @return {@link Rule} definition
     */
    Rule getRuleById(String folderNodeId, String ruleSetId, String ruleId, List<String> includes);

    /**
     * Create new rules (and potentially a rule set if "-default-" is supplied).
     *
     * @param folderNodeId The node id of a folder.
     * @param ruleSetId The id of a rule set (or "-default-" to use/create the default rule set for the folder).
     * @param rule The definition of the rule.
     * @param includes The list of optional fields to include in the response.
     * @return The newly created rules.
     * @throws InvalidArgumentException If the nodes are not the expected types, or the rule set does not correspond to the folder.
     * @throws RuleServiceException If the folder is already linked to another rule set.
     */
    List<Rule> createRules(String folderNodeId, String ruleSetId, List<Rule> rule, List<String> includes);

    /**
     * Update a rule.
     *
     * @param folderNodeId The id of a folder.
     * @param ruleSetId The id of a rule set within the folder (or "-default-" to use the default rule set for the folder).
     * @param ruleId The rule id.
     * @param rule The new version of the rule.
     * @param includes The list of optional fields to include in the response.
     * @return The newly updated rule.
     */
    Rule updateRuleById(String folderNodeId, String ruleSetId, String ruleId, Rule rule, List<String> includes);

    /**
     * Delete rule for rule's ID and check associations with folder node and rule set node
     *
     * @param folderNodeId - folder node ID
     * @param ruleSetId - rule set ID
     * @param ruleId - rule ID
     */
    void deleteRuleById(String folderNodeId, String ruleSetId, String ruleId);

    /**
     * Execute rules for given folder node.
     *
     * @param folderNodeId - the ID of a folder
     * @param eachSubFolderIncluded - indicates if rules should be executed also on sub-folders
     */
    RuleExecution executeRules(final String folderNodeId, final boolean eachSubFolderIncluded);
}
