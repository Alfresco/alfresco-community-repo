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

import org.alfresco.rest.api.model.rules.Rule;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.service.Experimental;
import org.alfresco.service.cmr.rule.RuleServiceException;

import java.util.List;

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
     * @param paging - {@link Paging} information
     * @return {@link CollectionWithPagingInfo} containing a list page of folder rules
     */
    CollectionWithPagingInfo<Rule> getRules(String folderNodeId, String ruleSetId, Paging paging);

    /**
     * Get rule for rule's ID and check associations with folder node and rule set node
     *
     * @param folderNodeId - folder node ID
     * @param ruleSetId - rule set ID
     * @param ruleId - rule ID
     * @return {@link Rule} definition
     */
    Rule getRuleById(String folderNodeId, String ruleSetId, String ruleId);

    /**
     * Create new rules (and potentially a rule set if "_default_" is supplied).
     *
     * @param folderNodeId The node id of a folder.
     * @param ruleSetId The id of a rule set (or "_default_" to use/create the default rule set for the folder).
     * @param rule The definition of the rule.
     * @return The newly created rules.
     * @throws InvalidArgumentException If the nodes are not the expected types, or the rule set does not correspond to the folder.
     * @throws RuleServiceException If the folder is already linked to another rule set.
     */
    List<Rule> createRules(String folderNodeId, String ruleSetId, List<Rule> rule);
}
