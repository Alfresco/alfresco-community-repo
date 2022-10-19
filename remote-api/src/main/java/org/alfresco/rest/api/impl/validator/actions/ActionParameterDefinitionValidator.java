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

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.rest.api.Actions;
import org.alfresco.rest.api.actions.ActionValidator;
import org.alfresco.rest.api.model.ActionDefinition;
import org.alfresco.rest.api.model.ActionParameterConstraint;
import org.alfresco.rest.api.model.rules.Action;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.core.exceptions.NotFoundException;
import org.alfresco.service.Experimental;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.logging.log4j.util.Strings;

/**
 * This class will validate all action types against action parameters definitions (mandatory parameters, parameter constraints)
 */
@Experimental
public class ActionParameterDefinitionValidator implements ActionValidator
{
    private static final boolean IS_ENABLED = true;

    static final String INVALID_PARAMETER_VALUE =
            "Action parameter: %s has invalid value (%s). Look up possible values for constraint name %s";
    static final String MISSING_PARAMETER = "Missing action's mandatory parameter: %s";
    static final String MUST_NOT_CONTAIN_PARAMETER = "Action of definition id: %s must not contain parameter of name: %s";
    static final String PARAMS_SHOULD_NOT_BE_EMPTY =
            "Action parameters should not be null or empty for this action. See Action Definition for action of: %s";
    static final String INVALID_ACTION_DEFINITION = "Invalid rule action definition requested %s";
    static final String EMPTY_ACTION_DEFINITION = "Empty/null rule action definition id";

    private final Actions actions;

    public ActionParameterDefinitionValidator(Actions actions)
    {
        this.actions = actions;
    }

    /**
     * Validates action against its parameters definitions (mandatory parameters, parameter constraints)
     *
     * @param action Action to be validated
     */
    @Override
    public void validate(Action action)
    {
        ActionDefinition actionDefinition;
        final String actionDefinitionId = action.getActionDefinitionId();
        if (Strings.isBlank(actionDefinitionId))
        {
            throw new InvalidArgumentException(EMPTY_ACTION_DEFINITION);
        }
        try
        {
            actionDefinition = actions.getRuleActionDefinitionById(actionDefinitionId);
        } catch (NotFoundException e)
        {
            throw new InvalidArgumentException(String.format(INVALID_ACTION_DEFINITION, actionDefinitionId));
        }
        validateParametersSize(action.getParams(), actionDefinition);
        final Map<String, Serializable> params = action.getParams();
        if (MapUtils.isNotEmpty(params))
        {
            params.forEach((key, value) -> checkParameterShouldExist(key, actionDefinition));
            actionDefinition.getParameterDefinitions().forEach(p -> validateParameterDefinitions(p, params));
        }
    }

    @Override
    public boolean isEnabled()
    {
        return IS_ENABLED;
    }

    /**
     * This validator should be applied to all actions
     *
     * @return list of all defined action definition ids
     */
    @Override
    public List<String> getActionDefinitionIds()
    {
        return List.of(ALL_ACTIONS);
    }

    /**
     * This validator should have highest priority and be executed first of all.
     *
     * @return minimal integer value
     */
    @Override
    public int getPriority()
    {
        return Integer.MIN_VALUE;
    }

    private void validateParametersSize(final Map<String, Serializable> params, final ActionDefinition actionDefinition)
    {
        if (CollectionUtils.isNotEmpty(actionDefinition.getParameterDefinitions()) && MapUtils.isEmpty(params))
        {
            throw new InvalidArgumentException(String.format(PARAMS_SHOULD_NOT_BE_EMPTY, actionDefinition.getName()));
        }
    }

    private void validateParameterDefinitions(final ActionDefinition.ParameterDefinition parameterDefinition,
                                              final Map<String, Serializable> params)
    {
        final Serializable parameterValue = params.get(parameterDefinition.getName());
        if (parameterDefinition.isMandatory() && parameterValue == null)
        {
            throw new InvalidArgumentException(String.format(MISSING_PARAMETER, parameterDefinition.getName()));
        }
        if (parameterDefinition.getParameterConstraintName() != null)
        {
            final ActionParameterConstraint actionConstraint =
                    actions.getActionConstraint(parameterDefinition.getParameterConstraintName());
            if (parameterValue != null && actionConstraint.getConstraintValues().stream()
                    .noneMatch(constraintData -> constraintData.getValue().equals(parameterValue.toString())))
            {
                throw new InvalidArgumentException(String.format(INVALID_PARAMETER_VALUE, parameterDefinition.getName(), parameterValue,
                        actionConstraint.getConstraintName()));
            }
        }
    }

    private void checkParameterShouldExist(final String parameterName, final ActionDefinition actionDefinition)
    {
        if (actionDefinition.getParameterDefinitions().stream().noneMatch(pd -> parameterName.equals(pd.getName())))
        {
            throw new InvalidArgumentException(String.format(MUST_NOT_CONTAIN_PARAMETER, actionDefinition.getName(), parameterName));
        }
    }
}
