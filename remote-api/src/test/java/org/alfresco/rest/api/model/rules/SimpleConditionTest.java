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

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.alfresco.repo.action.ActionConditionImpl;
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
import org.junit.Test;

@Experimental
public class SimpleConditionTest
{

    private static List<TestData> getTestData() {
        return List.of(
            TestData.of(ComparePropertyValueEvaluator.NAME),
            TestData.of(CompareMimeTypeEvaluator.NAME),
            TestData.of(HasAspectEvaluator.NAME),
            TestData.of(HasChildEvaluator.NAME),
            TestData.of(HasTagEvaluator.NAME),
            TestData.of(HasVersionHistoryEvaluator.NAME),
            TestData.of(InCategoryEvaluator.NAME),
            TestData.of(IsSubTypeEvaluator.NAME),
            TestData.of(NoConditionEvaluator.NAME, true),
            TestData.of("fake-definition-name", true),
            TestData.of("", true),
            TestData.of(null, true)
        );
    }

    @Test
    public void testFrom()
    {
        for (TestData testData : getTestData())
        {
            final ActionCondition actionCondition = createActionCondition(testData.actionDefinitionName);

            // when
            final SimpleCondition simpleCondition = SimpleCondition.from(actionCondition);

            assertThat(Objects.isNull(simpleCondition)).isEqualTo(testData.isNullResult);
            if (!testData.isNullResult)
            {
                assertThat(simpleCondition.getField()).isNotEmpty();
                assertThat(simpleCondition.getComparator()).isNotEmpty();
                assertThat(simpleCondition.getParameter()).isNotEmpty();
            }
        }
    }

    private static ActionCondition createActionCondition(final String actionDefinitionName)
    {
        return new ActionConditionImpl("fake-id", actionDefinitionName, createParameterValues());
    }

    private static Map<String, Serializable> createParameterValues() {
        final Map<String, Serializable> parameterValues = new HashMap<>();
        parameterValues.put(ComparePropertyValueEvaluator.PARAM_CONTENT_PROPERTY, "content-property");
        parameterValues.put(HasChildEvaluator.PARAM_ASSOC_TYPE, "assoc-type");
        parameterValues.put(ComparePropertyValueEvaluator.PARAM_PROPERTY, "property");
        parameterValues.put(ComparePropertyValueEvaluator.PARAM_OPERATION, "operation");
        parameterValues.put(ComparePropertyValueEvaluator.PARAM_VALUE, "value");
        parameterValues.put(HasAspectEvaluator.PARAM_ASPECT, "aspect");
        parameterValues.put(HasChildEvaluator.PARAM_ASSOC_NAME, "assoc-name");
        parameterValues.put(HasTagEvaluator.PARAM_TAG, "tag");
        parameterValues.put(InCategoryEvaluator.PARAM_CATEGORY_ASPECT, "category-aspect");
        parameterValues.put(InCategoryEvaluator.PARAM_CATEGORY_VALUE, "category-value");
        parameterValues.put(IsSubTypeEvaluator.PARAM_TYPE, "type");

        return parameterValues;
    }

    private static class TestData
    {
        String actionDefinitionName;
        boolean isNullResult;

        public TestData(String actionDefinitionName, boolean isNullResult)
        {
            this.actionDefinitionName = actionDefinitionName;
            this.isNullResult = isNullResult;
        }

        public static TestData of(String actionDefinitionName) {
            return new TestData(actionDefinitionName, false);
        }

        public static TestData of(String actionDefinitionName, boolean isNullResult) {
            return new TestData(actionDefinitionName, isNullResult);
        }
    }
}