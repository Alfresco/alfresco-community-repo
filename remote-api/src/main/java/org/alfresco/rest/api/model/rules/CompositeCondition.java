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

package org.alfresco.rest.api.model.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.alfresco.service.Experimental;
import org.alfresco.service.cmr.action.ActionCondition;
import org.apache.commons.collections.CollectionUtils;

@Experimental
public class CompositeCondition
{
    private boolean inverted;
    private ConditionOperator booleanMode = ConditionOperator.AND;
    private List<CompositeCondition> compositeConditions;
    private List<SimpleCondition> simpleConditions;

    /**
     * Converts Action conditions (service POJO) list to composite condition (REST model).
     *
     * @param actionConditions - list of {@link ActionCondition} service POJOs
     * @return {@link CompositeCondition} REST model
     */
    public static CompositeCondition from(final List<ActionCondition> actionConditions)
    {
        if (actionConditions == null)
        {
            return null;
        }

        final CompositeCondition conditions = new CompositeCondition();
        conditions.compositeConditions = new ArrayList<>();
        // group action conditions by inversion flag
        actionConditions.stream().filter(Objects::nonNull).collect(Collectors.groupingBy(ActionCondition::getInvertCondition))
            // map action condition sub lists
            .forEach((inverted, actionConditionsPart) -> Optional.ofNullable(CompositeCondition.ofActionConditions(actionConditionsPart, inverted, ConditionOperator.AND))
                // if composite condition present add to final list
                .ifPresent(compositeCondition -> conditions.compositeConditions.add(compositeCondition)));

        if (conditions.compositeConditions.isEmpty()) {
            conditions.compositeConditions = null;
        }

        return conditions;
    }

    private static CompositeCondition ofActionConditions(final List<ActionCondition> actionConditions, final boolean inverted, final ConditionOperator conditionOperator)
    {
        if (actionConditions == null)
        {
            return null;
        }

        return ofSimpleConditions(SimpleCondition.listOf(actionConditions), inverted, conditionOperator);
    }

    /**
     * Creates a composite condition instance of simple conditions.
     *
     * @param simpleConditions - list of {@link SimpleCondition}
     * @param inverted - determines if condition should be inverted
     * @param conditionOperator - determines the operation, see {@link ConditionOperator}
     * @return {@link CompositeCondition}
     */
    public static CompositeCondition ofSimpleConditions(final List<SimpleCondition> simpleConditions, final boolean inverted, final ConditionOperator conditionOperator)
    {
        return of(simpleConditions, null, inverted, conditionOperator);
    }

    private static CompositeCondition of(final List<SimpleCondition> simpleConditions, final List<CompositeCondition> compositeConditions,
        final boolean inverted, final ConditionOperator conditionOperator)
    {
        if (CollectionUtils.isEmpty(simpleConditions) && CollectionUtils.isEmpty(compositeConditions))
        {
            return null;
        }

        return builder()
            .inverted(inverted)
            .booleanMode(conditionOperator)
            .simpleConditions(simpleConditions)
            .compositeConditions(compositeConditions)
            .create();
    }

    public boolean isInverted()
    {
        return inverted;
    }

    public void setInverted(boolean inverted)
    {
        this.inverted = inverted;
    }

    public String getBooleanMode()
    {
        if (booleanMode == null)
        {
            return null;
        }
        return booleanMode.name().toLowerCase();
    }

    public void setBooleanMode(ConditionOperator booleanMode)
    {
        this.booleanMode = booleanMode;
    }

    public List<CompositeCondition> getCompositeConditions()
    {
        return compositeConditions;
    }

    public void setCompositeConditions(List<CompositeCondition> compositeConditions)
    {
        this.compositeConditions = compositeConditions;
    }

    public List<SimpleCondition> getSimpleConditions()
    {
        return simpleConditions;
    }

    public void setSimpleConditions(List<SimpleCondition> simpleConditions)
    {
        this.simpleConditions = simpleConditions;
    }

    @Override
    public String toString()
    {
        return "CompositeCondition{" + "inverted=" + inverted + ", booleanMode=" + booleanMode + ", compositeConditions=" + compositeConditions + ", simpleConditions="
            + simpleConditions + '}';
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        CompositeCondition that = (CompositeCondition) o;
        return inverted == that.inverted && booleanMode == that.booleanMode && Objects.equals(compositeConditions, that.compositeConditions) && Objects.equals(
            simpleConditions, that.simpleConditions);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(inverted, booleanMode, compositeConditions, simpleConditions);
    }

    public static Builder builder()
    {
        return new Builder();
    }

    public static class Builder
    {
        private boolean inverted;
        private ConditionOperator booleanMode = ConditionOperator.AND;
        private List<CompositeCondition> compositeConditions;
        private List<SimpleCondition> simpleConditions;

        public Builder inverted(boolean inverted)
        {
            this.inverted = inverted;
            return this;
        }

        public Builder booleanMode(ConditionOperator booleanMode)
        {
            this.booleanMode = booleanMode;
            return this;
        }

        public Builder compositeConditions(List<CompositeCondition> compositeConditions)
        {
            this.compositeConditions = compositeConditions;
            return this;
        }

        public Builder simpleConditions(List<SimpleCondition> simpleConditions)
        {
            this.simpleConditions = simpleConditions;
            return this;
        }

        public CompositeCondition create()
        {
            final CompositeCondition condition = new CompositeCondition();
            condition.setInverted(inverted);
            condition.setBooleanMode(booleanMode);
            condition.setCompositeConditions(compositeConditions);
            condition.setSimpleConditions(simpleConditions);
            return condition;
        }
    }
}
