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

package org.alfresco.rest.api.impl.validator.actions;

import java.util.List;

import org.alfresco.repo.action.executer.CopyActionExecuter;
import org.alfresco.rest.api.Actions;
import org.alfresco.rest.api.actions.ActionValidator;
import org.alfresco.rest.api.model.ActionDefinition;
import org.alfresco.rest.api.model.rules.Action;

public class CopyActionValidator implements ActionValidator
{
    private final Actions actions;

    public CopyActionValidator(Actions actions)
    {
        this.actions = actions;
    }

    @Override
    public void validate(Action action)
    {
        for (final String actionDefinitionId : getActionDefinitionIds())
        {
            final ActionDefinition actionDefinition = actions.getActionDefinitionById(actionDefinitionId);
            actionDefinition.getParameterDefinitions().forEach(p -> validate(p, action));
        }
    }

    private void validate(ActionDefinition.ParameterDefinition parameterDefinition, Action action)
    {
        if (action.getParams().get(parameterDefinition.getName()) == null) {
            throw new IllegalArgumentException("Missing action mandatory parameter: " + parameterDefinition.getName());
        }
        //TODO: further validations here
    }

    @Override
    public List<String> getActionDefinitionIds()
    {
        return List.of(CopyActionExecuter.NAME);
    }

    @Override
    public boolean isEnabled()
    {
        return true;
    }
}
