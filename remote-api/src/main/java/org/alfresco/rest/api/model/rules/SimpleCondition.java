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

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.alfresco.repo.action.evaluator.CompareMimeTypeEvaluator;
import org.alfresco.repo.action.evaluator.ComparePropertyValueEvaluator;
import org.alfresco.repo.action.evaluator.HasAspectEvaluator;
import org.alfresco.repo.action.evaluator.HasChildEvaluator;
import org.alfresco.repo.action.evaluator.HasTagEvaluator;
import org.alfresco.repo.action.evaluator.HasVersionHistoryEvaluator;
import org.alfresco.repo.action.evaluator.InCategoryEvaluator;
import org.alfresco.repo.action.evaluator.IsSubTypeEvaluator;
import org.alfresco.repo.action.evaluator.NoConditionEvaluator;
import org.alfresco.service.Experimental;
import org.alfresco.service.cmr.action.ActionCondition;
import org.apache.commons.collections.CollectionUtils;

@Experimental
public class SimpleCondition
{
    private static final String COMPARATOR_EQUALS = "equals";

    private String field;
    private String comparator;
    private String parameter;

    /**
     * Converts list of service POJO action conditions to list of REST model simple conditions.
     *
     * @param actionConditions - list of {@link ActionCondition} service POJOs
     * @return list of {@link SimpleCondition} REST models
     */
    public static List<SimpleCondition> listOf(final List<ActionCondition> actionConditions)
    {
        if (CollectionUtils.isEmpty(actionConditions))
        {
            return null;
        }

        return actionConditions.stream()
            .map(SimpleCondition::from)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    /**
     * Creates simple condition REST model instance from service POJO action condition.
     *
     * @param actionCondition - {@link ActionCondition} service POJO
     * @return {@link SimpleCondition} REST model
     */
    public static SimpleCondition from(final ActionCondition actionCondition)
    {
        if (actionCondition == null || actionCondition.getActionConditionDefinitionName() == null || actionCondition.getParameterValues() == null)
        {
            return null;
        }

        switch (actionCondition.getActionConditionDefinitionName())
        {
        case ComparePropertyValueEvaluator.NAME:
            return createComparePropertyValueCondition(actionCondition);
        case CompareMimeTypeEvaluator.NAME:
            return createCompareMimeTypeCondition(actionCondition);
        case HasAspectEvaluator.NAME:
            return createHasAspectCondition(actionCondition);
        case HasChildEvaluator.NAME:
            return createHasChildCondition(actionCondition);
        case HasTagEvaluator.NAME:
            return createHasTagCondition(actionCondition);
        case HasVersionHistoryEvaluator.NAME:
            return createHasVersionHistoryCondition(actionCondition);
        case InCategoryEvaluator.NAME:
            return createInCategoryCondition(actionCondition);
        case IsSubTypeEvaluator.NAME:
            return createIsSubtypeCondition(actionCondition);
        case NoConditionEvaluator.NAME:
        default:
            return null;
        }
    }

    public String getField()
    {
        return field;
    }

    public void setField(String field)
    {
        this.field = field;
    }

    public String getComparator()
    {
        return comparator;
    }

    public void setComparator(String comparator)
    {
        this.comparator = comparator;
    }

    public String getParameter()
    {
        return parameter;
    }

    public void setParameter(String parameter)
    {
        this.parameter = parameter;
    }

    @Override
    public String toString()
    {
        return "SimpleCondition{" + "field='" + field + '\'' + ", comparator='" + comparator + '\'' + ", parameter='" + parameter + '\'' + '}';
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        SimpleCondition that = (SimpleCondition) o;
        return Objects.equals(field, that.field) && Objects.equals(comparator, that.comparator) && Objects.equals(parameter, that.parameter);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(field, comparator, parameter);
    }

    private static SimpleCondition createComparePropertyValueCondition(final ActionCondition actionCondition) {
        final SimpleCondition.Builder builder = builder();
        if (actionCondition.getParameterValues().get(ComparePropertyValueEvaluator.PARAM_CONTENT_PROPERTY) != null)
        {
            builder.field(actionCondition.getParameterValues().get(ComparePropertyValueEvaluator.PARAM_CONTENT_PROPERTY).toString().toLowerCase());
        } else {
            builder.field(actionCondition.getParameterValues().get(ComparePropertyValueEvaluator.PARAM_PROPERTY).toString().toLowerCase());
        }
        return builder
            .comparator(actionCondition.getParameterValues().get(ComparePropertyValueEvaluator.PARAM_OPERATION).toString().toLowerCase())
            .parameter(actionCondition.getParameterValues().get(ComparePropertyValueEvaluator.PARAM_VALUE).toString())
            .create();
    }

    private static SimpleCondition createCompareMimeTypeCondition(final ActionCondition actionCondition) {
        return builder()
            .field(actionCondition.getParameterValues().get(ComparePropertyValueEvaluator.PARAM_PROPERTY).toString().toLowerCase())
            .comparator(COMPARATOR_EQUALS)
            .parameter(actionCondition.getParameterValues().get(ComparePropertyValueEvaluator.PARAM_VALUE).toString())
            .create();
    }

    private static SimpleCondition createHasAspectCondition(final ActionCondition actionCondition) {
        return builder()
            .field(HasAspectEvaluator.PARAM_ASPECT)
            .comparator(COMPARATOR_EQUALS)
            .parameter(actionCondition.getParameterValues().get(HasAspectEvaluator.PARAM_ASPECT).toString())
            .create();
    }

    private static SimpleCondition createHasChildCondition(final ActionCondition actionCondition) {
        final SimpleCondition.Builder builder = builder();
        if (actionCondition.getParameterValues().get(HasChildEvaluator.PARAM_ASSOC_TYPE) != null)
        {
            builder.field(actionCondition.getParameterValues().get(HasChildEvaluator.PARAM_ASSOC_TYPE).toString().toLowerCase());
        } else {
            builder.field(actionCondition.getParameterValues().get(HasChildEvaluator.PARAM_ASSOC_NAME).toString().toLowerCase());
        }
        return builder
            .comparator(COMPARATOR_EQUALS)
            .parameter(actionCondition.getParameterValues().get(ComparePropertyValueEvaluator.PARAM_VALUE).toString())
            .create();
    }

    private static SimpleCondition createHasTagCondition(final ActionCondition actionCondition) {
        return builder()
            .field(HasTagEvaluator.PARAM_TAG)
            .comparator(COMPARATOR_EQUALS)
            .parameter(actionCondition.getParameterValues().get(HasTagEvaluator.PARAM_TAG).toString())
            .create();
    }

    private static SimpleCondition createHasVersionHistoryCondition(final ActionCondition actionCondition) {
        return builder()
            .field(actionCondition.getParameterValues().get(ComparePropertyValueEvaluator.PARAM_PROPERTY).toString().toLowerCase())
            .comparator(actionCondition.getParameterValues().get(ComparePropertyValueEvaluator.PARAM_OPERATION).toString().toLowerCase())
            .parameter(actionCondition.getParameterValues().get(ComparePropertyValueEvaluator.PARAM_VALUE).toString())
            .create();
    }

    private static SimpleCondition createInCategoryCondition(final ActionCondition actionCondition) {
        return builder()
            .field(actionCondition.getParameterValues().get(InCategoryEvaluator.PARAM_CATEGORY_ASPECT).toString().toLowerCase())
            .comparator(COMPARATOR_EQUALS)
            .parameter(actionCondition.getParameterValues().get(InCategoryEvaluator.PARAM_CATEGORY_VALUE).toString())
            .create();
    }

    private static SimpleCondition createIsSubtypeCondition(final ActionCondition actionCondition) {
        return builder()
            .field(IsSubTypeEvaluator.PARAM_TYPE)
            .comparator(COMPARATOR_EQUALS)
            .parameter(actionCondition.getParameterValues().get(IsSubTypeEvaluator.PARAM_TYPE).toString())
            .create();
    }

    public static Builder builder()
    {
        return new Builder();
    }

    public static class Builder
    {
        private String field;
        private String comparator;
        private String parameter;

        public Builder field(String field)
        {
            this.field = field;
            return this;
        }

        public Builder comparator(String comparator)
        {
            this.comparator = comparator;
            return this;
        }

        public Builder parameter(String parameter)
        {
            this.parameter = parameter;
            return this;
        }

        public SimpleCondition create() {
            final SimpleCondition condition = new SimpleCondition();
            condition.setField(field);
            condition.setComparator(comparator);
            condition.setParameter(parameter);
            return condition;
        }
    }
}
