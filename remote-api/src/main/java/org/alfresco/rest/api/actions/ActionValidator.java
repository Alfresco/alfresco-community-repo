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

package org.alfresco.rest.api.actions;

import java.util.List;

import org.alfresco.rest.api.model.rules.Action;
import org.alfresco.service.Experimental;

@Experimental
public interface ActionValidator
{

    String ALL_ACTIONS = "all";

    /**
     * Provides validation logic for given action.
     */
    void validate(Action action);

    /**
     * Returns priority of validator (applied to bulk validation in @see {@link org.alfresco.rest.api.impl.mapper.rules.RestRuleActionModelMapper})
     * The lower number, the higher priority is set for the validator.
     * @return priority expressed as int
     */
    int getPriority();

    /**
     * By default validator is applied to all actions
     *
     * @return indicator for all defined action definition ids
     */
    default List<String> getActionDefinitionIds() {
        return List.of(ALL_ACTIONS);
    }
}
