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

package org.alfresco.rest.api.impl.mapper.rules;

import static java.util.Collections.emptyMap;

import static org.alfresco.repo.action.access.ActionAccessRestriction.ACTION_CONTEXT_PARAM_NAME;
import static org.alfresco.rest.api.actions.ActionValidator.ALL_ACTIONS;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.alfresco.repo.action.ActionImpl;
import org.alfresco.repo.action.CompositeActionImpl;
import org.alfresco.rest.api.actions.ActionValidator;
import org.alfresco.rest.api.impl.rules.ActionParameterConverter;
import org.alfresco.rest.api.model.mapper.RestModelMapper;
import org.alfresco.rest.api.model.rules.Action;
import org.alfresco.service.Experimental;
import org.alfresco.util.GUID;
import org.apache.commons.collections.CollectionUtils;

@Experimental
public class RestRuleActionModelMapper implements RestModelMapper<Action, org.alfresco.service.cmr.action.Action>
{
    private final ActionParameterConverter parameterConverter;
    private final List<ActionValidator> actionValidators;

    public RestRuleActionModelMapper(ActionParameterConverter parameterConverter,
                                     List<ActionValidator> actionValidators)
    {
        this.parameterConverter = parameterConverter;
        this.actionValidators = actionValidators;
    }

    /**
     * Converts service POJO action to REST model action.
     *
     * @param actionModel - {@link org.alfresco.service.cmr.action.Action} service POJO
     * @return {@link Action} REST model
     */
    @Override
    public Action toRestModel(org.alfresco.service.cmr.action.Action actionModel)
    {
        if (actionModel == null)
        {
            return null;
        }

        final Action.Builder builder = Action.builder().actionDefinitionId(actionModel.getActionDefinitionName());
        if (actionModel.getParameterValues() != null)
        {
            final Map<String, Serializable> convertedParams = actionModel.getParameterValues()
                    .entrySet()
                    .stream()
                    .collect(HashMap::new, (m, v) -> m.put(v.getKey(), parameterConverter.convertParamFromServiceModel(v.getValue())), HashMap::putAll);
            convertedParams.remove(ACTION_CONTEXT_PARAM_NAME);
            builder.params(convertedParams);
        }
        return builder.create();
    }

    /**
     * Convert the REST model objects to composite action service POJO.
     *
     * @param actions List of actions.
     * @return The composite action service POJO.
     */
    @Override
    public org.alfresco.service.cmr.action.Action toServiceModel(Collection<Action> actions)
    {
        if (CollectionUtils.isEmpty(actions))
        {
            return null;
        }

        final org.alfresco.service.cmr.action.CompositeAction compositeAction = new CompositeActionImpl(null, GUID.generate());
        actions.forEach(action -> compositeAction.addAction(toServiceAction(action)));
        return compositeAction;
    }

    private org.alfresco.service.cmr.action.Action toServiceAction(Action action)
    {
        final Map<String, Serializable> params = Optional.ofNullable(action.getParams()).orElse(emptyMap());
        validateAction(action);
        final Map<String, Serializable> convertedParams =
                parameterConverter.getConvertedParams(params, action.getActionDefinitionId());
        return new ActionImpl(null, GUID.generate(), action.getActionDefinitionId(), convertedParams);
    }
    private void validateAction(Action action) {
        actionValidators.stream()
                .filter(v -> (v.getActionDefinitionIds().contains(action.getActionDefinitionId()) ||
                        v.getActionDefinitionIds().equals(List.of(ALL_ACTIONS))))
                .sorted(Comparator.comparing(ActionValidator::getPriority))
                .forEachOrdered(v -> v.validate(action));
    }
}
