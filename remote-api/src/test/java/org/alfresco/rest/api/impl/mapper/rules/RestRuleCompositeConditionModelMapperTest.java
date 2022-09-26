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
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.alfresco.repo.action.ActionConditionImpl;
import org.alfresco.repo.action.evaluator.ComparePropertyValueEvaluator;
import org.alfresco.rest.api.model.mapper.RestModelMapper;
import org.alfresco.rest.api.model.rules.CompositeCondition;
import org.alfresco.rest.api.model.rules.ConditionOperator;
import org.alfresco.rest.api.model.rules.SimpleCondition;
import org.alfresco.service.Experimental;
import org.alfresco.service.cmr.action.ActionCondition;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@Experimental
@RunWith(MockitoJUnitRunner.class)
public class RestRuleCompositeConditionModelMapperTest
{

    @Mock
    private RestModelMapper<SimpleCondition, ActionCondition> simpleConditionMapperMock;

    @InjectMocks
    private RestRuleCompositeConditionModelMapper objectUnderTest;

    @Test
    public void testToRestModel()
    {
        final List<ActionCondition> actionConditions = List.of(
                createActionCondition("value1"),
                createActionCondition("value3"),
                createActionCondition("value2", true)
        );
        final List<SimpleCondition> simpleConditions = List.of(
                createSimpleCondition("value1"),
                createSimpleCondition("value3"),
                createSimpleCondition("value2")
        );

        final CompositeCondition expectedCompositeCondition = createCompositeCondition(List.of(
            createCompositeCondition(false, simpleConditions.subList(0,2)),
            createCompositeCondition(true, simpleConditions.subList(2,3))
        ));
        given(simpleConditionMapperMock.toRestModels(actionConditions.subList(2,3))).willReturn(simpleConditions.subList(2,3));
        given(simpleConditionMapperMock.toRestModels(actionConditions.subList(0,2))).willReturn(simpleConditions.subList(0,2));

        // when
        final CompositeCondition actualCompositeCondition = objectUnderTest.toRestModel(actionConditions);

        assertThat(actualCompositeCondition).isNotNull().usingRecursiveComparison().isEqualTo(expectedCompositeCondition);
    }

    @Test
    public void testToRestModel_fromEmptyList()
    {
        final List<ActionCondition> actionConditions = Collections.emptyList();

        // when
        final CompositeCondition actualCompositeCondition = objectUnderTest.toRestModel(actionConditions);

        assertThat(actualCompositeCondition).isNull();
    }

    @Test
    public void testToRestModel_fromNullValue()
    {
        // when
        final CompositeCondition actualCompositeCondition = objectUnderTest.toRestModel((Collection<ActionCondition>) null);

        assertThat(actualCompositeCondition).isNull();
    }

    @Test
    public void testToRestModel_fromListContainingNullsOnly()
    {
        final List<ActionCondition> actionConditions = new ArrayList<>();
        actionConditions.add(null);
        actionConditions.add(null);

        // when
        final CompositeCondition actualCompositeCondition = objectUnderTest.toRestModel(actionConditions);

        assertThat(actualCompositeCondition).isNull();
    }

    @Test
    public void testToRestModel_fromNoCondition()
    {
        final List<ActionCondition> actionConditions = new ArrayList<>();
        final ActionCondition noCondition = new ActionConditionImpl("fake-id", NAME);
        actionConditions.add(noCondition);

        // when
        final CompositeCondition actualCompositeCondition = objectUnderTest.toRestModel(actionConditions);

        assertThat(actualCompositeCondition).isNull();
    }

    @Test
    public void testToServiceModels() {
        final List<SimpleCondition> simpleConditions = List.of(
                createSimpleCondition("value1"),
                createSimpleCondition("value3"),
                createSimpleCondition("value2")
        );
        final CompositeCondition compositeCondition = createCompositeCondition(List.of(
                createCompositeCondition(false, simpleConditions.subList(0,2)),
                createCompositeCondition(true, simpleConditions.subList(2,3))
        ));
        final List<ActionCondition> actionConditions = List.of(
                createActionCondition("value1"),
                createActionCondition("value3"),
                createActionCondition("value2", true)
        );

        IntStream.rangeClosed(0, 2)
                .forEach(i -> given(simpleConditionMapperMock.toServiceModel(simpleConditions.get(i))).willReturn(actionConditions.get(i)));

        final List<ActionCondition> actualActionConditions = objectUnderTest.toServiceModels(compositeCondition);
        assertThat(actualActionConditions).isEqualTo(actionConditions);
    }

    @Test
    public void testToServiceModels_simpleNonInvertedConditionsOnly() {
        final List<SimpleCondition> simpleConditions = List.of(
                createSimpleCondition("value1"),
                createSimpleCondition("value2"),
                createSimpleCondition("value3")
        );
        final CompositeCondition compositeCondition = createCompositeCondition(false, simpleConditions);
        final List<ActionCondition> actionConditions = List.of(
                createActionCondition("value1"),
                createActionCondition("value2"),
                createActionCondition("value3")
        );

        IntStream.rangeClosed(0, 2)
                .forEach(i -> given(simpleConditionMapperMock.toServiceModel(simpleConditions.get(i))).willReturn(actionConditions.get(i)));

        final List<ActionCondition> actualActionConditions = objectUnderTest.toServiceModels(compositeCondition);
        assertThat(actualActionConditions).isEqualTo(actionConditions);
    }

    @Test
    public void testToServiceModels_nullSimpleConditions() {
        final CompositeCondition compositeCondition = createCompositeCondition(false, null);

        final List<ActionCondition> actualActionConditions = objectUnderTest.toServiceModels(compositeCondition);
        assertThat(actualActionConditions).isNotNull().isEmpty();
    }

    @Test
    public void testToServiceModels_emptyCompositeCondition() {
        final CompositeCondition compositeCondition = CompositeCondition.builder().create();

        final List<ActionCondition> actualActionConditions = objectUnderTest.toServiceModels(compositeCondition);
        assertThat(actualActionConditions).isNotNull().isEmpty();
    }

    @Test
    public void testToServiceModels_nullCompositeCondition() {
        final CompositeCondition compositeCondition = null;

        final List<ActionCondition> actualActionConditions = objectUnderTest.toServiceModels(compositeCondition);
        assertThat(actualActionConditions).isNotNull().isEmpty();
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
