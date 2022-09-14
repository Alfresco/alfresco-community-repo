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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.action.ActionConditionImpl;
import org.alfresco.repo.action.evaluator.ComparePropertyValueEvaluator;
import org.alfresco.service.Experimental;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.namespace.NamespaceService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@Experimental
@RunWith(MockitoJUnitRunner.class)
public class CompositeConditionTest
{

    private final NamespaceService namespaceService = mock(NamespaceService.class);

    @Test
    public void testFrom()
    {
        final List<ActionCondition> actionConditions = List.of(
            createActionCondition("value1"),
            createActionCondition("value2", true),
            createActionCondition("value3")
        );
        final CompositeCondition expectedCompositeCondition = createCompositeCondition(List.of(
            createCompositeCondition(false, List.of(
                createSimpleCondition("value1"),
                createSimpleCondition("value3")
            )),
            createCompositeCondition(true, List.of(
                createSimpleCondition("value2")
            ))
        ));

        // when
        final CompositeCondition actualCompositeCondition = CompositeCondition.from(actionConditions, namespaceService);

        assertThat(actualCompositeCondition).isNotNull().usingRecursiveComparison().isEqualTo(expectedCompositeCondition);
    }

    @Test
    public void testFromEmptyList()
    {
        final List<ActionCondition> actionConditions = Collections.emptyList();
        final CompositeCondition expectedCompositeCondition = CompositeCondition.builder().create();

        // when
        final CompositeCondition actualCompositeCondition = CompositeCondition.from(actionConditions, namespaceService);

        assertThat(actualCompositeCondition).isNotNull().usingRecursiveComparison().isEqualTo(expectedCompositeCondition);
    }

    @Test
    public void testFromNullValue()
    {
        // when
        final CompositeCondition actualCompositeCondition = CompositeCondition.from(null, namespaceService);

        assertThat(actualCompositeCondition).isNull();
    }

    @Test
    public void testFromListContainingNull()
    {
        final List<ActionCondition> actionConditions = new ArrayList<>();
        actionConditions.add(null);
        final CompositeCondition expectedCompositeCondition = CompositeCondition.builder().create();

        // when
        final CompositeCondition actualCompositeCondition = CompositeCondition.from(actionConditions, namespaceService);

        assertThat(actualCompositeCondition).isNotNull().usingRecursiveComparison().isEqualTo(expectedCompositeCondition);
    }

    @Test
    public void testOfSimpleConditions()
    {
        final List<SimpleCondition> simpleConditions = List.of(SimpleCondition.builder().field("field").comparator("comparator").parameter("param").create());
        final boolean inverted = true;
        final ConditionOperator conditionOperator = ConditionOperator.OR;
        final CompositeCondition expectedCondition = createCompositeCondition(inverted, conditionOperator, null, simpleConditions);

        // when
        final CompositeCondition actualCompositeCondition = CompositeCondition.ofSimpleConditions(simpleConditions, inverted, conditionOperator);

        assertThat(actualCompositeCondition).isNotNull().usingRecursiveComparison().isEqualTo(expectedCondition);
    }

    @Test
    public void testOfEmptySimpleConditions()
    {
        // when
        final CompositeCondition actualCompositeCondition = CompositeCondition.ofSimpleConditions(Collections.emptyList(), false, ConditionOperator.AND);

        assertThat(actualCompositeCondition).isNull();
    }

    @Test
    public void testOfNullSimpleConditions()
    {
        // when
        final CompositeCondition actualCompositeCondition = CompositeCondition.ofSimpleConditions(null, false, ConditionOperator.AND);

        assertThat(actualCompositeCondition).isNull();
    }

    private static ActionCondition createActionCondition(final String value)
    {
        return createActionCondition(value, false);
    }

    private static ActionCondition createActionCondition(final String value, final boolean inverted)
    {
        final ActionCondition actionCondition = new ActionConditionImpl("fake-id", ComparePropertyValueEvaluator.NAME);
        actionCondition.setInvertCondition(inverted);
        final Map<String, Serializable> parameterValues = new HashMap<>();
        parameterValues.put(ComparePropertyValueEvaluator.PARAM_CONTENT_PROPERTY, "content-property");
        parameterValues.put(ComparePropertyValueEvaluator.PARAM_OPERATION, "operation");
        parameterValues.put(ComparePropertyValueEvaluator.PARAM_VALUE, value);
        actionCondition.setParameterValues(parameterValues);
        return actionCondition;
    }

    private static SimpleCondition createSimpleCondition(final String value) {
        return SimpleCondition.builder()
            .field("content-property")
            .comparator("operation")
            .parameter(value)
            .create();
    }

    private static CompositeCondition createCompositeCondition(final List<CompositeCondition> compositeConditions) {
        return createCompositeCondition(false, ConditionOperator.AND, compositeConditions, null);
    }

    private static CompositeCondition createCompositeCondition(final boolean inverted, final List<SimpleCondition> simpleConditions) {
        return createCompositeCondition(inverted, ConditionOperator.AND, null, simpleConditions);
    }

    private static CompositeCondition createCompositeCondition(final boolean inverted, final ConditionOperator conditionOperator,
        final List<CompositeCondition> compositeConditions, final List<SimpleCondition> simpleConditions) {
        return CompositeCondition.builder()
            .inverted(inverted)
            .booleanMode(conditionOperator)
            .compositeConditions(compositeConditions)
            .simpleConditions(simpleConditions)
            .create();
    }
}