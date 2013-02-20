/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.web.scripts.rule;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.action.ExtendedActionDefinition;
import org.alfresco.service.cmr.action.ActionDefinition;

/**
 * An abstract class for the java backed webscripts to get the filtered action definition list.
 *
 * @author Tuna Aksoy
 * @since 2.1
 */
public class AbstractActionDefinitionsGet extends AbstractRuleWebScript
{
    /**
     * Returns a model with the filtered action definitions
     *
     * @param removeRmRelatedActionDefs if true the rm related action definitions will be removed, otherwise dm related actions
     * @return Map<String, Object> the model with the filtered action definitions
     */
    protected Map<String, Object> getModelWithFilteredActionDefinitions(boolean removeRmRelatedActionDefs)
    {
        // get all action definitions and filter them
        List<ActionDefinition> actiondefinitions = filterActionDefinitons(actionService.getActionDefinitions(), removeRmRelatedActionDefs);

        Map<String, Object> model = new HashMap<String, Object>();
        model.put("actiondefinitions", actiondefinitions);

        return model;
    }

    /**
     * Filters the action definition list
     *
     * @param actionDefinitions the list of action definitions to filter
     * @param removeRmRelatedActionDefs if true the rm related action definitions will be removed, otherwise dm related actions
     * @return List<ActionDefinition> the filtered list of action definitions
     */
    private List<ActionDefinition> filterActionDefinitons(List<ActionDefinition> actionDefinitions, boolean removeRmRelatedActionDefs)
    {
        for (Iterator<ActionDefinition> iterator = actionDefinitions.iterator(); iterator.hasNext();)
        {
            if ((iterator.next() instanceof ExtendedActionDefinition) == removeRmRelatedActionDefs)
            {
                iterator.remove();
            }
        }
        return actionDefinitions;
    }
}
