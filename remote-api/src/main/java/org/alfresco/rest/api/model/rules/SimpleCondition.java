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

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ActionConditionImpl;
import org.alfresco.repo.action.evaluator.CompareMimeTypeEvaluator;
import org.alfresco.repo.action.evaluator.ComparePropertyValueEvaluator;
import org.alfresco.repo.action.evaluator.HasAspectEvaluator;
import org.alfresco.repo.action.evaluator.HasTagEvaluator;
import org.alfresco.repo.action.evaluator.InCategoryEvaluator;
import org.alfresco.repo.action.evaluator.IsSubTypeEvaluator;
import org.alfresco.repo.action.evaluator.NoConditionEvaluator;
import org.alfresco.repo.action.evaluator.compare.ComparePropertyValueOperation;
import org.alfresco.repo.action.evaluator.compare.ContentPropertyName;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.service.Experimental;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.collections.CollectionUtils;

@Experimental
public class SimpleCondition
{
    private static final String CATEGORY_INVALID_MSG = "Category in condition is invalid";
    public static final String PARAM_CATEGORY = "category";
    public static final String PARAM_MIMETYPE = "mimetype";

    private String field;
    private String comparator;
    private String parameter;

    /**
     * Converts list of service POJO action conditions to list of REST model simple conditions.
     *
     * @param actionConditions - list of {@link ActionCondition} service POJOs
     * @return list of {@link SimpleCondition} REST models
     */
    public static List<SimpleCondition> listOf(final List<ActionCondition> actionConditions, final NamespaceService namespaceService)
    {
        if (CollectionUtils.isEmpty(actionConditions))
        {
            return null;
        }

        return actionConditions.stream()
            .map(actionCondition -> from(actionCondition, namespaceService))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    /**
     * Creates simple condition REST model instance from service POJO action condition.
     *
     * @param actionCondition - {@link ActionCondition} service POJO
     * @return {@link SimpleCondition} REST model
     */
    public static SimpleCondition from(final ActionCondition actionCondition, final NamespaceService namespaceService)
    {
        if (actionCondition == null || actionCondition.getActionConditionDefinitionName() == null || actionCondition.getParameterValues() == null)
        {
            return null;
        }

        switch (actionCondition.getActionConditionDefinitionName())
        {
        case ComparePropertyValueEvaluator.NAME:
            return createComparePropertyValueCondition(actionCondition, namespaceService);
        case CompareMimeTypeEvaluator.NAME:
            return createCompareMimeTypeCondition(actionCondition);
        case HasAspectEvaluator.NAME:
            return createHasAspectCondition(actionCondition, namespaceService);
        case HasTagEvaluator.NAME:
            return createHasTagCondition(actionCondition);
        case InCategoryEvaluator.NAME:
            return createInCategoryCondition(actionCondition);
        case IsSubTypeEvaluator.NAME:
            return createIsSubtypeCondition(actionCondition, namespaceService);
        case NoConditionEvaluator.NAME:
        default:
            return null;
        }
    }

    public ActionCondition toServiceModel(final boolean inverted, final Nodes nodes, final NamespaceService namespaceService)
    {
        if (field == null)
        {
            return null;
        }

        Map<String, Serializable> parameterValues = new HashMap<>();
        String conditionDefinitionId;

        switch (field)
        {
        case HasAspectEvaluator.PARAM_ASPECT:
            conditionDefinitionId = HasAspectEvaluator.NAME;
            parameterValues.put(HasAspectEvaluator.PARAM_ASPECT, QName.createQName(parameter, namespaceService));
            break;
        case HasTagEvaluator.PARAM_TAG:
            conditionDefinitionId = HasTagEvaluator.NAME;
            parameterValues.put(HasTagEvaluator.PARAM_TAG, parameter);
            break;
        case PARAM_CATEGORY:
            conditionDefinitionId = InCategoryEvaluator.NAME;
            parameterValues.put(InCategoryEvaluator.PARAM_CATEGORY_ASPECT, ContentModel.ASPECT_GEN_CLASSIFIABLE);
            try
            {
                parameterValues.put(InCategoryEvaluator.PARAM_CATEGORY_VALUE, nodes.validateOrLookupNode(parameter, null));
            } catch (EntityNotFoundException e) {
                throw new InvalidArgumentException(CATEGORY_INVALID_MSG);
            }
            break;
        case IsSubTypeEvaluator.PARAM_TYPE:
            conditionDefinitionId = IsSubTypeEvaluator.NAME;
            parameterValues.put(IsSubTypeEvaluator.PARAM_TYPE, QName.createQName(parameter, namespaceService));
            break;
        case PARAM_MIMETYPE:
            conditionDefinitionId = CompareMimeTypeEvaluator.NAME;
            parameterValues.put(ComparePropertyValueEvaluator.PARAM_PROPERTY, ContentModel.TYPE_CONTENT);
            parameterValues.put(ComparePropertyValueEvaluator.PARAM_VALUE, parameter);
            break;
        default:
            conditionDefinitionId = ComparePropertyValueEvaluator.NAME;
            try
            {
                // if size or encoding create content property evaluator
                ContentPropertyName.valueOf(field.toUpperCase());
                parameterValues.put(ComparePropertyValueEvaluator.PARAM_CONTENT_PROPERTY, field.toUpperCase());
                parameterValues.put(ComparePropertyValueEvaluator.PARAM_PROPERTY, ContentModel.TYPE_CONTENT);
            }
            catch (IllegalArgumentException ignore)
            {
                // else create common property evaluator
                parameterValues.put(ComparePropertyValueEvaluator.PARAM_PROPERTY, QName.createQName(field, namespaceService));
            }
            parameterValues.put(ComparePropertyValueEvaluator.PARAM_OPERATION, comparator.toUpperCase());
            parameterValues.put(ComparePropertyValueEvaluator.PARAM_VALUE, parameter);
            break;
        }

        final ActionCondition actionCondition = new ActionConditionImpl(UUID.randomUUID().toString(), conditionDefinitionId, parameterValues);
        actionCondition.setInvertCondition(inverted);
        return actionCondition;
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

    private static SimpleCondition createComparePropertyValueCondition(final ActionCondition actionCondition, final NamespaceService namespaceService)
    {
        final SimpleCondition.Builder builder = builder();
        if (actionCondition.getParameterValues().get(ComparePropertyValueEvaluator.PARAM_CONTENT_PROPERTY) != null)
        {
            builder.field(actionCondition.getParameterValues().get(ComparePropertyValueEvaluator.PARAM_CONTENT_PROPERTY).toString().toLowerCase());
        } else {
            builder.field(((QName) actionCondition.getParameterValues().get(ComparePropertyValueEvaluator.PARAM_PROPERTY)).toPrefixString(namespaceService));
        }
        return builder
            .comparator(actionCondition.getParameterValues().get(ComparePropertyValueEvaluator.PARAM_OPERATION).toString().toLowerCase())
            .parameter(actionCondition.getParameterValues().get(ComparePropertyValueEvaluator.PARAM_VALUE).toString())
            .create();
    }

    private static SimpleCondition createCompareMimeTypeCondition(final ActionCondition actionCondition)
    {
        return builder()
            .field(PARAM_MIMETYPE)
            .comparator(ComparePropertyValueOperation.EQUALS.toString().toLowerCase())
            .parameter(actionCondition.getParameterValues().get(ComparePropertyValueEvaluator.PARAM_VALUE).toString())
            .create();
    }

    private static SimpleCondition createHasAspectCondition(final ActionCondition actionCondition, final NamespaceService namespaceService)
    {
        return builder()
            .field(HasAspectEvaluator.PARAM_ASPECT)
            .comparator(ComparePropertyValueOperation.EQUALS.toString().toLowerCase())
            .parameter(((QName) actionCondition.getParameterValues().get(HasAspectEvaluator.PARAM_ASPECT)).toPrefixString(namespaceService))
            .create();
    }

    private static SimpleCondition createHasTagCondition(final ActionCondition actionCondition)
    {
        return builder()
            .field(HasTagEvaluator.PARAM_TAG)
            .comparator(ComparePropertyValueOperation.EQUALS.toString().toLowerCase())
            .parameter(actionCondition.getParameterValues().get(HasTagEvaluator.PARAM_TAG).toString())
            .create();
    }

    private static SimpleCondition createInCategoryCondition(final ActionCondition actionCondition)
    {
        return builder()
            .field(PARAM_CATEGORY)
            .comparator(ComparePropertyValueOperation.EQUALS.toString().toLowerCase())
            .parameter(((NodeRef) actionCondition.getParameterValues().get(InCategoryEvaluator.PARAM_CATEGORY_VALUE)).getId())
            .create();
    }

    private static SimpleCondition createIsSubtypeCondition(final ActionCondition actionCondition, final NamespaceService namespaceService)
    {
        return builder()
            .field(IsSubTypeEvaluator.PARAM_TYPE)
            .comparator(ComparePropertyValueOperation.EQUALS.toString().toLowerCase())
            .parameter(((QName) actionCondition.getParameterValues().get(IsSubTypeEvaluator.PARAM_TYPE)).toPrefixString(namespaceService))
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
