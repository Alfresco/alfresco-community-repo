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

import static org.alfresco.repo.action.evaluator.NoConditionEvaluator.NAME;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.alfresco.rest.api.model.mapper.RestModelMapper;
import org.alfresco.rest.api.model.rules.CompositeCondition;
import org.alfresco.rest.api.model.rules.ConditionOperator;
import org.alfresco.rest.api.model.rules.SimpleCondition;
import org.alfresco.service.Experimental;
import org.alfresco.service.cmr.action.ActionCondition;
import org.apache.commons.collections.CollectionUtils;

@Experimental
public class RestRuleCompositeConditionModelMapper implements RestModelMapper<CompositeCondition, ActionCondition>
{
    private final RestModelMapper<SimpleCondition, ActionCondition> simpleConditionMapper;

    public RestRuleCompositeConditionModelMapper(
            RestModelMapper<SimpleCondition, ActionCondition> simpleConditionMapper)
    {
        this.simpleConditionMapper = simpleConditionMapper;
    }

    /**
     * Converts Action conditions (service POJO) list to composite condition (REST model).
     *
     * @param actionConditions - list of {@link ActionCondition} service POJOs
     * @return {@link CompositeCondition} REST model
     */
    @Override
    public CompositeCondition toRestModel(final Collection<ActionCondition> actionConditions)
    {
        if (CollectionUtils.isEmpty(actionConditions))
        {
            return null;
        }
        final List<ActionCondition> filteredActions = actionConditions.stream()
                .filter(Objects::nonNull)
                .filter(c -> !NAME.equals(c.getActionConditionDefinitionName()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(filteredActions))
        {
            return null;
        }
        final CompositeCondition conditions = new CompositeCondition();
        conditions.setCompositeConditions(new ArrayList<>());
        // group action conditions by inversion flag
        filteredActions.stream()
                .collect(Collectors.groupingBy(ActionCondition::getInvertCondition))
                // map action condition sub lists
                .forEach((inverted, actionConditionsPart) -> Optional
                        .ofNullable(ofActionConditions(actionConditionsPart, inverted, ConditionOperator.AND))
                        // if composite condition present add to final list
                        .ifPresent(compositeCondition -> conditions.getCompositeConditions().add(compositeCondition)));

        if (CollectionUtils.isEmpty(conditions.getCompositeConditions()))
        {
            conditions.setCompositeConditions(null);
        }
        return conditions;
    }

    @Override
    public List<ActionCondition> toServiceModels(final CompositeCondition compositeCondition)
    {
        final List<ActionCondition> actionConditions = new ArrayList<>();
        if (compositeCondition == null)
        {
            return actionConditions;
        }
        if (CollectionUtils.isNotEmpty(compositeCondition.getSimpleConditions()))
        {
            compositeCondition.getSimpleConditions()
                    .forEach(simpleCondition -> actionConditions.add(mapSimpleCondition(simpleCondition, compositeCondition.isInverted())));
        }
        if (CollectionUtils.isNotEmpty(compositeCondition.getCompositeConditions()))
        {
            compositeCondition.getCompositeConditions().forEach(condition -> actionConditions.addAll(toServiceModels(condition)));
        }

        return actionConditions;
    }

    private ActionCondition mapSimpleCondition(final SimpleCondition simpleCondition, final boolean inverted)
    {
        final ActionCondition actionCondition = simpleConditionMapper.toServiceModel(simpleCondition);
        actionCondition.setInvertCondition(inverted);
        return actionCondition;
    }

    private CompositeCondition ofActionConditions(final List<ActionCondition> actionConditions, final boolean inverted,
                                                  final ConditionOperator conditionOperator)
    {
        if (CollectionUtils.isEmpty(actionConditions))
        {
            return null;
        }
        return ofSimpleConditions(simpleConditionMapper.toRestModels(actionConditions), inverted, conditionOperator);
    }

    /**
     * Creates a composite condition instance of simple conditions.
     *
     * @param simpleConditions  - list of {@link SimpleCondition}
     * @param inverted          - determines if condition should be inverted
     * @param conditionOperator - determines the operation, see {@link ConditionOperator}
     * @return {@link CompositeCondition}
     */
    private CompositeCondition ofSimpleConditions(final List<SimpleCondition> simpleConditions, final boolean inverted,
                                                  final ConditionOperator conditionOperator)
    {
        return of(simpleConditions, null, inverted, conditionOperator);
    }

    private CompositeCondition of(final List<SimpleCondition> simpleConditions, final List<CompositeCondition> compositeConditions,
                                  final boolean inverted, final ConditionOperator conditionOperator)
    {
        if (CollectionUtils.isEmpty(simpleConditions) && CollectionUtils.isEmpty(compositeConditions))
        {
            return null;
        }
        return CompositeCondition.builder()
                .inverted(inverted)
                .booleanMode(conditionOperator)
                .simpleConditions(simpleConditions)
                .compositeConditions(compositeConditions)
                .create();
    }
}
