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

import org.alfresco.repo.action.ActionConditionImpl;
import org.alfresco.repo.action.evaluator.ComparePropertyValueEvaluator;
import org.alfresco.service.Experimental;
import org.alfresco.service.cmr.action.ActionCondition;
import org.assertj.core.api.Condition;
import org.junit.Test;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Experimental
public class CompositeConditionTest
{

    @Test
    public void testFrom()
    {
        final List<ActionCondition> actionConditions = List.of(
            createActionCondition(),
            createActionCondition(true),
            createActionCondition()
        );

        // when
        final CompositeCondition compositeCondition = CompositeCondition.from(actionConditions);

        assertThat(compositeCondition).is(havingCompositeConditions(2, false));
        assertThat(compositeCondition.getCompositeConditions().get(0)).is(havingSimpleConditions(2, false));
        assertThat(compositeCondition.getCompositeConditions().get(1)).is(havingSimpleConditions(1, true));
    }

    @SuppressWarnings("SameParameterValue")
    private static Condition<CompositeCondition> havingCompositeConditions(final int expectedSize, final boolean inverted)
    {
        var ref = new Object() { CompositeCondition compositeCondition; };
        return new Condition<>(
            condition -> {
                ref.compositeCondition = condition;
                assertThat(condition)
                        .isNotNull()
                    .extracting(CompositeCondition::getCompositeConditions)
                        .isNotNull()
                    .extracting(Collection::size)
                        .isEqualTo(expectedSize);
                assertThat(condition.isInverted()).isEqualTo(inverted);
                assertThat(condition.getBooleanMode()).isEqualTo(ConditionOperator.AND);
                assertThat(condition.getSimpleConditions()).isNull();
                return true;
            },
            String.format("containing compositeCondition=%s", ref.compositeCondition)
        );
    }

    private static Condition<CompositeCondition> havingSimpleConditions(final int expectedSize, final boolean inverted)
    {
        var ref = new Object() { CompositeCondition compositeCondition; };
        return new Condition<>(
            condition -> {
                ref.compositeCondition = condition;
                assertThat(condition)
                        .isNotNull()
                    .extracting(CompositeCondition::getSimpleConditions)
                        .isNotNull()
                    .extracting(Collection::size)
                        .isEqualTo(expectedSize);
                assertThat(condition.isInverted()).isEqualTo(inverted);
                assertThat(condition.getBooleanMode()).isEqualTo(ConditionOperator.AND);
                assertThat(condition.getCompositeConditions()).isNull();
                return true;
            },
            String.format("containing compositeCondition=%s", ref.compositeCondition)
        );
    }

    private static ActionCondition createActionCondition()
    {
        return createActionCondition(false);
    }

    private static ActionCondition createActionCondition(final boolean inverted)
    {
        final ActionCondition actionCondition = new ActionConditionImpl("fake-id", ComparePropertyValueEvaluator.NAME);
        actionCondition.setInvertCondition(inverted);
        final Map<String, Serializable> parameterValues = new HashMap<>();
        parameterValues.put(ComparePropertyValueEvaluator.PARAM_CONTENT_PROPERTY, "content-property");
        parameterValues.put(ComparePropertyValueEvaluator.PARAM_OPERATION, "operation");
        parameterValues.put(ComparePropertyValueEvaluator.PARAM_VALUE, "value");
        actionCondition.setParameterValues(parameterValues);
        return actionCondition;
    }
}