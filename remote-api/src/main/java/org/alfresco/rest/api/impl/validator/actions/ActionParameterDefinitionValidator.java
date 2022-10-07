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
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.rest.api.Actions;
import org.alfresco.rest.api.actions.ActionValidator;
import org.alfresco.rest.api.model.ActionDefinition;
import org.alfresco.rest.api.model.ActionParameterConstraint;
import org.alfresco.rest.api.model.rules.Action;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;

/**
 * This class will validate all action types against action parameters definitions (mandatory parameters, parameter constraints)
 */
public class ActionParameterDefinitionValidator implements ActionValidator
{
    private static final boolean IS_ENABLED = true;
    static final String INVALID_PARAMETER_VALUE =
            "Action parameter: %s has invalid value (%s). Look up possible values for constraint name %s";

    private final Actions actions;

    public ActionParameterDefinitionValidator(Actions actions)
    {
        this.actions = actions;
    }

    /**
     * Validates action against its paramaters definitions (mandatory parameters, parameter constraints)
     *
     * @param action Action to be validated
     */
    @Override
    public void validate(Action action)
    {
        final ActionDefinition actionDefinition = actions.getActionDefinitionById(action.getActionDefinitionId());
        actionDefinition.getParameterDefinitions().forEach(p -> validateParameters(p, action));
    }

    /**
     * This list is meant to pick up all action definition names so that the validation will be executed against all possible actions.
     *
     * @return List of all action definition names derived from sub classes of ActionExecuterAbstractBase
     */
    @Override
    public List<String> getActionDefinitionIds()
    {
        final ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AssignableTypeFilter(ActionExecuterAbstractBase.class));

        final Set<BeanDefinition> components = provider.findCandidateComponents("org/alfresco/repo/action/executer");
        final List<String> actionDefinitionsIds = new ArrayList<>();
        for (BeanDefinition component : components)
        {
            try
            {
                final Class<?> clazz = Class.forName(component.getBeanClassName());
                final Field field = clazz.getDeclaredField("NAME");
                actionDefinitionsIds.add((String) field.get(String.class));
            } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e)
            {
                //don't add to the list
            }
        }
        return actionDefinitionsIds;
    }

    @Override
    public boolean isEnabled()
    {
        return IS_ENABLED;
    }

    private void validateParameters(ActionDefinition.ParameterDefinition parameterDefinition, Action action)
    {
        final Serializable parameterValue = action.getParams().get(parameterDefinition.getName());
        if (parameterDefinition.isMandatory() && parameterValue == null)
        {
            throw new IllegalArgumentException("Missing action mandatory parameter: " + parameterDefinition.getName());
        }
        if (parameterDefinition.getParameterConstraintName() != null)
        {
            final ActionParameterConstraint actionConstraint =
                    actions.getActionConstraint(parameterDefinition.getParameterConstraintName());
            if (actionConstraint.getConstraintValues().stream()
                    .noneMatch(constraintData -> constraintData.getValue().equals(parameterValue.toString())))
            {
                throw new IllegalArgumentException(String.format(INVALID_PARAMETER_VALUE, parameterDefinition.getName(), parameterValue,
                        actionConstraint.getConstraintName()));
            }
        }
    }


}
