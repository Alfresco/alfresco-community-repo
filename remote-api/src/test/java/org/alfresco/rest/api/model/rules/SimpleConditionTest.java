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
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.action.ActionConditionImpl;
import org.alfresco.repo.action.evaluator.ComparePropertyValueEvaluator;
import org.alfresco.repo.action.evaluator.compare.ComparePropertyValueOperation;
import org.alfresco.rest.api.impl.mapper.rules.RestRuleSimpleConditionModelMapper;
import org.alfresco.rest.api.model.mapper.RestModelMapper;
import org.alfresco.service.Experimental;
import org.alfresco.service.cmr.action.ActionCondition;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@Experimental
@RunWith(MockitoJUnitRunner.class)
public class SimpleConditionTest
{
    private static final boolean INVERTED = true;
    private static final String VALUE = "value";
    private static final String KEY = "key";

    private final RestModelMapper<SimpleCondition, ActionCondition> simpleConditionMapperMock = mock(RestRuleSimpleConditionModelMapper.class);

    @Test
    public void testFrom()
    {
        final ActionCondition actionCondition = createActionCondition(ComparePropertyValueEvaluator.NAME);
        final SimpleCondition simpleConditionMock = mock(SimpleCondition.class);
        given(simpleConditionMapperMock.toRestModel(actionCondition)).willReturn(simpleConditionMock);

        //when
        final SimpleCondition actualSimpleCondition = SimpleCondition.from(actionCondition, simpleConditionMapperMock);

        then(simpleConditionMapperMock).should().toRestModel(actionCondition);
        then(simpleConditionMapperMock).shouldHaveNoMoreInteractions();
        assertThat(actualSimpleCondition).isEqualTo(simpleConditionMock);
    }

    @Test
    public void testFromNullValue()
    {
        // when
        final SimpleCondition actualSimpleCondition = SimpleCondition.from(null, simpleConditionMapperMock);

        then(simpleConditionMapperMock).shouldHaveNoInteractions();
        assertThat(actualSimpleCondition).isNull();
    }

    @Test
    public void testFromActionConditionWithoutDefinitionName()
    {
        final ActionCondition actionCondition = new ActionConditionImpl("fake-id", null, Map.of(KEY, VALUE));

        // when
        final SimpleCondition actualSimpleCondition = SimpleCondition.from(actionCondition, simpleConditionMapperMock);

        then(simpleConditionMapperMock).shouldHaveNoInteractions();
        assertThat(actualSimpleCondition).isNull();
    }

    @Test
    public void testFromActionConditionWithoutParameterValues()
    {
        final ActionCondition actionCondition = new ActionConditionImpl("fake-id", "fake-def-name", null);

        // when
        final SimpleCondition actualSimpleCondition = SimpleCondition.from(actionCondition, simpleConditionMapperMock);

        then(simpleConditionMapperMock).shouldHaveNoInteractions();
        assertThat(actualSimpleCondition).isNull();
    }

    @Test
    public void testListOf()
    {
        final List<ActionCondition> actionConditionsMock = mock(List.class);
        final List<SimpleCondition> simpleConditionsMock = mock(List.class);
        given(simpleConditionMapperMock.toRestModels(actionConditionsMock)).willReturn(simpleConditionsMock);

        // when
        final List<SimpleCondition> actualSimpleConditions = SimpleCondition.listOf(actionConditionsMock, simpleConditionMapperMock);

        then(simpleConditionMapperMock).should().toRestModels(actionConditionsMock);
        then(simpleConditionMapperMock).shouldHaveNoMoreInteractions();
        assertThat(actualSimpleConditions)
            .isNotNull()
            .containsExactlyElementsOf(simpleConditionsMock);
    }

    @Test
    public void testListOfEmptyActionConditions()
    {
        // when
        final List<SimpleCondition> actualSimpleConditions = SimpleCondition.listOf(Collections.emptyList(), simpleConditionMapperMock);

        assertThat(actualSimpleConditions).isNull();
    }

    @Test
    public void testListOfNullActionConditions()
    {
        // when
        final List<SimpleCondition> actualSimpleConditions = SimpleCondition.listOf(null, simpleConditionMapperMock);

        then(simpleConditionMapperMock).shouldHaveNoInteractions();
        assertThat(actualSimpleConditions).isNull();
    }

    @Test
    public void testListOfActionConditionsContainingNull()
    {
        final List<ActionCondition> actionConditions = new ArrayList<>();
        actionConditions.add(null);

        // when
        final List<SimpleCondition> actualSimpleConditions = SimpleCondition.listOf(actionConditions, simpleConditionMapperMock);

        then(simpleConditionMapperMock).should().toRestModels(actionConditions);
        then(simpleConditionMapperMock).shouldHaveNoMoreInteractions();
        assertThat(actualSimpleConditions).isNotNull().isEmpty();
    }

    @Test
    public void testToServiceModel_notInverted()
    {
        final SimpleCondition simpleCondition = createSimpleCondition("field");
        final ActionCondition actionCondition = createActionCondition(ComparePropertyValueEvaluator.NAME);
        given(simpleConditionMapperMock.toServiceModel(simpleCondition)).willReturn(actionCondition);

        // when
        final ActionCondition actualActionCondition = simpleCondition.toServiceModel(!INVERTED, simpleConditionMapperMock);

        assertThat(actualActionCondition).isEqualTo(actionCondition);
    }

    @Test
    public void testToServiceModel_inverted()
    {
        final SimpleCondition simpleCondition = createSimpleCondition("field");
        final ActionCondition actionCondition = createActionCondition(ComparePropertyValueEvaluator.NAME);
        given(simpleConditionMapperMock.toServiceModel(simpleCondition)).willReturn(actionCondition);

        // when
        final ActionCondition actualActionCondition = simpleCondition.toServiceModel(INVERTED, simpleConditionMapperMock);

        assertThat(actualActionCondition).isEqualTo(actionCondition);
    }

    private static ActionCondition createActionCondition(final String actionDefinitionName)
    {
        return new ActionConditionImpl("fake-id", actionDefinitionName, Map.of(KEY, VALUE));
    }

    private static SimpleCondition createSimpleCondition(final String field)
    {
        return createSimpleCondition(field, VALUE);
    }

    private static SimpleCondition createSimpleCondition(final String field, final String parameter)
    {
        return SimpleCondition.builder()
            .field(field)
            .comparator(ComparePropertyValueOperation.EQUALS.toString().toLowerCase())
            .parameter(parameter)
            .create();
    }
}
