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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.rometools.utils.Strings;
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
import org.alfresco.rest.api.model.mapper.RestModelMapper;
import org.alfresco.rest.api.model.rules.SimpleCondition;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.service.Experimental;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.collections.MapUtils;

@Experimental
public class RestRuleSimpleConditionModelMapper implements RestModelMapper<SimpleCondition, ActionCondition>
{
    static final String CATEGORY_INVALID_MSG = "Category in condition is invalid";
    static final String PARAM_CATEGORY = "category";
    static final String PARAM_MIMETYPE = "mimetype";
    static final String FIELD_NOT_NULL = "Field in condition must not be blank";
    static final String PARAMETER_NOT_NULL = "Parameter in condition must not be blank";
    static final String COMPARATOR_NOT_NULL = "Comparator in condition must not be blank";
    static final String INVALID_COMPARATOR_VALUE = "Comparator value for condition is invalid: %s";
    private final NamespaceService namespaceService;
    private final Nodes nodes;

    public RestRuleSimpleConditionModelMapper(NamespaceService namespaceService, Nodes nodes)
    {
        this.namespaceService = namespaceService;
        this.nodes = nodes;
    }

    @Override
    public SimpleCondition toRestModel(ActionCondition actionCondition)
    {
        if (actionCondition == null || actionCondition.getActionConditionDefinitionName() == null ||
                MapUtils.isEmpty(actionCondition.getParameterValues()))
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

    @Override
    public ActionCondition toServiceModel(SimpleCondition restModel)
    {
        final String field = restModel.getField();
        checkStringNotBlank(field, FIELD_NOT_NULL);

        final Map<String, Serializable> parameterValues = new HashMap<>();
        String conditionDefinitionId;
        final String parameter = restModel.getParameter();
        checkStringNotBlank(parameter, PARAMETER_NOT_NULL);

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
                    parameterValues.put(InCategoryEvaluator.PARAM_CATEGORY_VALUE, nodes.validateOrLookupNode(parameter));
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
                checkStringNotBlank(restModel.getComparator(), COMPARATOR_NOT_NULL);
                parameterValues.put(ComparePropertyValueEvaluator.PARAM_OPERATION, getComparatorValue(restModel.getComparator()));
                parameterValues.put(ComparePropertyValueEvaluator.PARAM_VALUE, parameter);
                break;
        }
        return new ActionConditionImpl(UUID.randomUUID().toString(), conditionDefinitionId, parameterValues);
    }

    private String getComparatorValue(String comparator)
    {
        try
        {
            return ComparePropertyValueOperation.valueOf(comparator.toUpperCase()).toString();
        } catch (IllegalArgumentException e) {
            throw new InvalidArgumentException(String.format(INVALID_COMPARATOR_VALUE, comparator));
        }
    }

    private void checkStringNotBlank(final String string, final String message) {
        if (Strings.isBlank(string))
        {
            throw new InvalidArgumentException(message);
        }
    }

    private static SimpleCondition createComparePropertyValueCondition(final ActionCondition actionCondition, final NamespaceService namespaceService)
    {
        final SimpleCondition.Builder builder = SimpleCondition.builder();
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
        return SimpleCondition.builder()
                .field(PARAM_MIMETYPE)
                .comparator(ComparePropertyValueOperation.EQUALS.toString().toLowerCase())
                .parameter(actionCondition.getParameterValues().get(ComparePropertyValueEvaluator.PARAM_VALUE).toString())
                .create();
    }

    private static SimpleCondition createHasAspectCondition(final ActionCondition actionCondition, final NamespaceService namespaceService)
    {
        return SimpleCondition.builder()
                .field(HasAspectEvaluator.PARAM_ASPECT)
                .comparator(ComparePropertyValueOperation.EQUALS.toString().toLowerCase())
                .parameter(((QName) actionCondition.getParameterValues().get(HasAspectEvaluator.PARAM_ASPECT)).toPrefixString(namespaceService))
                .create();
    }

    private static SimpleCondition createHasTagCondition(final ActionCondition actionCondition)
    {
        return SimpleCondition.builder()
                .field(HasTagEvaluator.PARAM_TAG)
                .comparator(ComparePropertyValueOperation.EQUALS.toString().toLowerCase())
                .parameter(actionCondition.getParameterValues().get(HasTagEvaluator.PARAM_TAG).toString())
                .create();
    }

    private static SimpleCondition createInCategoryCondition(final ActionCondition actionCondition)
    {
        return SimpleCondition.builder()
                .field(PARAM_CATEGORY)
                .comparator(ComparePropertyValueOperation.EQUALS.toString().toLowerCase())
                .parameter(((NodeRef) actionCondition.getParameterValues().get(InCategoryEvaluator.PARAM_CATEGORY_VALUE)).getId())
                .create();
    }

    private static SimpleCondition createIsSubtypeCondition(final ActionCondition actionCondition, final NamespaceService namespaceService)
    {
        return SimpleCondition.builder()
                .field(IsSubTypeEvaluator.PARAM_TYPE)
                .comparator(ComparePropertyValueOperation.EQUALS.toString().toLowerCase())
                .parameter(((QName) actionCondition.getParameterValues().get(IsSubTypeEvaluator.PARAM_TYPE)).toPrefixString(namespaceService))
                .create();
    }
}
